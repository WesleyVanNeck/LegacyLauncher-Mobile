// net.kdt.pojavlaunch.extra

package net.kdt.pojavlaunch.extra;

/**
 * A class containing various constants used throughout the ExtraCore module.
 */
public final class ExtraConstants {

    /**
     * A mapping of game version names to their corresponding display names.
     */
    public static final String RELEASE_TABLE = "release_table";

    /**
     * A preference key used to track whether the back button has been pressed.
     */
    public static final String BACK_PREFERENCE = "back_preference";

    /**
     * The OpenGL version to expose.
     */
    public static final String OPEN_GL_VERSION = "open_gl_version";

    /**
     * A preference key used to indicate when the Microsoft authentication via webview is done.
     */
    public static final String MICROSOFT_LOGIN_TODO = "webview_login_done";

    /**
     * A preference key used to indicate whether to perform Mojang or "local" authentication.
     */
    public static final String MOJANG_LOGIN_TODO = "mojang_login_todo";

    /**
     * A preference key used to trigger the account selection procedure.
     */
    public static final String SELECT_AUTH_METHOD = "start_login_procedure";

    /**
     * A preference key used to store the selected file or folder as a string.
     */
    public static final String FILE_SELECTOR = "file_selector";

    /**
     * A preference key used to indicate that the version spinner needs to be refreshed.
     */
    public static final String REFRESH_VERSION_SPINNER = "refresh_version";

    /**
     * A preference key used to trigger the game launch.
     */
    public static final String LAUNCH_GAME = "launch_game";

    // Prevent instantiation
    private ExtraConstants() {
        throw new AssertionError("This class cannot be instantiated.");
    }
}

/* JVM
version=11
*/
