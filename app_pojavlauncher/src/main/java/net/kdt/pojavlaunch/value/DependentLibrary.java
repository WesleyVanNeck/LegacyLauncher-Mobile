package net.kdt.pojavlaunch.value;

import androidx.annotation.Keep;
import net.kdt.pojavlaunch.JMinecraftVersionList.Arguments.ArgValue.ArgRules;

@Keep
public class DependentLibrary {
    /**
     * ArgRules array
     */
    @Keep
    public ArgRules[] rules;

    /**
     * Name of the library
     */
    @Keep
    public String name;

    /**
     * Library downloads
     */
    @Keep
    public LibraryDownloads downloads;

    /**
     * URL of the library
     */
    @Keep
    public String url;

    @Keep
    public static class LibraryDownloads {
        /**
         * Minecraft library artifact
         */
        public final MinecraftLibraryArtifact artifact;

        /**
         * Constructor for LibraryDownloads
         * @param artifact Minecraft library artifact
         */
        public LibraryDownloads(MinecraftLibraryArtifact artifact) {
            this.artifact = artifact;
        }
    }
}
