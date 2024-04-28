//
// Created by maks on 21.09.2022.
//
#include <stddef.h>
#include <stdlib.h>
#include <dlfcn.h>
#include "egl_loader.h"

typedef struct {
    EGLBoolean (*eglMakeCurrent_p) (EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx);
    EGLBoolean (*eglDestroyContext_p) (EGLDisplay dpy, EGLContext ctx);
    EGLBoolean (*eglDestroySurface_p) (EGLDisplay dpy, EGLSurface surface);
    EGLBoolean (*eglTerminate_p) (EGLDisplay dpy);
    EGLBoolean (*eglReleaseThread_p) (void);
    EGLContext (*eglGetCurrentContext_p) (void);
    EGLDisplay (*eglGetDisplay_p) (NativeDisplayType display);
    EGLBoolean (*eglInitialize_p) (EGLDisplay dpy, EGLint *major, EGLint *minor);
    EGLBoolean (*eglChooseConfig_p) (EGLDisplay dpy, const EGLint *attrib_list, EGLConfig *configs, EGLint config_size, EGLint *num_config);
    EGLBoolean (*eglGetConfigAttrib_p) (EGLDisplay dpy, EGLConfig config, EGLint attribute, EGLint *value);
    EGLBoolean (*eglBindAPI_p) (EGLenum api);
    EGLSurface (*eglCreatePbufferSurface_p) (EGLDisplay dpy, EGLConfig config, const EGLint *attrib_list);
    EGLSurface (*eglCreateWindowSurface_p) (EGLDisplay dpy, EGLConfig config, NativeWindowType window, const EGLint *attrib_list);
    EGLBoolean (*eglSwapBuffers_p) (EGLDisplay dpy, EGLSurface draw);
    EGLint (*eglGetError_p) (void);
    EGLContext (*eglCreateContext_p) (EGLDisplay dpy, EGLConfig config, EGLContext share_list, const EGLint *attrib_list);
    EGLBoolean (*eglSwapInterval_p) (EGLDisplay dpy, EGLint interval);
    EGLSurface (*eglGetCurrentSurface_p) (EGLint readdraw);
} EGLFunctions;

void dlsym_EGL(EGLFunctions* functions) {
    void* dl_handle = NULL;
    if(getenv("POJAVEXEC_EGL")) dl_handle = dlopen(getenv("POJAVEXEC_EGL"), RTLD_LAZY);
    if(dl_handle == NULL) dl_handle = dlopen("libEGL.so", RTLD_LAZY);
    if(dl_handle == NULL) abort();

    functions->eglBindAPI_p = dlsym(dl_handle,"eglBindAPI");
    if (functions->eglBindAPI_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglBindAPI'\n");
        abort();
    }

    functions->eglChooseConfig_p = dlsym(dl_handle, "eglChooseConfig");
    if (functions->eglChooseConfig_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglChooseConfig'\n");
        abort();
    }

    functions->eglCreateContext_p = dlsym(dl_handle, "eglCreateContext");
    if (functions->eglCreateContext_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglCreateContext'\n");
        abort();
    }

    functions->eglCreatePbufferSurface_p = dlsym(dl_handle, "eglCreatePbufferSurface");
    if (functions->eglCreatePbufferSurface_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglCreatePbufferSurface'\n");
        abort();
    }

    functions->eglCreateWindowSurface_p = dlsym(dl_handle, "eglCreateWindowSurface");
    if (functions->eglCreateWindowSurface_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglCreateWindowSurface'\n");
        abort();
    }

    functions->eglDestroyContext_p = dlsym(dl_handle, "eglDestroyContext");
    if (functions->eglDestroyContext_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglDestroyContext'\n");
        abort();
    }

    functions->eglDestroySurface_p = dlsym(dl_handle, "eglDestroySurface");
    if (functions->eglDestroySurface_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglDestroySurface'\n");
        abort();
    }

    functions->eglGetConfigAttrib_p = dlsym(dl_handle, "eglGetConfigAttrib");
    if (functions->eglGetConfigAttrib_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglGetConfigAttrib'\n");
        abort();
    }

    functions->eglGetDisplay_p = dlsym(dl_handle, "eglGetDisplay");
    if (functions->eglGetDisplay_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglGetDisplay'\n");
        abort();
    }

    functions->eglGetError_p = dlsym(dl_handle, "eglGetError");
    if (functions->eglGetError_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglGetError'\n");
        abort();
    }

    functions->eglInitialize_p = dlsym(dl_handle, "eglInitialize");
    if (functions->eglInitialize_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglInitialize'\n");
        abort();
    }

    functions->eglMakeCurrent_p = dlsym(dl_handle, "eglMakeCurrent");
    if (functions->eglMakeCurrent_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglMakeCurrent'\n");
        abort();
    }

    functions->eglSwapBuffers_p = dlsym(dl_handle, "eglSwapBuffers");
    if (functions->eglSwapBuffers_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglSwapBuffers'\n");
        abort();
    }

    functions->eglReleaseThread_p = dlsym(dl_handle, "eglReleaseThread");
    if (functions->eglReleaseThread_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglReleaseThread'\n");
        abort();
    }

    functions->eglSwapInterval_p = dlsym(dl_handle, "eglSwapInterval");
    if (functions->eglSwapInterval_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglSwapInterval'\n");
        abort();
    }

    functions->eglTerminate_p = dlsym(dl_handle, "eglTerminate");
    if (functions->eglTerminate_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglTerminate'\n");
        abort();
    }

    functions->eglGetCurrentSurface_p = dlsym(dl_handle,"eglGetCurrentSurface");
    if (functions->eglGetCurrentSurface_p == NULL) {
        fprintf(stderr, "Error: Unable to load symbol 'eglGetCurrentSurface'\n");
        abort();
    }
}
