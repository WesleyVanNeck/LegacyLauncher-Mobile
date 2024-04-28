package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.databinding.FragmentModVersionListBinding;
import net.kdt.pojavlaunch.mirrors.DownloadMirror;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

import java.io.File;
import java.io.IOException;

public abstract class ModVersionListFragment<T> extends Fragment implements Runnable, View.OnClickListener, ExpandableListView.OnChildClickListener, ModloaderDownloadListener {
    public static final String TAG = "ForgeInstallFragment";
    private FragmentModVersionListBinding binding;
    private ProgressBar mProgressBar;
    private ModloaderListenerProxy taskProxy;

    public ModVersionListFragment() {
        super(R.layout.fragment_mod_version_list);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requireActivity().getLifecycle().addObserver(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentModVersionListBinding.bind(view);
        binding.titleTextView.setText(getTitleText());
        mProgressBar = binding.modDlListProgress;
        ExpandableListView expandableListView = binding.modDlExpandableVersionList;
        expandableListView.setOnChildClickListener(this);
        binding.forgeInstallerRetryButton.setOnClickListener(this);
        taskProxy = getTaskProxy();
        if (taskProxy != null) {
            expandableListView.setEnabled(false);
            taskProxy.attachListener(this);
        }
        new Thread(this).start();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onStop() {
        ModloaderListenerProxy taskProxy = getTaskProxy();
        if (taskProxy != null) taskProxy.detachListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        requireActivity().getLifecycle().removeObserver(this);
    }

    @Override
    public void run() {
        T versions = null;
        try {
            versions = loadVersionList();
        } catch (IOException e) {
            Log.e(TAG, "Error loading version list", e);
            Tools.runOnUiThread(() -> {
                mProgressBar.setVisibility(View.GONE);
                binding.modDlRetryLayout.setVisibility(View.VISIBLE);
            });
            return;
        }
        Tools.runOnUiThread(() -> {
            if (versions != null) {
                mProgressBar.setVisibility(View.GONE);
                binding.modDlRetryLayout.setVisibility(View.GONE);
                binding.modDlExpandableVersionList.setAdapter(createAdapter(versions, requireActivity().getLayoutInflater()));
            } else {
                mProgressBar.setVisibility(View.GONE);
                binding.modDlRetryLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View view) {
        mProgressBar.setVisibility(View.VISIBLE);
        binding.modDlRetryLayout.setVisibility(View.GONE);
        new Thread(this).start();
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        if (ProgressKeeper.hasOngoingTasks()) {
            Toast.makeText(expandableListView.getContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
            return true;
        }
        Object forgeVersion = expandableListView.getExpandableListAdapter().getChild(i, i1);
        ModloaderListenerProxy taskProxy = new ModloaderListenerProxy();
        Runnable downloadTask = createDownloadTask(forgeVersion, taskProxy);
        setTaskProxy(taskProxy);
        taskProxy.attachListener(this);
        expandableListView.setEnabled(false);
        new Thread(downloadTask).start();
        return true;
    }

    @Override
    public void onDownloadFinished(File downloadedFile) {
        Tools.runOnUiThread(() -> {
            getTaskProxy().detachListener();
            setTaskProxy(null);
            binding.modDlExpandableVersionList.setEnabled(true);
            requireActivity().getSupportFragmentManager().popBackStack();
            onDownloadFinished(requireContext(), downloadedFile);
        });
    }

    @Override
    public void onDataNotAvailable() {
        Tools.runOnUiThread(() -> {
            getTaskProxy().detachListener();
            setTaskProxy(null);
            binding.modDlExpandableVersionList.setEnabled(true);
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.global_error)
                    .setMessage(getNoDataMsg())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });
    }

    @Override
    public void onDownloadError(Exception e) {
        Tools.runOnUiThread(() -> {
            getTaskProxy().detachListener();
            setTaskProxy(null);
            binding.modDlExpandableVersionList.setEnabled(true);
            Tools.showError(requireContext(), e);
        });
    }

    public abstract int getTitleText();

    public abstract int getNoDataMsg();

    public abstract ModloaderListenerProxy getTaskProxy();

    public abstract T loadVersionList() throws IOException;

    public abstract void setTaskProxy(ModloaderListenerProxy proxy);

    public abstract ExpandableListAdapter createAdapter(T versionList, LayoutInflater layoutInflater);

    public abstract Runnable createDownloadTask(Object selectedVersion, ModloaderListenerProxy listenerProxy);

    public abstract void onDownloadFinished(Context context, File downloadedFile);
}
