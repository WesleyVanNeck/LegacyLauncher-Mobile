package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ExpandableListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.JavaGUILauncherActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.ForgeDownloadTask;
import net.kdt.pojavlaunch.modloaders.ForgeUtils;
import net.kdt.pojavlaunch.modloaders.ForgeVersionListAdapter;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ForgeInstallFragment extends ModVersionListFragment<List<String>> {
    public static final String TAG = "ForgeInstallFragment";
    private static ModloaderListenerProxy sTaskProxy;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sTaskProxy = requireArguments().getParcelable("taskProxy");
    }

    @Override
    public int getTitleText() {
        return R.string.forge_dl_select_version;
    }

    @Override
    public int getNoDataMsg() {
        return R.string.forge_dl_no_installer;
    }

    @Override
    public ModloaderListenerProxy getTaskProxy() {
        return sTaskProxy;
    }

    @Override
    public void setTaskProxy(ModloaderListenerProxy proxy) {
        sTaskProxy = proxy;
    }

    @Override
    public List<String> loadVersionList() throws IOException {
        return ForgeUtils.downloadForgeVersions();
    }

    @Override
    public ExpandableListAdapter createAdapter(List<String> versionList, LayoutInflater layoutInflater) {
        return new ForgeVersionListAdapter(versionList, layoutInflater);
    }

    @Override
    public Runnable createDownloadTask(Object selectedVersion, ModloaderListenerProxy listenerProxy) {
        return new ForgeDownloadTask(listenerProxy, (String) selectedVersion);
    }

    @Override
    public void onDownloadFinished(Context context, File downloadedFile) {
        Intent modInstallerStartIntent = new Intent(context, JavaGUILauncherActivity.class);
        ForgeUtils.addAutoInstallArgs(modInstallerStartIntent, downloadedFile, true);
        context.startActivity(modInstallerStartIntent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onVersionSelected(int position, List<String> versionList) {
        super.onVersionSelected(position, versionList);
        if (sTaskProxy != null) {
            sTaskProxy.onVersionSelected(position, versionList);
        }
    }

    @NonNull
    @Override
    public Fragment argumentNotNull() {
        Fragment fragment = super.argumentNotNull();
        if (fragment instanceof ForgeInstallFragment) {
            ForgeInstallFragment f = (ForgeInstallFragment) fragment;
            f.sTaskProxy = requireNonNull(f.sTaskProxy);
            f.setTaskProxy(f.sTaskProxy);
        }
        return fragment;
    }

    @Override
    public LayoutInflater requireLayoutInflater() {
        return super.requireLayoutInflater();
    }

    @Override
    public Context requireContext() {
        return super.requireContext();
    }

    @Override
    public Activity requireActivity() {
        return super.requireActivity();
    }
}
