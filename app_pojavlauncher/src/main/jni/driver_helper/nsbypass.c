#include <android/dlext.h>
#include <android/log.h>
#include <fcntl.h>
#include <linux/limits.h>
#include <mach/mach_init.h>
#include <mach/mach_port.h>
#include <mach/mach_vm.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/user.h>
#include <sys/types.h>
#include <unistd.h>

#define  LOG_TAG    "nsbypass"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define PAGE_SIZE (4096)
#define SEARCH_PATH "/system/lib64"
#define ELF_EHDR Elf64_Ehdr
#define ELF_SHDR Elf64_Shdr
#define ELF_HALF Elf64_Half
#define ELF_XWORD Elf64_Xword
#define ELF_DYN Elf64_Dyn

static ld_android_create_namespace_t android_create_namespace;
static struct android_namespace_t* driver_namespace;

static void* find_branch_label(void* func_start) {
    void* func_page_start = (void*)(((uintptr_t)func_start) & ~(PAGE_SIZE-1));
    mprotect(func_page_start, PAGE_SIZE, PROT_READ | PROT_EXEC);
    uint32_t* bl_addr = func_start;
    while((*bl_addr & 0b11111100000000000000000000000000) != 0b10010100000000000000000000000000) {
        bl_addr++;
    }
    return ((char*)bl_addr) + (*bl_addr & 0b00000011111111111111111111111111) * 4;
}

static bool linker_ns_load(const char* lib_search_path) {
    loader_dlopen_t loader_dlopen = find_branch_label(&dlopen);
    mprotect((void*)loader_dlopen, PAGE_SIZE, PROT_WRITE | PROT_READ | PROT_EXEC);
    void* ld_android_handle = loader_dlopen("ld-android.so", RTLD_LAZY, &dlopen);
    if (!ld_android_handle) {
        LOGE("Failed to load ld-android.so: %s", dlerror());
        return false;
    }
    android_create_namespace = (ld_android_create_namespace_t)dlsym(ld_android_handle, "__loader_android_create_namespace");
    ld_android_link_namespaces_t android_link_namespaces = (ld_android_link_namespaces_t)dlsym(ld_android_handle, "__loader_android_link_namespaces");
    if (!android_create_namespace || !android_link_namespaces) {
        LOGE("Failed to find required symbols in ld-android.so: %s", dlerror());
        dlclose(ld_android_handle);
        return false;
    }
    __android_log_print(ANDROID_LOG_INFO, "nsbypass", "Found functions at %p %p", android_create_namespace, android_link_namespaces);
    char full_path[strlen(SEARCH_PATH) + strlen(lib_search_path) + 2 + 1];
    sprintf(full_path, "%s:%s", SEARCH_PATH, lib_search_path);
    driver_namespace = local_android_create_namespace("pojav-driver",
                                                      full_path,
                                                      full_path,
                                                      3 /* TYPE_SHAFED | TYPE_ISOLATED */,
                                                      "/system/:/data/:/vendor/:/apex/", NULL);
    if (!driver_namespace) {
        LOGE("Failed to create new namespace");
        dlclose(ld_android_handle);
        return false;
    }
    if (android_link_namespaces(driver_namespace, NULL, "ld-android.so") != 0) {
        LOGE("Failed to link new namespace with ld-android.so: %s", strerror(errno));
        dlclose(ld_android_handle);
        return false;
    }
    if (android_link_namespaces(driver_namespace, NULL, "libnativeloader.so") != 0) {
        LOGE("Failed to link new namespace with libnativeloader.so: %s", strerror(errno));
        dlclose(ld_android_handle);
        return false;
    }
    if (android_link_namespaces(driver_namespace, NULL, "libnativeloader_lazy.so") != 0) {
        LOGE("Failed to link new namespace with libnativeloader_lazy.so: %s", strerror(errno));
        dlclose(ld_android_handle);
        return false;
    }
    dlclose(ld_android_handle);
    return true;
}

static void* linker_ns_dlopen(const char* name, int flag) {
    android_dlextinfo dlextinfo;
    dlextinfo.flags = ANDROID_DLEXT_USE_NAMESPACE;
    dlextinfo.library_namespace = driver_namespace;
    return android_dlopen_ext(name, flag, &dlextinfo);
}

static bool patch_elf_soname(int patchfd, int realfd, uint16_t patchid) {
    struct stat realstat;
    if (fstat(realfd, &realstat) == -1) {
        return false;
    }
    if (ftruncate64(patchfd, realstat.st_size) == -1) {
        return false;
    }
    char* target = mmap(NULL, realstat.st_size, PROT_READ | PROT_WRITE, MAP_SHARED, patchfd, 0);
    if (!target) {
        return false;
    }
    if (read(realfd, target, realstat.st_size) != realstat.st_size) {
        munmap(target, realstat.st_size);
        return false;
    }
    close(realfd);

    ELF_EHDR *ehdr = (ELF_EHDR*)target;
    ELF_SHDR *shdr = (ELF_SHDR*)(target + ehdr->e_shoff);
    for (ELF_HALF i = 0; i < ehdr->e_shnum; i++) {
        ELF_SHDR *hdr = &shdr[i];
        if (hdr->sh_type == SHT_DYNAMIC) {
            char* strtab = target + shdr[hdr->sh_link].sh_offset;
            ELF_DYN *dynEntries = (ELF_DYN*)(target + hdr->sh_offset);
            for (ELF_XWORD k = 0; k < (hdr->sh_size / hdr->sh_entsize); k++) {
                ELF_DYN* dynEntry = &dynEntries[k];
                if (dynEntry->d_tag == DT_SONAME) {
                    char* soname = strtab + dynEntry->d_un.d_val;
                    char sprb[4];
                    snprintf(sprb, 4, "%03x", patchid);
                    memcpy(soname, sprb, 3);
                    munmap(target, realstat.st_size);
                    return true;
                }
            }
        }
    }
    munmap(target, realstat.st_size);
    return false;
}

static void* linker_ns_dlopen_unique(const char* tmpdir, const char* name, int flags) {
    char pathbuf[PATH_MAX];
    static uint16_t patch_id = 0;
    int patch_fd, real_fd;
    snprintf(pathbuf, PATH_MAX, "%s/%d_p.so", tmpdir, patch_id);
    patch_fd = open(pathbuf, O_CREAT | O_RDWR, S_IRUSR | S_IWUSR);
    if (patch_fd == -1) {
        return NULL;
    }
    snprintf(pathbuf, PATH_MAX, "%s/%s", SEARCH_PATH, name);
    real_fd = open(pathbuf, O_RDONLY);
    if (real_fd == -1) {
        close(patch_fd);
        return NULL;
    }
    if (!patch_elf_soname(patch_fd, real_fd, patch_id)) {
        close(patch_fd);
        close(real_fd);
        return NULL;
    }
    android_dlextinfo extinfo;
    extinfo.flags = ANDROID_DLEXT_USE_NAMESPACE | ANDROID_DLEXT_USE_LIBRARY_FD;
    extinfo.library_fd = patch_fd;
    extinfo.library_namespace = driver_namespace;
    snprintf(pathbuf, PATH_MAX, "/proc/self/fd/%d", patch_fd);
    return android_dlopen_ext(pathbuf, flags, &extinfo);
}
