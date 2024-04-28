package net.kdt.pojavlaunch.customcontrols;

import androidx.annotation.Keep;

import java.util.ArrayList;

/**
 * Data class representing the properties of a control drawer.
 */
@Keep
public class ControlDrawerData {

    /**
     * List of button properties.
     */
    public final ArrayList<ControlData> buttonProperties;

    /**
     * Properties of the control drawer.
     */
    public final ControlData properties;

    /**
     * Orientation of the control drawer.
     */
    public Orientation orientation;

    /**
     * Enum representing the possible orientations of the control drawer.
     */
    @Keep
    public enum Orientation {
        DOWN,
        LEFT,
        UP,
        RIGHT,
        FREE
    }

    /**
     * Gets an array of all possible orientations.
     *
     * @return an array of all possible orientations
     */
    public static Orientation[] getOrientations() {
        return new Orientation[]{Orientation.DOWN, Orientation.LEFT, Orientation.UP, Orientation.RIGHT, Orientation.FREE};
    }

    /**
     * Converts an orientation to an integer.
     *
     * @param orientation the orientation to convert
     * @return the integer representation of the orientation
     */
    public static int orientationToInt(Orientation orientation) {
        switch (orientation) {
            case DOWN:
                return 0;
            case LEFT:
                return 1;
            case UP:
                return 2;
            case RIGHT:
                return 3;
            case FREE:
                return 4;
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    /**
     * Converts an integer to an orientation.
     *
     * @param by the integer to convert
     * @return the orientation representation of the integer
     */
    public static Orientation intToOrientation(int by) {
        switch (by) {
            case 0:
                return Orientation.DOWN;
            case 1:
                return Orientation.LEFT;
            case 2:
                return Orientation.UP;
            case 3:
                return Orientation.RIGHT;
            case 4:
                return Orientation.FREE;
            default:
                throw new IllegalArgumentException("Invalid integer: " + by);
        }
    }

    /**
     * Constructor for ControlDrawerData.
     *
     * @param buttonProperties list of button properties
     * @param properties       properties of the control drawer
     * @param orientation      orientation of the control drawer
     */
    public ControlDrawerData(ArrayList<ControlData> buttonProperties, ControlData properties, Orientation orientation) {
        this.buttonProperties = buttonProperties != null ? buttonProperties : new ArrayList<>();
        this.properties = properties != null ? properties : new ControlData("Drawer", new int[0], 0, 0);
        this.orientation = orientation != null ? orientation : Orientation.LEFT;
    }

    /**
     * Copy constructor for ControlDrawerData.
     *
     * @param drawerData the ControlDrawerData object to copy
     */
    public ControlDrawerData(ControlDrawerData drawerData) {
        this(drawerData.buttonProperties, drawerData.properties, drawerData.orientation);
    }
}
