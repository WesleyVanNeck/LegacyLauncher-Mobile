package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.openURL;
import static net.kdt.pojavlaunch.Tools.swapFragment;
import static net.kdt.pojavlaunch.Tools.installMod;
import static net.kdt.pojavlaunch.Tools.swapFragment;
import static net.kdt.pojavlaunch.Tools.shareLog;
import static net.kdt.pojavlaunch.ProgressKeeper.getTaskCount;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.CustomControlsActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button newsButton = view.findViewById(R.id.news_button);
        Button customControlButton = view.findViewById(R.id.custom_control_button);
        Button installJarButton = view.findViewById(R.id.install_jar_button);
        Button shareLogsButton = view.findViewById(R.id.share_logs_button);

        ImageButton editProfileButton = view.findViewById(R.id.edit_profile_button);
        Button playButton = view.findViewById(R.id.play_button);

        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        newsButton.setOnClickListener(v -> openURL(requireActivity(), Tools.URL_HOME));
        customControlButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), CustomControlsActivity.class)));
        installJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        installJarButton.setOnLongClickListener(v->{
            runInstallerWithConfirmation(true);
            return true;
        });
        editProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));

        playButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        shareLogsButton.setOnClickListener((v) -> shareLog(requireContext()));

        newsButton.setOnLongClickListener((v)->{
            swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, null);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mVersionSpinner.reloadProfiles();
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (getTaskCount() == 0)
            installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }
}
