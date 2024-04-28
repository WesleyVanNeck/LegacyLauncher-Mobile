package net.kdt.pojavlaunch.colorselector;

import java.util.function.BiConsumer;

public interface RectangleSelectionListener {
    default void onLuminosityIntensityChanged(float luminosity, float intensity) {
        // Provide a default implementation here, if needed
    }

    void setOnLuminosityIntensityChanged(BiConsumer<Float, Float> listener);
}


RectangleSelectionListener listener = ...;
listener.setOnLuminosityIntensityChanged((luminosity, intensity) -> {
    // Do something when the luminosity and intensity values change
});


RectangleSelectionListener listener = new RectangleSelectionListener() {
    @Override
    public void onLuminosityIntensityChanged(float luminosity, float intensity) {
        // Provide custom behavior here
    }
};
