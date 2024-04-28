package net.kdt.pojavlaunch.utils;

/**
 * Utilities class for mathematical operations.
 */
public class MathUtils {

    // Ported from https://www.arduino.cc/reference/en/language/functions/math/map/
    /**
     * Maps a number from one range to another.
     *
     * @param x               the number to map
     * @param in_min          the lower bound of the input range
     * @param in_max          the upper bound of the input range
     * @param out_min         the lower bound of the output range
     * @param out_max         the upper bound of the output range
     * @return the mapped number
     * @throws IllegalArgumentException if the input range is not valid
     */
    public static float map(float x, float in_min, float in_max, float out_min, float out_max) {
        if (in_min >= in_max) {
            throw new IllegalArgumentException("Invalid input range: in_min must be less than in_max");
        }
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * Returns the distance between two points.
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @return the distance between the two points
     */
    public static float dist(float x1, float y1, float x2, float y2) {
        final float x = (x2 - x1);
        final float y = (y2 - y1);
        return (float) Math.hypot(x, y);
    }

}
