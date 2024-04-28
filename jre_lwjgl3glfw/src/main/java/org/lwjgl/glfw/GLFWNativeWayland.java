package org.lwjgl.glfw;

import org.lwjgl.system.NativeType;

/**
 * Provides access to Wayland-specific functions in GLFW.
 */
public final class GLFWNativeWayland {

    /**
     * Returns the Wayland display associated with the current thread, or {@code null} if there is no associated display.
     *
     * @return the Wayland display associated with the current thread, or {@code null} if there is no associated display
     */
    @NativeType("struct wl_display *")
    public static long glfwGetWaylandDisplay() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the Wayland output associated with the given GLFW monitor, or {@code null} if the monitor is not associated with a Wayland output.
     *
     * @param monitor the GLFW monitor to get the associated Wayland output for
     * @return the Wayland output associated with the given GLFW monitor, or {@code null} if the monitor is not associated with a Wayland output
     */
    @NativeType("struct wl_output *")
    public static long glfwGetWaylandMonitor(@NativeType("GLFWmonitor *") long monitor) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the Wayland surface associated with the given GLFW window, or {@code null} if the window is not associated with a Wayland surface.
     *
     * @param window the GLFW window to get the associated Wayland surface for
     * @return the Wayland surface associated with the given GLFW window, or {@code null} if the window is not associated with a Wayland surface
     */
    @NativeType("struct wl_surface *")
    public static long glfwGetWaylandWindow(@NativeType("GLFWwindow *") long window) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
