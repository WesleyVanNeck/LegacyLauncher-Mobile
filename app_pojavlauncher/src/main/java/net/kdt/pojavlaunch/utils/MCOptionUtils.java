package net.kdt.pojavlaunch.utils;

import static org.lwjgl.glfw.CallbackBridge.windowHeight;
import static org.lwjgl.glfw.CallbackBridge.windowWidth;

import android.os.Build;
import android.os.FileObserver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MCOptionUtils {
    private static final String OPTIONS_FILE = "options.txt";
    private static final Map<String, String> PARAMETER_MAP = new WeakHashMap<>();
    private static FileObserver FILE_OBSERVER;
    private static String OPTION_FOLDER_PATH = null;

    public interface MCOptionListener {
        /**
         * Called when an option is changed.
         * Don't know which one though
         */
        void onOptionChanged();
    }

    public static void load() {
        load(OPTION_FOLDER_PATH == null
                ? Tools.DIR_GAME_NEW
                : OPTION_FOLDER_PATH);
    }

    public static void load(@NonNull String folderPath) {
        File optionFile = new File(folderPath, OPTIONS_FILE);

        if (!optionFile.exists()) {
            try {
                optionFile.createNewFile();
            } catch (IOException e) {
                Log.w(Tools.APP_NAME, "Could not create options.txt", e);
            }
        }

        if (FILE_OBSERVER == null || !Objects.equals(OPTION_FOLDER_PATH, folderPath)) {
            stopFileObserver();
            OPTION_FOLDER_PATH = folderPath;
            setupFileObserver();
        }

        PARAMETER_MAP.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(optionFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int firstColonIndex = line.indexOf(':');
                if (firstColonIndex < 0) {
                    Log.w(Tools.APP_NAME, "No colon on line \"" + line + "\", skipping");
                    continue;
                }
                PARAMETER_MAP.put(line.substring(0, firstColonIndex), line.substring(firstColonIndex + 1));
            }
        } catch (IOException e) {
            Log.w(Tools.APP_NAME, "Could not load options.txt", e);
        }
    }

    public static void set(@NonNull String key, @Nullable String value) {
        if (value == null) {
            PARAMETER_MAP.remove(key);
        } else {
            PARAMETER_MAP.put(key, value);
        }
    }

    public static String get(@NonNull String key) {
        return PARAMETER_MAP.get(key);
    }

    public static List<String> getAsList(@NonNull String key) {
        String value = get(key);

        if (value == null) {
            return new ArrayList<>();
        }

        // Remove the edges
        value = value.replace("[", "").replace("]", "");
        if (value.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.asList(value.split(","));
    }

    public static void save() {
        StringBuilder result = new StringBuilder();
        for (String key : PARAMETER_MAP.keySet()) {
            result.append(key)
                    .append(':')
                    .append(PARAMETER_MAP.get(key))
                    .append('\n');
        }

        File optionFile = new File(OPTION_FOLDER_PATH, OPTIONS_FILE);

        try (FileWriter writer = new FileWriter(optionFile)) {
            writer.write(result.toString());
        } catch (IOException e) {
            Log.w(Tools.APP_NAME, "Could not save options.txt", e);
        }
    }

    /**
     * @return The stored Minecraft GUI scale, also auto-computed if on auto-mode or improper setting
     */
    public static int getMcScale() {
        String str = get("guiScale");
        int guiScale = (str == null ? 0 : Integer.parseInt(str));

        int scale = Math.max(Math.min(windowWidth / 320, windowHeight / 240), 1);
        if (scale < guiScale || guiScale == 0) {
            guiScale = scale;
        }

        return guiScale;
    }

    /**
     * Add a file observer to reload options on file change
     * Listeners get notified of the change
     */
    private static void setupFileObserver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FILE_OBSERVER = new FileObserver(new File(OPTION_FOLDER_PATH, OPTIONS_FILE), FileObserver.MODIFY) {
                @Override
                public void onEvent(int i, @Nullable String s) {
                    load();
                    notifyListeners();
                }
            };
        } else {
            FILE_OBSERVER = new FileObserver(OPTION_FOLDER_PATH + "/" + OPTIONS_FILE, FileObserver.MODIFY) {
                @Override
                public void onEvent(int i, @Nullable String s) {
                    load();
                    notifyListeners();
                }
            };
        }

        FILE_OBSERVER.startWatching();
    }

    /**
     * Notify the option listeners
     */
    public static void notifyListeners() {
        for (WeakReference<MCOptionListener> weakReference : sOptionListeners) {
            MCOptionListener optionListener = weakReference.get();
            if (optionListener == null) continue;

            optionListener.onOptionChanged();
        }
    }

    /**
     * Add an option listener, notice how we don't have a reference to it
     */
    public static void addMCOptionListener(@NonNull MCOptionListener listener) {
        sOptionListeners.add(new WeakReference<>(listener));
    }

    /**
     * Remove a listener from existence, or at least, its reference here
     */
    public static void removeMCOptionListener(@NonNull MCOptionListener listener) {
        for (WeakReference<MCOptionListener> weakReference : sOptionListeners) {
            MCOptionListener optionListener = weakReference.get();
            if (optionListener == null) continue;
            if (optionListener == listener) {
                sOptionListeners.remove(weakReference);
                return;
            }
        }
    }

    private static void stopFileObserver() {
        if (FILE_OBSERVER != null) {
            FILE_OBSERVER.stopWatching();
            FILE_OBSERVER = null;
        }
    }

    private static final List<WeakReference<MCOptionListener>> sOptionListeners = new ArrayList<>();
}
