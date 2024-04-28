package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Architecture.archAsString;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;
import net.kdt.pojavlaunch.Tools;

import java.io.IOException;
import java.io.InputStream;

public class JRE17Util {
    public static final String NEW_JRE_NAME = "Internal-17";

    public static boolean checkInternalNewJre(AssetManager assetManager) {
        String launcherJreVersion = getJreVersionFromAssets(assetManager, "components/jre-new/version");
        String installedJreVersion = MultiRTUtils.__internal__readBinpackVersion(NEW_JRE_NAME);

        if (launcherJreVersion == null || installedJreVersion == null) {
            // If either version is null, return true if the installed JRE version is not null
            // This allows the code to proceed even if there is no update available
            return installedJreVersion != null;
        }

        if (!launcherJreVersion.equals(installedJreVersion)) {
            return unpackJre17(assetManager, launcherJreVersion);
        }

        return true;
    }

    private static boolean unpackJre17(AssetManager assetManager, String rtVersion) {
        try {
            if (isJre17Installed(NEW_JRE_NAME)) {
                // If the internal JRE is already installed, return true without unpacking it again
                return true;
            }

            MultiRTUtils.installRuntimeNamedBinpack(
                    getAssetInputStream(assetManager, "components/jre-new/universal.tar.xz"),
                    getAssetInputStream(assetManager, "components/jre-new/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                    NEW_JRE_NAME, rtVersion);
            MultiRTUtils.postPrepare(NEW_JRE_NAME);
            return true;
        } catch (IOException e) {
            Log.e("JRE17Auto", "Internal JRE unpack failed", e);
            return false;
        }
    }

    public static boolean isInternalNewJRE(String s_runtime) {
        Runtime runtime = MultiRTUtils.read(s_runtime);
        return runtime != null && NEW_JRE_NAME.equals(runtime.name);
    }

    /**
     * @return true if everything is good, false otherwise.
     */
    public static boolean installNewJreIfNeeded(Activity activity, JMinecraftVersionList.Version versionInfo) {
        LauncherProfiles.load();
        MinecraftProfile minecraftProfile = LauncherProfiles.getCurrentProfile();

        String selectedRuntime = Tools.getSelectedRuntime(minecraftProfile);
        Runtime runtime = MultiRTUtils.read(selectedRuntime);

        if (runtime == null || runtime.javaVersion < versionInfo.javaVersion.majorVersion) {
            String appropriateRuntime = MultiRTUtils.getNearestJreName(versionInfo.javaVersion.majorVersion);

            if (appropriateRuntime != null) {
                if (JRE17Util.isInternalNewJRE(appropriateRuntime)) {
                    if (!JRE17Util.checkInternalNewJre(activity.getAssets())) {
                        showRuntimeFail(activity, versionInfo);
                        return false;
                    }
                }
                minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + appropriateRuntime;
            } else {
                if (versionInfo.javaVersion.majorVersion <= 17) {
                    if (!JRE17Util.checkInternalNewJre(activity.getAssets())) {
                        showRuntimeFail(activity, versionInfo);
                        return false;
                    }
                    minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + JRE17Util.NEW_JRE_NAME;
                } else {
                    showRuntimeFail(activity, versionInfo);
                    return false;
                }
            }

            LauncherProfiles.save();
        }

        return true;
    }

    private static void showRuntimeFail(Activity activity, JMinecraftVersionList.Version verInfo) {
        Tools.dialogOnUiThread(activity, activity.getString(R.string.global_error),
                activity.getString(R.string.multirt_nocompartiblert, verInfo.javaVersion.majorVersion));
    }

    private static InputStream getAssetInputStream(AssetManager assetManager, String path) throws IOException {
        return assetManager.open(path);
    }

    private static String getJreVersionFromAssets(AssetManager assetManager, String path) {
        try {
            return Tools.read(getAssetInputStream(assetManager, path));
        } catch (IOException e) {
            Log.e("JRE17Util", "Failed to read JRE version from assets", e);
            return null;
        }
    }

    private static boolean isJre17Installed(String name) {
        return MultiRTUtils.__internal__readBinpackVersion(name) != null;
    }
}
