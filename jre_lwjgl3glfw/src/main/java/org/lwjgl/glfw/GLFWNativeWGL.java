package org.lwjgl.glfw;

import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Pointer;

/** LWJGL GLFW WGL (Windows) native bindings */
public class GLFWNativeWGL {

    /**
     * Gets the WGL context associated with the given GLFW window.
     *
     * @param window the GLFW window
     * @return the WGL context, or 0 if an error occurred
     */
    @NativeType("HGLRC")
    public static long glfwGetWGLContext(@NativeType("GLFWwindow *") long window) {
        // Load the WGL function from the GLFW library
        long glfw = GLFW.glfwGetWin32WindowHandle(window);
        if (glfw == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to get the Win32 window handle");
        }
        long wglGetCurrentContext = GLFW.glfwGetWGLFunction("wglGetCurrentContext");
        if (wglGetCurrentContext == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to load the wglGetCurrentContext function");
        }

        // Get the current WGL context
        long currentContext = JNI.getStaticLongField(GLFW.getClass(), wglGetCurrentContext);

        // Get the WGL device associated with the GLFW window
        long wglGetCurrentDC = GLFW.glfwGetWGLFunction("wglGetCurrentDC");
        if (wglGetCurrentDC == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to load the wglGetCurrentDC function");
        }
        long deviceContext = JNI.getStaticLongField(GLFW.getClass(), wglGetCurrentDC);
        Pointer devicePointer = Pointer.pointerToAddress(deviceContext);
        long hdc = devicePointer.get(long.class);

        // Get the WGL pixel format associated with the GLFW window
        long wglChoosePixelFormat = GLFW.glfwGetWGLFunction("wglChoosePixelFormat");
        if (wglChoosePixelFormat == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to load the wglChoosePixelFormat function");
        }
        int[] pixelFormatAttribs = {
            // Match the pixel format to the current context
            WGL.WGL_CONTEXT_MAJOR_VERSION_ARB, GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_CONTEXT_VERSION_MAJOR),
            WGL.WGL_CONTEXT_MINOR_VERSION_ARB, GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_CONTEXT_VERSION_MINOR),
            WGL.WGL_CONTEXT_PROFILE_MASK_ARB, WGL.WGL_CONTEXT_CORE_PROFILE_BIT_ARB,
            0
        };
        int[] pixelFormat = new int[1];
        int numFormats = WGL.wglChoosePixelFormat(hdc, pixelFormatAttribs, MemoryUtil.NULL, 1, pixelFormat, 0);
        if (numFormats == 0) {
            throw new IllegalStateException("Failed to choose a pixel format");
        }

        // Create a new WGL context
        long wglCreateContext = GLFW.glfwGetWGLFunction("wglCreateContext");
        if (wglCreateContext == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to load the wglCreateContext function");
        }
        long newContext = WGL.wglCreateContext(hdc);
        if (newContext == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to create a WGL context");
        }

        // Share resources between the current and new contexts
        long wglShareLists = GLFW.glfwGetWGLFunction("wglShareLists");
        if (wglShareLists == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to load the wglShareLists function");
        }
        if (!WGL.wglShareLists(currentContext, newContext)) {
            throw new IllegalStateException("Failed to share resources between the contexts");
        }

        // Make the new context current
        long wglMakeCurrent = GLFW.glfwGetWGLFunction("wglMakeCurrent");
        if (wglMakeCurrent == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to load the wglMakeCurrent function");
        }
        if (!WGL.wglMakeCurrent(hdc, newContext)) {
            throw new IllegalStateException("Failed to make the new context current");
        }

        return newContext;
    }
}
