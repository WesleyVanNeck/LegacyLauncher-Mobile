//
// Created by maks on 19.06.2023.
//

#define _GNU_SOURCE // we are GNU GPLv3

#include <linux/limits.h>
#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sched.h>
#include <string.h>

#define FREQ_MAX 256
#define CPU_PATH_MAX (PATH_MAX - 20) // adjust for the length of "/cpufreq/cpuinfo_max_freq"

void bigcore_format_cpu_path(char* buffer, unsigned int cpu_core) {
    snprintf(buffer, CPU_PATH_MAX, "/sys/devices/system/cpu/cpu%u/cpufreq/cpuinfo_max_freq", cpu_core);
}

int is_positive(unsigned long x) {
    return x > 0;
}

int is_valid_core_id(unsigned int core_id, unsigned int corecnt) {
    return core_id < corecnt;
}

void bigcore_set_affinity() {
    char path_buffer[PATH_MAX];
    char freq_buffer[FREQ_MAX];
    char* discard;
    unsigned long core_freq;
    unsigned long max_freq = 0;
    unsigned int corecnt = 0;
    unsigned int big_core_id = 0;

    while(1) {
        bigcore_format_cpu_path(path_buffer, corecnt);
        int corefreqfd = open(path_buffer, O_RDONLY);
        if(corefreqfd == -1) {
            break;
        }

        ssize_t read_count = read(corefreqfd, freq_buffer, FREQ_MAX);
        if (read_count <= 0) {
            close(corefreqfd);
            break;
        }
        freq_buffer[read_count] = 0;

        core_freq = strtoul(freq_buffer, &discard, 10);
        if (*discard != '\0' || !is_positive(core_freq)) {
            close(corefreqfd);
            break;
        }

        if(core_freq >= max_freq) {
            max_freq = core_freq;
            big_core_id = corecnt;
        }

        close(corefreqfd);
        corecnt++;
    }

    if (is_valid_core_id(big_core_id, corecnt)) {
        printf("bigcore: big CPU number is %u, frequency %lu Hz\n", big_core_id, max_freq);
        cpu_set_t bigcore_affinity_set;
        CPU_ZERO(&bigcore_affinity_set);
        CPU_SET_S(big_core_id, CPU_SETSIZE, &bigcore_affinity_set);
        int result = sched_setaffinity(0, CPU_SETSIZE, &bigcore_affinity_set);
        if(result == 0) {
            printf("bigcore: forced current thread onto big core\n");
        } else {
            printf("bigcore: setting affinity failed: %s\n", strerror(result));
        }
    } else {
        printf("bigcore: no valid CPU cores found\n");
    }
}
