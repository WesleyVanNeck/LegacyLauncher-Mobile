#include <jni.h>
#include <sys/types.h>
#include <stdbool.h>
#include <unistd.h>
#include <pthread.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include <bytehook.h>

//
// Created by maks on 17.02.21.
//

static volatile jobject exitTrap_ctx;
static volatile jclass exitTrap_exitClass;
static volatile jmethodID exitTrap_staticMethod;
static JavaVM *exitTrap_jvm;

static JavaVM *stdiois_jvm;
static int pfd[2];
static pthread_t logger;
static jmethodID logger_onEventLogged;
static volatile jobject logListener = NULL;
static int latestlog_fd = -1;
static _Atomic bool exit_tripped = false;

// Returns true if the buffer should be recorded, false otherwise
static bool recordBuffer(char* buf, ssize_t len) {
    if (strstr(buf, "Session ID is")) {
        return false;
    }
    if (latestlog_fd != -1) {
        write(latestlog_fd, buf, len);
        fdatasync(latestlog_fd);
    }
    return true;
}

// Called when the JVM is loaded
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, __attribute((unused)) void* reserved) {
    stdiois_jvm = vm;
    JNIEnv *env;
    jint result = (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_4);
    if (result != JNI_OK) {
        return result;
    }
    jclass eventLogListener = (*env)->FindClass(env, "net/kdt/pojavlaunch/Logger$eventLogListener");
    if (eventLogListener == NULL) {
        return JNI_EDETACHED;
    }
    logger_onEventLogged = (*env)->GetMethodID(env, eventLogListener, "onEventLogged", "(Ljava/lang/String;)V");
    if (logger_onEventLogged == NULL) {
        return JNI_EDETACHED;
    }
    return JNI_VERSION_1_4;
}

// The logger thread function
static void *logger_thread(void *arg) {
    JNIEnv *env;
    jstring writeString;
    (*stdiois_jvm)->AttachCurrentThread(stdiois_jvm, &env, NULL);
    ssize_t  rsize;
    char buf[2050];
    while((rsize = read(pfd[0], buf, sizeof(buf)-1)) > 0) {
        bool shouldRecordString = recordBuffer(buf, rsize);
        if(buf[rsize-1]=='\n') {
            rsize=rsize-1;
        }
        buf[rsize]=0x00;
        if(shouldRecordString && logListener != NULL) {
            writeString = (*env)->NewStringUTF(env, buf);
            (*env)->CallVoidMethod(env, logListener, logger_onEventLogged, writeString);
            (*env)->DeleteLocalRef(env, writeString);
        }
    }
    (*stdiois_jvm)->DetachCurrentThread(stdiois_jvm);
    return NULL;
}

// Starts the logger
JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_Logger_begin(JNIEnv *env, __attribute((unused)) jclass clazz, jstring logPath) {
    if(latestlog_fd != -1) {
        close(latestlog_fd);
    }
    jclass ioeClass = (*env)->FindClass(env, "java/io/IOException");

    setvbuf(stdout, 0, _IOLBF, 0);
    setvbuf(stderr, 0, _IONBF, 0);

    pipe(pfd);
    dup2(pfd[1], 1);
    dup2(pfd[1], 2);

    const char* logFilePath = (*env)->GetStringUTFChars(env, logPath, NULL);
    latestlog_fd = open(logFilePath, O_WRONLY | O_TRUNC);
    if(latestlog_fd == -1) {
        latestlog_fd = 0;
        (*env)->ThrowNew(env, ioeClass, strerror(errno));
        return;
    }
    (*env)->ReleaseStringUTFChars(env, logPath, logFilePath);

    int result = pthread_create(&logger, 0, logger_thread, NULL);
    if(result != 0) {
        close(latestlog_fd);
        (*env)->ThrowNew(env, ioeClass, strerror(result));
    }
    pthread_detach(logger);
}

typedef void (*exit_func)(int);

// Called when the program exits
static void nominal_exit(int code) {
    JNIEnv *env;
    jint errorCode = (*exitTrap_jvm)->GetEnv(exitTrap_jvm, (void**)&env, JNI_VERSION_1_6);
    if(errorCode == JNI_EDETACHED) {
        errorCode = (*exitTrap_jvm)->AttachCurrentThread(exitTrap_jvm, &env, NULL);
    }
    if(errorCode != JNI_OK) {
        killpg(getpgrp(), SIGTERM);
    }
    if(code != 0) {
        (*env)->CallStaticVoidMethod(env, exitTrap_exitClass, exitTrap_staticMethod, exitTrap_ctx, code);
    }
    (*env)->DeleteGlobalRef(env, exitTrap_ctx);
    (*env)->DeleteGlobalRef(env, exitTrap_exitClass);
    jclass systemClass = (*env)->FindClass(env,"java/lang/System");
    jmethodID exitMethod = (*env)->GetStaticMethodID(env, systemClass, "exit", "(I)V");
    (*env)->CallStaticVoidMethod(env, systemClass, exitMethod, 0);
    while(1) {}
}

// Called when the program exits
static void custom_exit(int code) {
    if(exit_tripped) {
        BYTEHOOK_CALL_PREV(custom_exit, exit_func, code);
        BYTEHOOK_POP_STACK();
        return;
    }
    exit_tripped = true;
    nominal_exit(code);
}

// Called when the program exits
static void custom_atexit() {
    if(exit_tripped) {
        return;
    }
    exit_tripped = true;
    nominal_exit(0);
}

// Sets up the exit trap
JNIEXPORT void JNICALL Java_net_kdt_pojavlaunch_utils_JREUtils_setupExitTrap(JNIEnv *env, __attribute((unused)) jclass clazz, jobject context) {
    exitTrap_ctx = (*env)->NewGlobalRef(env,context);
    (*env)->GetJavaVM(env,&exitTrap_jvm);
    exitTrap_exitClass = (*env)->NewGlobalRef(env,(*env)->FindClass(env,"net/kdt/pojavlaunch/ExitActivity"));
    exitTrap_staticMethod = (*env)->GetStaticMethodID(env,exitTrap_exitClass,"showExitMessage","(Landroid/content/Context;I)V");

    if(bytehook_init(BYTEHOOK_MODE_AUTOMATIC, false) == BYTEHOOK_STATUS_CODE_OK) {
        bytehook_hook_all(NULL,
                          "exit",
                          &custom_exit,
                          NULL,
                          NULL);
    }else {
        atexit(custom_atexit);
    }
}

// Appends to the log
JNIEXPORT void JNICALL Java_net_kdt_pojavlaunch_Logger_appendToLog(JNIEnv *env, __attribute((unused)) jclass clazz, jstring text) {
    jsize appendStringLength = (*env)->GetStringUTFLength(env, text);
    char newChars[appendStringLength+2];
    (*env)->GetStringUTFRegion(env, text, 0, (*env)->GetStringLength(env, text), newChars);
    newChars[appendStringLength] = '\n';
    newChars[appendStringLength+1] = 0;
    if(recordBuffer(newChars, appendStringLength+1) && logListener != NULL) {
        (*env)->CallVoidMethod(env, logListener, logger_onEventLogged, text);
    }
}

// Sets the log listener
JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_Logger_setLogListener(JNIEnv *env, __attribute((unused)) jclass clazz, jobject log_listener) {
    jobject logListenerLocal = logListener;
    if(log_listener == NULL) {
        logListener = NULL;
    }else{
        logListener = (*env)->NewGlobalRef(env, log_listener);
    }
    if(logListenerLocal != NULL) (*env)->DeleteGlobalRef(env, logListenerLocal);
}
