package net.kdt.pojavlaunch.modloaders.modpacks.models;

public abstract class ModSource {
    // Add visibility modifier
    private int apiSource;
    private boolean isModpack;

    // Add constructor
    public ModSource(int apiSource, boolean isModpack) {
        this.apiSource = apiSource;
        this.isModpack = isModpack;
    }

    // Add getter/setter methods
    public int getApiSource() {
        return apiSource;
    }

    public void setApiSource(int apiSource) {
        this.apiSource = apiSource;
    }

    public boolean isModpack() {
        return isModpack;
    }

    public void setModpack(boolean isModpack) {
        this.isModpack = isModpack;
    }
}
