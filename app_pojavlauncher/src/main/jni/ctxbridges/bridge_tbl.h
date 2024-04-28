//
// Created by [your name] on [date].
//

#ifndef POJAVLAUNCHER_BRIDGE_TBL_H
#define POJAVLAUNCHER_BRIDGE_TBL_H

#include <ctxbridges/common.h>
#include <ctxbridges/gl_bridge.h>
#include <ctxbridges/osm_bridge.h>

// Forward declaration of the render window type.
typedef struct basic_render_window_t basic_render_window_t;

// Enum for the bridge types.
typedef enum {
    BRIDGE_TYPE_OSM,
    BRIDGE_TYPE_GL,
} bridge_type_t;

// Struct for the bridge function pointers.
typedef struct bridge_funcs_t {
    bool (*init)();
    br_init_context_t init_context;
    br_make_current_t make_current;
    br_get_current_t get_current;
    void (*swap_buffers)();
    void (*setup_window)();
    void (*swap_interval)(int swapInterval);
} bridge_funcs_t;

// Static function to initialize the function pointers based on the bridge type.
static void init_bridge_funcs(bridge_type_t bridge_type) {
    if (bridge_type == BRIDGE_TYPE_OSM) {
        br_init = osm_init;
        br_init_context = (br_init_context_t) osm_init_context;
        br_make_current = (br_make_current_t) osm_make_current;
        br_get_current = (br_get_current_t) osm_get_current;
        br_swap_buffers = osm_swap_buffers;
        br_setup_window = osm_setup_window;
        br_swap_interval = osm_swap_interval;
    } else if (bridge_type == BRIDGE_TYPE_GL) {
        br_init = gl_init;
        br_init_context = (br_init_context_t) gl_init_context;
        br_make_current = (br_make_current_t) gl_make_current;
        br_get_current = (br_get_current_t) gl_get_current;
        br_swap_buffers = gl_swap_buffers;
        br_setup_window = gl_setup_window;
        br_swap_interval = gl_swap_interval;
    }
}

// Function to set the OpenSMILE bridge function pointers.
void set_osm_bridge_tbl() {
    init_bridge_funcs(BRIDGE_TYPE_OSM);
}

// Function to set the OpenGL bridge function pointers.
void set_gl_bridge_tbl() {
    init_bridge_funcs(BRIDGE_TYPE_GL);
}

#endif //POJAVLAUNCHER_BRIDGE_TBL_H
