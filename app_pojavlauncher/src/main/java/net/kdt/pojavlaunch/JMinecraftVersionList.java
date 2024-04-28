package net.kdt.pojavlaunch;

import androidx.annotation.Keep;
import java.util.Map;
import java.util.Objects;

@Keep
@SuppressWarnings("unused") // all unused fields here are parts of JSON structures
public class JMinecraftVersionList {

    /**
     * Mapping of version names to their corresponding URLs.
     */
    public Map<String, String> latest;

    /**
     * Array of version objects, each containing information about a specific Minecraft version.
     */
    public Version[] versions;

    @Keep
    public static class FileProperties {
        /**
         * The unique identifier for the file.
         */
        public String id;

        /**
         * The SHA-1 hash of the file.
         */
        public String sha1;

        /**
         * The URL of the file.
         */
        public String url;

        /**
         * Constructs a new FileProperties object with the given fields.
         *
         * @param id   the unique identifier for the file
         * @param sha1 the SHA-1 hash of the file
         * @param url  the URL of the file
         */
        public FileProperties(String id, String sha1, String url) {
            this.id = id;
            this.sha1 = sha1;
            this.url = url;
        }
    }

    /**
     * Represents a Minecraft version.
     */
    @Keep
    public static class Version extends FileProperties {
        // Since 1.13, so it's one of ways to check

        /**
         * The command-line arguments for the game process.
         */
        public Arguments arguments;

        /**
         * The asset index for the version.
         */
        public AssetIndex assetIndex;

        /**
         * The URL for the assets of the version.
         */
        public String assets;

        /**
         * A mapping of download names to download objects, each containing information about a specific download.
         */
        public Map<String, MinecraftClientInfo> downloads;

        /**
         * The unique identifier of the parent version, if this version is a child of another version.
         */
        public String inheritsFrom;

        /**
         * Information about the required Java version for the version.
         */
        public JavaVersionInfo javaVersion;

        /**
         * An array of library objects, each containing information about a specific library required by the version.
         */
        public DependentLibrary[] libraries;

        /**
         * The logging configuration for the version.
         */
        public LoggingConfig logging;

        /**
         * The name of the main class to run for the version.
         */
        public String mainClass;

        /**
         * The command-line arguments for the Java Virtual Machine (JVM).
         */
        public String minecraftArguments;

        /**
         * The minimum launcher version required to use this version.
         */
        public int minimumLauncherVersion;

        /**
         * The release time of the version, in UTC format.
         */
        public String releaseTime;

        /**
         * The time that the version was created, in UTC format.
         */
        public String time;

        /**
         * The type of the version (e.g., "release", "snapshot").
         */
        public String type;

        /**
         * Constructs a new Version object with the given fields.
         *
         * @param id          the unique identifier for the file
         * @param sha1       the SHA-1 hash of the file
         * @param url         the URL of the file
         * @param arguments  the command-line arguments for the game process
         * @param assetIndex the asset index for the version
         * @param assets     the URL for the assets of the version
         * @param downloads  a mapping of download names to download objects
         * @param inheritsFrom the unique identifier of the parent version
         * @param javaVersion information about the required Java version
         * @param libraries  an array of library objects
         * @param logging    the logging configuration for the version
         * @param mainClass  the name of the main class to run
         * @param minecraftArguments the command-line arguments for the JVM
         * @param minimumLauncherVersion the minimum launcher version required
         * @param releaseTime the release time of the version
         * @param time       the time that the version was created
         * @param type       the type of the version
         */
        public Version(String id, String sha1, String url, Arguments arguments,
                       AssetIndex assetIndex, String assets,
                       Map<String, MinecraftClientInfo> downloads,
                       String inheritsFrom, JavaVersionInfo javaVersion,
                       DependentLibrary[] libraries, LoggingConfig logging,
                       String mainClass, String minecraftArguments,
                       int minimumLauncherVersion, String releaseTime,
                       String time, String type) {
            super(id, sha1, url);
            this.arguments = arguments;
            this.assetIndex = assetIndex;
            this.assets = assets;
            this.downloads = downloads;
            this.inheritsFrom = inheritsFrom;
            this.javaVersion = javaVersion;
            this.libraries = libraries;
            this.logging = logging;
            this.mainClass = mainClass;
            this.minecraftArguments = minecraftArguments;
            this.minimumLauncherVersion = minimumLauncherVersion;
            this.releaseTime = releaseTime;
            this.time = time;
            this.type = type;
        }
    }

    /**
     * Information about the required Java version for a Minecraft version.
     */
    @Keep
    public static class JavaVersionInfo {
        /**
         * The name of the Java component required by the version.
         */
        public String component;

        /**
         * The major version of the required Java component.
         */
        public int majorVersion;

        /**
         * The version of the required Java component.
         */
        public int version; // parameter used by LabyMod 4

        /**
         * Constructs a new JavaVersionInfo object with the given fields.
         *
         * @param component the name of the Java component required by the version
         * @param majorVersion the major version of the required Java component
         * @param version the version of the required Java component
         */
        public JavaVersionInfo(String component, int majorVersion, int version) {
            this.component = component;
            this.majorVersion = majorVersion;
            this.version = version;
        }
    }

    /**
     * The logging configuration for a Minecraft version.
     */
    @Keep
    public static class LoggingConfig {
        /**
         * The logging configuration for the client.
         */
        public LoggingClientConfig client;

        /**
         * Constructs a new LoggingConfig object with the given fields.
         *
         * @param client the logging configuration for the client
         */
        public LoggingConfig(LoggingClientConfig client) {
            this.client = client;
        }
    }

    /**
     * The logging configuration for the client.
     */
    @Keep
    public static class LoggingClientConfig {
        /**
         * The argument for the logging configuration.
         */
        public String argument;

        /**
         * The file properties for the logging configuration.
         */
        public FileProperties file;

        /**
         * The type of the logging configuration.
         */
        public String type;

        /**
         * Constructs a new LoggingClientConfig object with the given fields.
         *
         * @param argument the argument for the logging configuration
         * @param file     the file properties for the logging configuration
         * @param type     the type of the logging configuration
         */
        public LoggingClientConfig(String argument, FileProperties file, String type) {
            this.argument = argument;
            this.file = file;
            this.type = type;
        }
    }

    /**
     * The command-line arguments for a Minecraft version.
     */
    @Keep
    public static class Arguments {
        // Since 1.13

        /**
         * An array of game arguments.
         */
        public Object[] game;

        /**
         * An array of JVM arguments.
         */
        public Object[] jvm;

        /**
         * An array of argument value objects, each containing information about a specific argument value.
         */
        public ArgValue[] gameArgs;

        /**
         * An array of argument value objects, each containing information about a specific JVM argument value.
         */
        public ArgValue[] jvmArgs;

        /**
         * Constructs a new Arguments object with the given fields.
         *
         * @param game          an array of game arguments
         * @param jvm          an array of JVM arguments
         * @param gameArgs      an array of game argument value objects
         * @param jvmArgs      an array of JVM argument value objects
         */
        public Arguments(Object[] game, Object[] jvm, ArgValue[] gameArgs, ArgValue[] jvmArgs) {
            this.game = game;
            this.jvm = jvm;
            this.gameArgs = gameArgs;
            this.jvmArgs = jvmArgs;
        }

        /**
         * Represents an argument value for a Minecraft version.
         */
        @Keep
        public static class ArgValue {
            /**
             * An array of argument rules objects, each containing information about a specific argument rule.
             */
            public ArgRules[] rules;

            /**
             * The value of the argument.
             */
            public String value;

            /**
             * An array of possible values for the argument.
             */
            public String[] values;

            /**
             * Constructs a new ArgValue object with the given fields.
             *
             * @param rules      an array of argument rules objects
             * @param value     the value of the argument
             * @param values    an array of possible values for the argument
             */
            public ArgValue(ArgRules[] rules, String value, String[] values) {
                this.rules = rules;
                this.value = value;
                this.values = values;
            }
        }

        /**
         * Represents an argument rule for a Minecraft version.
         */
        @Keep
        public static class ArgRules {
            /**
             * The action to perform on the argument.
             */
            public String action;

            /**
             * The features to apply to the argument.
             */
            public String features;

            /**
             * The operating system to apply the rule to.
             */
            public ArgOS os;

            /**
             * Constructs a new ArgRules object with the given fields.
             *
             * @param action  the action to perform on the argument
             * @param features the features to apply to the argument
             * @param os      the operating system to apply the rule to
             */
            public ArgRules(String action, String features, ArgOS os) {
                this.action = action;
                this.features = features;
                this.os = os;
            }
        }

        /**
         * Represents the operating system for an argument rule.
         */
        @Keep
        public static class ArgOS {
            /**
             * The name of the operating system.
             */
            public String name;

            /**
             * The version of the operating system.
             */
            public String version;

            /**
             * Constructs a new ArgOS object with the given fields.
             *
             * @param name  the name of the operating system
             * @param version the version of the operating system
             */
            public ArgOS(String name, String version) {
                this.name = name;
                this.version = version;
            }
        }
    }

    /**
     * The asset index for a Minecraft version.
     */
    @Keep
    public static class AssetIndex extends FileProperties {
        /**
         * The total size of the assets.
         */
        public long totalSize;

        /**
