package net.kdt.pojavlaunch.customcontrols.buttons;

import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NONE;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH_WEST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH_WEST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_WEST;

import android.annotation.SuppressLint;
import android.view.View;

import net.kdt.pojavlaunch.LwjglGlfwKeycode;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.ControlJoystickData;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick;
import net.kdt.pojavlaunch.customcontrols.handleview.EditControlPopup;

import org.lwjgl.glfw.CallbackBridge;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import java.util.Arrays;

@SuppressLint("ViewConstructor")
public class ControlJoystick extends JoystickView implements ControlInterface {

    /**
     * The keycodes for the directions
     */
    private final int[] directionForwardLock;
    private final int[] directionForward;
    private final int[] directionRight;
    private final int[] directionBackward;
    private final int[] directionLeft;

    private ControlJoystickData controlData;
    private int lastDirectionInt = DIRECTION_NONE;
    private int currentDirectionInt = DIRECTION_NONE;

    /**
     * Constructor for the ControlJoystick class
     *
     * @param parent the parent ControlLayout
     * @param data   the ControlJoystickData
     */
    public ControlJoystick(ControlLayout parent, ControlJoystickData data) {
        super(parent.getContext());
        init(data, parent);
    }

    /**
     * Sends input for the given keys
     *
     * @param keys   the keys to send input for
     * @param isDown true if the keys are being pressed, false otherwise
     */
    private static void sendInput(int[] keys, boolean isDown) {
        for (int key : keys) {
            CallbackBridge.sendKeyPress(key, CallbackBridge.getCurrentMods(), isDown);
        }
    }

    /**
     * Initializes the ControlJoystick
     *
     * @param data   the ControlJoystickData
     * @param layout the ControlLayout
     */
    private void init(ControlJoystickData data, ControlLayout layout) {
        this.controlData = data;
        setProperties(preProcessProperties(data, layout));
        setDeadzone(35);
        setFixedCenter(data.absolute);
        setAutoReCenterButton(true);

        injectBehaviors();

        setOnMoveListener(new OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                lastDirectionInt = currentDirectionInt;
                currentDirectionInt = getDirectionInt(angle, strength);

                if (lastDirectionInt != currentDirectionInt) {
                    sendDirectionalKeycode(lastDirectionInt, false);
                    sendDirectionalKeycode(currentDirectionInt, true);
                }
            }

            @Override
            public void onForwardLock(boolean isLocked) {
                sendInput(directionForwardLock, isLocked);
            }
        });
    }

    @Override
    public View getControlView() {
        return this;
    }

    @Override
    public ControlData getProperties() {
        return controlData;
    }

    @Override
    public void setProperties(ControlData properties, boolean changePos) {
        if (properties instanceof ControlJoystickData) {
            controlData = (ControlJoystickData) properties;
            controlData.isHideable = true;
            ControlInterface.super.setProperties(properties, changePos);
            postDelayed(() -> {
                setForwardLockDistance(controlData.forwardLock ? (int) Tools.dpToPx(60) : 0);
                setFixedCenter(controlData.absolute);
            }, 10);
        }
    }

    @Override
    public void removeButton() {
        ControlLayout parent = getControlLayoutParent();
        if (parent != null) {
            parent.getLayout().joystickDataList.remove(getProperties());
            parent.removeView(this);
        }
    }

    @Override
    public void cloneButton() {
        ControlLayout parent = getControlLayoutParent();
        if (parent != null) {
            ControlJoystickData data = new ControlJoystickData(controlData);
            parent.addJoystickButton(data);
        }
    }

    @Override
    public void setBackground() {
        setBorderWidth((int) Tools.dpToPx(getProperties().strokeWidth * (getControlLayoutParent().getLayoutScale() / 100f)));
        setBorderColor(getProperties().strokeColor);
        setBackgroundColor(getProperties().bgColor);
    }

    @Override
    public void sendKeyPresses(boolean isDown) {
        // STUB since non swipeable
    }

    @Override
    public void loadEditValues(EditControlPopup editControlPopup) {
        editControlPopup.loadJoystickValues(controlData);
    }

    /**
     * Gets the direction int from the given angle and intensity
     *
     * @param angle   the angle
     * @param intensity the intensity
     * @return the direction int
     */
    private int getDirectionInt(int angle, int intensity) {
        if (intensity == 0) return DIRECTION_NONE;
        int directionInt = (int) (((angle + 22.5) / 45) % 8);
        if (directionInt < 0) directionInt += 8;
        return directionInt;
    }

    /**
     * Sends directional keycode
     *
     * @param direction the direction
     * @param isDown    true if the keys are being pressed, false otherwise
     */
    private void sendDirectionalKeycode(int direction, boolean isDown) {
        switch (direction) {
            case DIRECTION_NORTH:
                sendInput(directionForward, isDown);
                break;
            case DIRECTION_NORTH_EAST:
                sendInput(directionForward, isDown);
                sendInput(directionRight, isDown);
                break;
            case DIRECTION_EAST:
                sendInput(directionRight, isDown);
                break;
            case DIRECTION_SOUTH_EAST:
                sendInput(directionRight, isDown);
                sendInput(directionBackward, isDown);
                break;
            case DIRECTION_SOUTH:
                sendInput(directionBackward, isDown);
                break;
            case DIRECTION_SOUTH_WEST:
                sendInput(directionBackward, isDown);
                sendInput(directionLeft, isDown);
                break;
            case DIRECTION_WEST:
                sendInput(directionLeft, isDown);
                break;
            case DIRECTION_NORTH_WEST:
                sendInput(directionForward, isDown);
                sendInput(directionLeft, isDown);
                break;
            case DIRECTION_FORWARD_LOCK:
                sendInput(directionForwardLock, isDown);
                break;
        }
    }
}
