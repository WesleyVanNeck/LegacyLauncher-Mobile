//
// Created by maks on 21.09.2022.
//
#ifndef POJAVLAUNCHER_EGL_LOADER_H
#define POJAVLAUNCHER_EGL_LOADER_H

#include <EGL/egl.h>
#include <dlfcn.h>

extern EGLBoolean (*eglMakeCurrent_p) (EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx);
extern EGLBoolean (*eglDestroyContext_p) (EGLDisplay dpy, EGLContext ctx);
extern EGLBoolean (*eglDestroySurface_p) (EGLDisplay dpy, EGLSurface surface);
extern EGLBoolean (*eglTerminate_p) (EGLDisplay dpy);
extern EGLBoolean (*eglReleaseThread_p) (void);
extern EGLContext (*eglGetCurrentContext_p) (void);
extern EGLDisplay (*eglGetDisplay_p) (NativeDisplayType display);
extern EGLBoolean (*eglInitialize_p) (EGLDisplay dpy, EGLint *major, EGLint *minor);
extern EGLBoolean (*eglChooseConfig_p) (EGLDisplay dpy, const EGLint *attrib_list, EGLConfig *configs, EGLint config_size, EGLint *num_config);
extern EGLBoolean (*eglGetConfigAttrib_p) (EGLDisplay dpy, EGLConfig config, EGLint attribute, EGLint *value);
extern EGLBoolean (*eglBindAPI_p) (EGLenum api);
extern EGLSurface (*eglCreatePbufferSurface_p) (EGLDisplay dpy, EGLConfig config, const EGLint *attrib_list);
extern EGLSurface (*eglCreateWindowSurface_p) (EGLDisplay dpy, EGLConfig config, NativeWindowType window, const EGLint *attrib_list);
extern EGLBoolean (*eglSwapBuffers_p) (EGLDisplay dpy, EGLSurface draw);
extern EGLint (*eglGetError_p) (void);
extern EGLContext (*eglCreateContext_p) (EGLDisplay dpy, EGLConfig config, EGLContext share_list, const EGLint *attrib_list);
extern EGLBoolean (*eglSwapInterval_p) (EGLDisplay dpy, EGLint interval);
extern EGLSurface (*eglGetCurrentSurface_p) (EGLint readdraw);

void dlsym_EGL() {
    void *handle = dlopen("libEGL.so", RTLD_LAZY);
    if (!handle) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglMakeCurrent_p = (EGLBoolean (*) (EGLDisplay, EGLSurface, EGLSurface, EGLContext)) dlsym(handle, "eglMakeCurrent");
    if (!eglMakeCurrent_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglDestroyContext_p = (EGLBoolean (*) (EGLDisplay, EGLContext)) dlsym(handle, "eglDestroyContext");
    if (!eglDestroyContext_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglDestroySurface_p = (EGLBoolean (*) (EGLDisplay, EGLSurface)) dlsym(handle, "eglDestroySurface");
    if (!eglDestroySurface_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglTerminate_p = (EGLBoolean (*) (EGLDisplay)) dlsym(handle, "eglTerminate");
    if (!eglTerminate_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglReleaseThread_p = (EGLBoolean (*) (void)) dlsym(handle, "eglReleaseThread");
    if (!eglReleaseThread_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglGetCurrentContext_p = (EGLContext (*) (void)) dlsym(handle, "eglGetCurrentContext");
    if (!eglGetCurrentContext_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglGetDisplay_p = (EGLDisplay (*) (NativeDisplayType)) dlsym(handle, "eglGetDisplay");
    if (!eglGetDisplay_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglInitialize_p = (EGLBoolean (*) (EGLDisplay, EGLint *, EGLint *)) dlsym(handle, "eglInitialize");
    if (!eglInitialize_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglChooseConfig_p = (EGLBoolean (*) (EGLDisplay, const EGLint *, EGLConfig *, EGLint, EGLint *)) dlsym(handle, "eglChooseConfig");
    if (!eglChooseConfig_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglGetConfigAttrib_p = (EGLBoolean (*) (EGLDisplay, EGLConfig, EGLint, EGLint *)) dlsym(handle, "eglGetConfigAttrib");
    if (!eglGetConfigAttrib_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglBindAPI_p = (EGLBoolean (*) (EGLenum)) dlsym(handle, "eglBindAPI");
    if (!eglBindAPI_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglCreatePbufferSurface_p = (EGLSurface (*) (EGLDisplay, EGLConfig, const EGLint *)) dlsym(handle, "eglCreatePbufferSurface");
    if (!eglCreatePbufferSurface_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglCreateWindowSurface_p = (EGLSurface (*) (EGLDisplay, EGLConfig, NativeWindowType, const EGLint *)) dlsym(handle, "eglCreateWindowSurface");
    if (!eglCreateWindowSurface_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglSwapBuffers_p = (EGLBoolean (*) (EGLDisplay, EGLSurface)) dlsym(handle, "eglSwapBuffers");
    if (!eglSwapBuffers_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglGetError_p = (EGLint (*) (void)) dlsym(handle, "eglGetError");
    if (!eglGetError_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglCreateContext_p = (EGLContext (*) (EGLDisplay, EGLConfig, EGLContext, const EGLint *)) dlsym(handle, "eglCreateContext");
    if (!eglCreateContext_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglSwapInterval_p = (EGLBoolean (*) (EGLDisplay, EGLint)) dlsym(handle, "eglSwapInterval");
    if (!eglSwapInterval_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    eglGetCurrentSurface_p = (EGLSurface (*) (EGLint)) dlsym(handle, "eglGetCurrentSurface");
    if (!eglGetCurrentSurface_p) {
        fprintf(stderr, "Error: %s\n", dlerror());
        exit(EXIT_FAILURE);
    }

    dlclose(handle);
}

#endif //POJAVLAUNCHER_EGL_LOADER_H
