package net.kdt.pojavlaunch.modloaders.modpacks.models;

public final class Constants {
    private Constants() {}

    // Types of modpack APIs
    public static final int MODRINTH = 0x0;
    public static final int CURSEFORGE = 0x1;
    public static final int TECHNIC = 0x2;

    // Modrinth API, file environments
    public static final String MODRINTH_FILE_ENV_REQUIRED = "required";
    public static final String MODRINTH_FILE_ENV_OPTIONAL = "optional";
    public static final String MODRINTH_FILE_ENV_UNSUPPORTED = "unsupported";
}
