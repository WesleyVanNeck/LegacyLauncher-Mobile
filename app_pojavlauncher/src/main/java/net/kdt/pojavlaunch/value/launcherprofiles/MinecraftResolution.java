package net.kdt.pojavlaunch.value.launcherprofiles;

import androidx.annotation.Keep;

@Keep
public class MinecraftResolution {
    private int width;
    private int height;

    public MinecraftResolution() {}

    public MinecraftResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
