package net.kdt.pojavlaunch.modloaders.modpacks.models;

import org.jetbrains.annotations.Nullable;

/**
 * Search filters class, containing properties for filters to be passed to APIs.
 */
public class SearchFilters {

    // isModpack property
    private boolean isModpack;

    // name property
    private String name;

    // mcVersion property
    @Nullable
    private String mcVersion;

    /**
     * Constructs a SearchFilters object with the given parameters.
     *
     * @param isModpack whether the search is for a modpack
     * @param name the name of the mod or modpack
     * @param mcVersion the Minecraft version of the mod or modpack
     */
    public SearchFilters(boolean isModpack, String name, @Nullable String mcVersion) {
        this.isModpack = isModpack;
        this.name = name;
        this.mcVersion = mcVersion;
    }

    /**
     * Gets the value of the isModpack property.
     *
     * @return whether the search is for a modpack
     */
    public boolean isModpack() {
        return isModpack;

