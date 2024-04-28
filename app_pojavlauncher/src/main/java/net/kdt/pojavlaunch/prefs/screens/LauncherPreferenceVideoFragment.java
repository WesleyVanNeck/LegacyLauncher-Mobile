package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_NOTCH_SIZE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.util.Arrays;
import java.util.List;

/**
 * Fragment for any settings video related
 */
public class LauncherPreferenceVideoFragment extends PreferenceFragmentCompat {
    private CustomSeekBarPreference seek5;
    private SwitchPreference sustainedPerfSwitch;
    private ListPreference rendererListPreference;
    private SwitchPreferenceCompat forceVsyncSwitch;

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_video);

        initPreferences();

        setPreferenceVisibility();

        setPreferenceListeners();
    }

    private void initPreferences() {
        seek5 = findPreference("resolutionRatio", CustomSeekBarPreference.class);
        seek5.setMin(25);
        seek5.setSuffix(" %");

        sustainedPerfSwitch = findPreference("sustainedPerformance", SwitchPreference.class);

        rendererListPreference = findPreference("renderer", ListPreference.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setPreferenceVisibility() {
        seek5.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && PREF_NOTCH_SIZE > 0);
        sustainedPerfSwitch.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
    }

    private void setPreferenceListeners() {
        seek5.addOnPreferenceChangeListener((preference, newValue) -> {
            if (Integer.parseInt(newValue.toString()) < 25) {
                seek5.setValue(100);
            }
            return true;
        });

        SharedPreferences.OnSharedPreferenceChangeListener listener = (p, s) -> {
            if (s.equals("useAlternateSurface")) {
                forceVsyncSwitch.setVisible(LauncherPreferences.PREF_USE_ALTERNATE_SURFACE);
            }
        };

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

        forceVsyncSwitch = findPreference("force_vsync", SwitchPreferenceCompat.class);
        forceVsyncSwitch.setVisible(LauncherPreferences.PREF_USE_ALTERNATE_SURFACE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    private ListPreference findPreference(String key, Class<? extends Preference> clazz) {
        return (ListPreference) findPreference(key, clazz, getPreferenceScreen());
    }

    @NonNull
    private <T extends Preference> T findPreference(String key, Class<T> clazz, PreferenceGroup group) {
        for (Preference preference : group) {
            if (preference.getKey().equals(key) && clazz.isAssignableFrom(preference.getClass())) {
                return (T) preference;
            }
            if (preference instanceof PreferenceGroup) {
                T found = findPreference(key, clazz, (PreferenceGroup) preference);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
