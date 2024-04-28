package net.kdt.pojavlaunch.mirrors;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadMirror {
    public static final int DOWNLOAD_CLASS_LIBRARIES = 0;
    public static final int DOWNLOAD_CLASS_METADATA = 1;
    public static final int DOWNLOAD_CLASS_ASSETS = 2;

    private static final String URL_PROTOCOL_TAIL = "://";
    private static final String[] MIRROR_BMCLAPI = {
            "https://bmclapi2.bangbang93.com/maven",
            "https://bmclapi2.bangbang93.com",
            "https://bmclapi2.bangbang93.com/assets"
    };

    /**
     * Download a file with the current mirror. If the file is missing on the mirror,
     * fall back to the official source.
     * @param context The application context
     * @param downloadClass Class of the download. Can either be DOWNLOAD_CLASS_LIBRARIES,
     *                      DOWNLOAD_CLASS_METADATA or DOWNLOAD_CLASS_ASSETS
     * @param urlInput The original (Mojang) URL for the download
     * @param outputFile The output file for the download
     * @param buffer The shared buffer, or null if not used
     * @param monitor The download monitor, or null if not used
     * @return True if the download was successful, false otherwise
     */
    public static boolean downloadFileMirrored(Context context, int downloadClass, String urlInput, File outputFile,
                                              @Nullable byte[] buffer, @Nullable Tools.DownloaderFeedback monitor) {
        if (outputFile.exists() && outputFile.canWrite()) {
            return true;
        }

        try {
            return downloadFileMirroredInternal(context, downloadClass, urlInput, outputFile, buffer, monitor);
        } catch (IOException e) {
            Log.e("DownloadMirror", "Failed to download file", e);
            return false;
        }
    }

    /**
     * Download a file with the current mirror. If the file is missing on the mirror,
     * fall back to the official source.
     * @param context The application context
     * @param downloadClass Class of the download. Can either be DOWNLOAD_CLASS_LIBRARIES,
     *                      DOWNLOAD_CLASS_METADATA or DOWNLOAD_CLASS_ASSETS
     * @param urlInput The original (Mojang) URL for the download
     * @param outputFile The output file for the download
     * @return True if the download was successful, false otherwise
     */
    public static boolean downloadFileMirrored(Context context, int downloadClass, String urlInput, File outputFile) {
        return downloadFileMirrored(context, downloadClass, urlInput, outputFile, null, null);
    }

    /**
     * Check if the current download source is a mirror and not an official source.
     * @return true if the source is a mirror, false otherwise
     */
    public static boolean isMirrored() {
        return !LauncherPreferences.PREF_DOWNLOAD_SOURCE.equals("default");
    }

    private static boolean downloadFileMirroredInternal(Context context, int downloadClass, String urlInput, File outputFile,
                                                         @Nullable byte[] buffer, @Nullable Tools.DownloaderFeedback monitor) throws IOException {
        String mirrorUrl = getMirrorMapping(downloadClass, urlInput);
        if (mirrorUrl == null) {
            throw new IOException("Invalid mirror URL");
        }

        if (buffer != null) {
            DownloadUtils.downloadFileMonitored(mirrorUrl, outputFile, buffer, monitor);
        } else {
            DownloadUtils.downloadFileMonitored(mirrorUrl, outputFile, monitor);
        }

        if (outputFile.exists() && outputFile.length() > 0) {
            return true;
        } else {
            throw new FileNotFoundException("Failed to download file from mirror");
        }
    }

    private static String[] getMirrorSettings() {
        switch (LauncherPreferences.PREF_DOWNLOAD_SOURCE) {
            case "bmclapi": return MIRROR_BMCLAPI;
            case "default":
            default:
                return null;
        }
    }

    private static String getMirrorMapping(int downloadClass, String mojangUrl) {
        String[] mirrorSettings = getMirrorSettings();
        if (mirrorSettings == null) {
            return mojangUrl;
        }

        try {
            URL mojangUrlObj = new URL(mojangUrl);
            int urlTail = getBaseUrlTail(mojangUrlObj);
            String baseUrl = mojangUrlObj.getProtocol() + ":" + URL_PROTOCOL_TAIL + mojangUrlObj.getHost();
            String path = mojangUrl.substring(urlTail);

            switch (downloadClass) {
                case DOWNLOAD_CLASS_ASSETS:
                case DOWNLOAD_CLASS_METADATA:
                    baseUrl = mirrorSettings[downloadClass];
                    break;
                case DOWNLOAD_CLASS_LIBRARIES:
                    if (!baseUrl.endsWith("libraries.minecraft.net")) {
                        break;
                    }
                    baseUrl = mirrorSettings[downloadClass];
                    break;
                default:
                    break;
            }

            return baseUrl + path;
        } catch (MalformedURLException e) {
            Log.e("DownloadMirror", "Failed to parse URL", e);
            return null;
        }
    }

    private static int getBaseUrlTail(URL url) {
        int protocolNameEnd = url.toString().indexOf(URL_PROTOCOL_TAIL);
        if (protocolNameEnd == -1) {
            throw new MalformedURLException("No protocol, or non path-based URL");
        }
        protocolNameEnd += URL_PROTOCOL_TAIL.length();
        int hostnameEnd = url.toString().indexOf('/', protocolNameEnd);
        if (protocolNameEnd >= url.toString().length() || hostnameEnd == protocolNameEnd) {
            throw new MalformedURLException("No hostname");
        }
        if (hostnameEnd == -1) {
            hostnameEnd = url.toString().length();
        }
        return hostnameEnd;
    }
}
