package net.kdt.pojavlaunch;

/**
 * AWTInputBridge provides a way to send various types of input events to the AWT event queue.
 */
public class AWTInputBridge {
    // Constants for different types of events
    public static final int EVENT_TYPE_CHAR = 1000;
    public static final int EVENT_TYPE_CURSOR_POS = 1003;
    public static final int EVENT_TYPE_KEY = 1005;
    public static final int EVENT_TYPE_MOUSE_BUTTON = 1006;

    /**
     * Sends a key event to the AWT event queue.
     * @param keychar The character code of the key.
     * @param keycode The key code of the key.
     * @param state The state of the key (e.g. pressed or released).
     */
    public static void sendKey(char keychar, int keycode, int state) {
        // TODO: Android -> AWT keycode mapping
        nativeSendData(EVENT_TYPE_KEY, keychar, keycode, state, 0);
    }

    /**
     * Sends a character event to the AWT event queue.
     * @param keychar The character code of the character.
     */
    public static void sendChar(char keychar){
        nativeSendData(EVENT_TYPE_CHAR, keychar, 0, 0, 0);
    }

    /**
     * Sends a mouse button event to the AWT event queue.
     * @param awtButtons The AWT button constants (e.g. InputEvent.BUTTON1_MASK).
     * @param isDown Whether the button is pressed or released.
     */
    public static void sendMousePress(int awtButtons, boolean isDown) {
        int buttonState = isDown ? 1 : 0;
        nativeSendData(EVENT_TYPE_MOUSE_BUTTON, awtButtons, buttonState, 0, 0);
    }

    /**
     * Sends a mouse position event to the AWT event queue.
     * @param x The x-coordinate of the mouse position.
     * @param y The y-coordinate of the mouse position.
     */
    public static void sendMousePos(int x, int y) {
        nativeSendData(EVENT_TYPE_CURSOR_POS, x, y, 0, 0);
    }

    static {
        // Load the native library
        System.loadLibrary("pojavexec_awt");
    }

    // Native methods
    public static native void nativeSendData(int type, int i1, int i2, int i3, int i4);
    public static native void nativeClipboardReceived(String data, String mimeTypeSub);
    public static native void nativeMoveWindow(int xoff, int yoff);
}
