package org.lwjgl.glfw;

import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeType;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public interface GLFWNative {
    @NativeType("char const *")
    String glfwGetWin32Adapter(long monitor);

    @NativeType("char const *")
    String glfwGetWin32Monitor(long monitor);

    long glfwGetWin32Window(long window);

    long glfwAttachWin32Window(long handle, long share);
}

class GLFWNativeWin32 implements GLFWNative {
    @Override
    public String glfwGetWin32Adapter(long monitor) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String glfwGetWin32Monitor(long monitor) {
        throw new UnsupportedOperationException
