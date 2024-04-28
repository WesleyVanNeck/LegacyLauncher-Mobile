package org.lwjgl.glfw;

import org.lwjgl.system.APIUtil;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Pointer;

import java.nio.ByteBuffer;

public class GLFWNativeEGL {
    @NativeType("EGLDisplay")
    public static long glfwGetEGLDisplay() {
        return APIUtil.pokeCallable(EGLDisplay::eglGetDisplay, MemoryUtil.NULL_POINTER);
    }

    @NativeType("EGLContext")
    public static long glfwGetEGLContext(@NativeType("GLFWwindow *") long window) {
        return APIUtil.pokeCallable(EGLContext::eglGetCurrentContext, MemoryUtil.NULL_POINTER);
    }

    @NativeType("EGLSurface")
    public static long glfwGetEGLSurface(@NativeType("GLFWwindow *") long window) {
        return APIUtil.pokeCallable(EGLSurface::eglGetCurrentSurface, MemoryUtil.NULL_POINTER, GLFW.GLFW_DRAW_BUFFER);
    }

    @NativeType("EGLConfig")
    public static long glfwGetEGLConfig(@NativeType("GLFWwindow *") long window) {
        Pointer<ByteBuffer> config = Pointer.allocateBytes(4 * Pointer.SIZE);
        if (!APIUtil.pokeCallable(EGLConfig::eglGetConfig, config, MemoryUtil.NULL_POINTER, MemoryUtil.NULL_POINTER, 1)) {
            throw new IllegalStateException("Failed to get EGL config");
        }
        return config.getPointer(0);
    }

    private interface EGLDisplay {
        boolean eglGetDisplay(long display);
    }

    private interface EGLContext {
        long eglGetCurrentContext();
    }

    private interface EGLSurface {
        long eglGetCurrentSurface(int readeble);
    }

    private interface EGLConfig {
        boolean eglGetConfig(long dpy, long config, long attrib_list, int max_attribs);
    }
}
