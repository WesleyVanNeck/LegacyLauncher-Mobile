//
// Created by Your Name on Your Date.
//
#ifndef GL_BRIDGE_H
#define GL_BRIDGE_H

#include <EGL/egl.h>
#include <stdbool.h>

typedef struct {
    char state;
    struct ANativeWindow *nativeSurface;
    struct ANativeWindow *newNativeSurface;
    EGLConfig config;
    EGLint format;
    EGLContext context;
    EGLSurface surface;
} gl_render_window_t;

// Initializes the GL context
bool gl_init(void);

// Gets the current GL rendering window
gl_render_window_t *gl_get_current(void);

// Initializes the GL context with sharing
gl_render_window_t *gl_init_context(gl_render_window_t *share);

// Makes the specified GL rendering window current
void gl_make_current(gl_render_window_t *bundle);

// Swaps the buffers of the current GL rendering window
void gl_swap_buffers(void);

// Sets up the GL rendering window
void gl_setup_window(void);

// Sets the swap interval for the current GL rendering window
void gl_swap_interval(int swapInterval);

#endif // GL_BRIDGE_H
