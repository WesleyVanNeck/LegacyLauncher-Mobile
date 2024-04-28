package net.kdt.pojavlaunch.customcontrols.mouse;

/**
 * Interface for an abstract touchpad.
 */
public interface AbstractTouchpad {

    /**
     * Gets the current display state of the touchpad.
     *
     * @return true if the touchpad is currently displayed, false otherwise
     */
    boolean getDisplayState();

    /**
     * Applies a motion vector to the touchpad.
     *
     * @param vector the motion vector to apply
     */
    void applyMotionVector(float[] vector);

    /**
     * Sets the display state of the touchpad.
     *
     * @param displayState the desired display state
     */
    void setDisplayState(boolean displayState);

    /**
     * Calculates the distance between two points.
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @return the distance between the two points
     */
    default double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Scales a motion vector by a given factor.
     *
     * @param vector the motion vector to scale
     * @param factor the scaling factor
     */
    default void scaleMotionVector(float[] vector, float factor) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] *= factor;
        }
    }
}
