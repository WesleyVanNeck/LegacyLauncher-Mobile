package net.kdt.pojavlaunch.modloaders.modpacks.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

/**
 * POJO to represent the Modrinth index inside mrpacks.
 */
public class ModrinthIndex {

    @SerializedName("format_version")
    @Expose
    public int formatVersion;
    @SerializedName("game")
    @Expose
    public String game;
    @SerializedName("version_id")
    @Expose
    public String versionId;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("summary")
    @Expose
    public String summary;
    @SerializedName("files")
    @Expose
    public ModrinthIndexFile[] files;
    @SerializedName("dependencies")
    @Expose
    public Map<String, String> dependencies;

    /**
     * Constructor for ModrinthIndex.
     *
     * @param formatVersion Format version of the Modrinth index.
     * @param game          Game associated with the Modrinth index.
     * @param versionId     Version ID of the Modrinth index.
     * @param name          Name of the Modrinth index.
     * @param summary       Summary of the Modrinth index.
     * @param files         Files in the Modrinth index.
     * @param dependencies  Dependencies of the Modrinth index.
     */
    public ModrinthIndex(int formatVersion, String game, String versionId, String name, String summary, ModrinthIndexFile[] files, Map<String, String> dependencies) {
        this.formatVersion = formatVersion;
        this.game = game;
        this.versionId = versionId;
        this.name = name;
        this.summary = summary;
        this.files = files;
        this.dependencies = dependencies;
    }

    @Override
    @NotNull
    public String toString() {
        return "ModrinthIndex{" +
                "formatVersion=" + formatVersion +
                ", game='" + game + '\'' +
                ", versionId='" + versionId + '\'' +
                ", name='" + name + '\'' +
                ", summary='" + summary + '\'' +
                ", files=" + Arrays.toString(files) +
                '}';
    }

    /**
     * POJO to represent a Modrinth index file.
     */
    public static class ModrinthIndexFile {

        @SerializedName("path")
        @Expose
        public String path;
        @SerializedName("downloads")
        @Expose
        public String[] downloads;
        @SerializedName("file_size")
        @Expose
        public int fileSize;
        @SerializedName("hashes")
        @Expose
        public ModrinthIndexFileHashes hashes;
        @SerializedName("env")
        @Expose
        @Nullable
        public ModrinthIndexFileEnv env;

        /**
         * Constructor for ModrinthIndexFile.
         *
         * @param path          Path of the Modrinth index file.
         * @param downloads     Downloads for the Modrinth index file.
         * @param fileSize      File size of the Modrinth index file.
         * @param hashes        Hashes for the Modrinth index file.
         * @param env           Environment for the Modrinth index file.
         */
        public ModrinthIndexFile(String path, String[] downloads, int fileSize, ModrinthIndexFileHashes hashes, @Nullable ModrinthIndexFileEnv env) {
            this.path = path;
            this.downloads = downloads;
            this.fileSize = fileSize;
            this.hashes = hashes;
            this.env = env;
        }

        @Override
        @NotNull
        public String toString() {
            return "ModrinthIndexFile{" +
                    "path='" + path + '\'' +
                    ", downloads=" + Arrays.toString(downloads) +
                    ", fileSize=" + fileSize +
                    ", hashes=" + hashes +
                    ", env=" + env +
                    '}';
        }

        /**
         * POJO to represent hashes for a Modrinth index file.
         */
        public static class ModrinthIndexFileHashes {

            @SerializedName("sha1")
            @Expose
            public String sha1;
            @SerializedName("sha512")
            @Expose
            public String sha512;

            /**
             * Constructor for ModrinthIndexFileHashes.
             *
             * @param sha1 SHA-1 hash of the Modrinth index file.
             * @param sha512 SHA-512 hash of the Modrinth index file.
             */
            public ModrinthIndexFileHashes(String sha1, String sha512) {
                this.sha1 = sha1;
                this.sha512 = sha512;
            }

            @Override
            @NotNull
            public String toString() {
                return "ModrinthIndexFileHashes{" +
                        "sha1='" + sha1 + '\'' +
                        ", sha512='" + sha512 + '\'' +
                        '}';
            }
        }

        /**
         * POJO to represent the environment for a Modrinth index file.
         */
        public static class ModrinthIndexFileEnv {

            @SerializedName("client")
            @Expose
            public String client;
            @SerializedName("server")
            @Expose
            public String server;

            /**
             * Constructor for ModrinthIndexFileEnv.
             *
             * @param client Client environment for the Modrinth index file.
             * @param server Server environment for the Modrinth index file.
             */
            public ModrinthIndexFileEnv(String client, String server) {
                this.client = client;
                this.server = server;
            }

            @Override
            @NotNull
            public String toString() {
                return "ModrinthIndexFileEnv{" +
                        "client='" + client + '\'' +
                        ", server='" + server + '\'' +
                        '}';
            }
        }
    }
}
