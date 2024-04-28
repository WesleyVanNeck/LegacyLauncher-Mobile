package net.kdt.pojavlaunch.colorselector;

@FunctionalInterface
public interface ColorSelectionListener {
    /**
     * This method gets called by the ColorSelector when the color is selected
     * @param color the selected color
     */
    void onColorSelected(int color);
}

// Usage example:
ColorSelectionListener listener = (color) -> {
    // Do something with the selected color...
    System.out.println("Selected color: #" + Integer.toHexString(color));
};
