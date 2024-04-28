package net.kdt.pojavlaunch.modloaders.modpacks.models;

import androidx.annotation.NonNull;

public class ModItem implements ModSource {

    private static final String CACHE_TAG_DELIMITER = "_";

    @NonNull
    private final int apiSource;
    @NonNull
    private final boolean isModpack;
    @NonNull
    private final String id;
    @NonNull
    private final String title;
    @NonNull

