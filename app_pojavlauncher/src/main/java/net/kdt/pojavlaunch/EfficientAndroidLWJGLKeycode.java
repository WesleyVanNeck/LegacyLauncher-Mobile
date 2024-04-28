package net.kdt.pojavlaunch;

import static org.lwjgl.glfw.CallbackBridge.sendKeyPress;

import android.view.KeyEvent;

import java.util.Arrays;

public class EfficientAndroidLWJGLKeycode {

    private static final int KEYCODE_COUNT = 106;
    private static final KeycodeMapping keycodeMapping = getKeycodeMapping();
    private static int mTmpCount = 0;

    public static boolean containsIndex(int index){
        return index >= 0 && index < KEYCODE_COUNT;
    }

    public static void execKey(KeyEvent keyEvent, int valueIndex) {
        if (keyEvent == null || !containsIndex(valueIndex)) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        boolean isAltPressed = keyEvent.isAltPressed();
        boolean isCapsLockOn = keyEvent.isCapsLockOn();
        boolean isCtrlPressed = keyEvent.isCtrlPressed();
        boolean isNumLockOn = keyEvent.isNumLockOn();
        boolean isShiftPressed = keyEvent.isShiftPressed();

        char key = (char) (keyEvent.getUnicodeChar() != 0 ? keyEvent.getUnicodeChar() : '\u0000');
        sendKeyPress(
                keycodeMapping.getLwjglKeycodeByIndex(valueIndex),
                key,
                0,
                getCurrentMods(isAltPressed, isCapsLockOn, isCtrlPressed, isNumLockOn, isShiftPressed),
                keyEvent.getAction() == KeyEvent.ACTION_DOWN);
    }

    public static void execKeyIndex(int index){
        if (!containsIndex(index)) {
            throw new IllegalArgumentException("Invalid index");
        }

        sendKeyPress(keycodeMapping.getLwjglKeycodeByIndex(index));
    }

    public static int getLwjglKeycodeByIndex(int index) {
        if (!containsIndex(index)) {
            throw new IllegalArgumentException("Invalid index");
        }

        return keycodeMapping.getLwjglKeycodeByIndex(index);
    }

    public static int getIndexByKey(int key){
        return Arrays.binarySearch(keycodeMapping.getAndroidKeycodes(), key);
    }

    private static int[] getCurrentMods(boolean isAltPressed, boolean isCapsLockOn, boolean isCtrlPressed, boolean isNumLockOn, boolean isShiftPressed) {
        int mods = 0;
        if (isAltPressed) mods |= LwjglGlfwKeycode.GLFW_MOD_ALT;
        if (isCapsLockOn) mods |= LwjglGlfwKeycode.GLFW_MOD_CAPS_LOCK;
        if (isCtrlPressed) mods |= LwjglGlfwKeycode.GLFW_MOD_CONTROL;
        if (isNumLockOn) mods |= LwjglGlfwKeycode.GLFW_MOD_NUM_LOCK;
        if (isShiftPressed) mods |= LwjglGlfwKeycode.GLFW_MOD_SHIFT;
        return mods;
    }

    private static KeycodeMapping getKeycodeMapping() {
        int[] androidKeycodes = new int[KEYCODE_COUNT];
        short[] lwjglKeycodes = new short[KEYCODE_COUNT];

        int index = 0;
        addKeycodeMapping(androidKeycodes, lwjglKeycodes, KeyEvent.KEYCODE_UNKNOWN, LwjglGlfwKeycode.GLFW_KEY_UNKNOWN, index);
        addKeycodeMapping(androidKeycodes, lwjglKeycodes, KeyEvent.KEYCODE_HOME, LwjglGlfwKeycode.GLFW_KEY_HOME, index);
        // Escape key
        addKeycodeMapping(androidKeycodes, lwjglKeycodes, KeyEvent.KEYCODE_BACK, LwjglGlfwKeycode.GLFW_KEY_ESCAPE, index);

        // 0-9 keys
        for (int i = 0; i < 10; i++) {
            addKeycodeMapping(androidKeycodes, lwjglKeycodes, KeyEvent.KEYCODE_0 + i, LwjglGlfwKeycode.GLFW_KEY_0 + i, index);
        }

        // ... (add the rest of the keycode mappings here)

        return new KeycodeMapping(androidKeycodes, lwjglKeycodes);
    }

    private static void addKeycodeMapping(int[] androidKeycodes, short[] lwjglKeycodes, int androidKeycode, short lwjglKeycode, int index) {
        androidKeycodes[index] = androidKeycode;
        lwjglKeycodes[index] = lwjglKeycode;
        index++;
    }

    private static class KeycodeMapping {
        private final int[] androidKeycodes;
        private final short[] lwjglKeycodes;

        public KeycodeMapping(int[] androidKeycodes, short[] lwjglKeycodes) {
            this.androidKeycodes = androidKeycodes;
            this.lwjglKeycodes = lwjglKeycodes;
        }

        public int[] getAndroidKeycodes() {
            return androidKeycodes;
        }

        public short[] getLwjglKeycodes() {
            return lwjglKeycodes;
        }

        public short getLwjglKeycodeByIndex(int index) {
            return lwjglKeycodes[index];
        }
    }
}
