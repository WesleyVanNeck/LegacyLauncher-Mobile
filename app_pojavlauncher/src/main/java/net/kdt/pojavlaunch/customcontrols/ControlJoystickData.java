package net.kdt.pojavlaunch.customcontrols;

/**
 * ControlJoystickData class representing joystick data for custom controls.
 */
public class ControlJoystickData extends ControlData {

    // Whether the joystick can stay forward
    private boolean forwardLock;
    // Whether the finger tracking is absolute or relative
    private boolean absolute;

    /**
     * Constructs a new ControlJoystickData instance with default values.
     */
    public ControlJoystickData() {
        this(false, false);
    }

    /**
     * Constructs a new ControlJoystickData instance with the given properties.
     *
     * @param forwardLock  Whether the joystick can stay forward
     * @param isAbsolute   Whether the finger tracking is absolute
     */
    public ControlJoystickData(boolean forwardLock, boolean isAbsolute) {
        super();
        this.forwardLock = forwardLock;
        this.absolute = isAbsolute;
    }

    /**

