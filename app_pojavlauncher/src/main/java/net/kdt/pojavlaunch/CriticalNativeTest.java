package net.kdt.pojavlaunch;

import android.annotation.SuppressLint;

public class CriticalNativeTest {

    /**
     * A native method that will be implemented in the native code.
     * This method is marked as critical, indicating that it should be optimized for performance.
     *
     * @param arg0 The first argument.
     * @param arg1 The second argument.
     */
    @SuppressLint("NewApi")
    @CriticalNative
    public static native void testCriticalNative(int arg0, int arg1);

    /**
     * Invokes the native method.
     */
    public static void invokeTest() {
        testCriticalNative(0, 0);
    }

    /**
     * A sample main method to demonstrate how to use this class.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        invokeTest();
    }
}
