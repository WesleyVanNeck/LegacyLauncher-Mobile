package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.FabriclikeDownloadTask;
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils;
import net.kdt.pojavlaunch.modloaders.FabricVersion;
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils.ModloaderListenerProxy;
import net.kdt.pojavlaunch.modloaders.modpacks.SelfReferencingFuture;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * A fragment for installing a Fabric mod loader.
 */
public abstract class FabriclikeInstallFragment extends Fragment implements ModloaderDownloadListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "FabriclikeInstallFragment";

    private final FabriclikeUtils mFabriclikeUtils;
    private Spinner mGameVersionSpinner;
    private FabricVersion[] mGameVersionArray;
    private Future<?> mGameVersionFuture;
    private String mSelectedGameVersion;
    private Spinner mLoaderVersionSpinner;
    private FabricVersion[] mLoaderVersionArray;
    private Future<?> mLoaderVersionFuture;
    private String mSelectedLoaderVersion;
    private ProgressBar mProgressBar;
    private Button mStartButton;
    private View mRetryView;
    private CheckBox mOnlyStableCheckbox;

    /**
     * Constructs a new FabriclikeInstallFragment instance.
     *
     * @param mFabriclikeUtils the FabriclikeUtils instance to use
     */
    public FabriclikeInstallFragment(FabriclikeUtils mFabriclikeUtils) {
        super(R.layout.fragment_fabric_install);
        this.mFabriclikeUtils = mFabriclikeUtils;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        mStartButton = view.findViewById(R.id.fabric_installer_start_button);
        mStartButton.setOnClickListener(this::onClickStart);
        mGameVersionSpinner = view.findViewById(R.id.fabric_installer_game_ver_spinner);
        mGameVersionSpinner.setOnItemSelectedListener(new GameVersionSelectedListener());
        mLoaderVersionSpinner = view.findViewById(R.id.fabric_installer_loader_ver_spinner);
        mLoaderVersionSpinner.setOnItemSelectedListener(new LoaderVersionSelectedListener());
        mProgressBar = view.findViewById(R.id.fabric_installer_progress_bar);
        mRetryView = view.findViewById(R.id.fabric_installer_retry_layout);
        mOnlyStableCheckbox = view.findViewById(R.id.fabric_installer_only_stable_checkbox);
        mOnlyStableCheckbox.setOnCheckedChangeListener(this);
        view.findViewById(R.id.fabric_installer_retry_button).setOnClickListener(this::onClickRetry);

        // Set up the loader version spinner
        ((TextView) view.findViewById(R.id.fabric_installer_label_loader_ver))
                .setText(getString(R.string.fabric_dl_loader_version, mFabriclikeUtils.getName()));

        // Set up the listener proxy
        ModloaderListenerProxy proxy = getListenerProxy();
        if (proxy != null) {
            mStartButton.setEnabled(false);
            proxy.attachListener(this);
        }

        // Update the game versions
        updateGameVersions();
    }

    @Override
    public void onStop() {
        // Cancel any ongoing tasks
        cancelFutureChecked(mGameVersionFuture);
        cancelFutureChecked(mLoaderVersionFuture);

        // Detach the listener proxy
        ModloaderListenerProxy proxy = getListenerProxy();
        if (proxy != null) {
            proxy.detachListener();
        }

        super.onStop();
    }

    /**
     * Handles the click event for the start button.
     *
     * @param v the view that was clicked
     */
    private void onClickStart(View v) {
        if (ProgressKeeper.hasOngoingTasks()) {
            Toast.makeText(v.getContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
            return;
        }

        // Create a new listener proxy
        ModloaderListenerProxy proxy = new ModloaderListenerProxy();

        // Create a new download task
        FabriclikeDownloadTask fabricDownloadTask =
                new FabriclikeDownloadTask(proxy, mFabriclikeUtils, mSelectedGameVersion,
                        mSelectedLoaderVersion, true);

        // Attach the listener proxy
        proxy.attachListener(this);

        // Set the listener proxy
        setListenerProxy(proxy);

        // Disable the start button
        mStartButton.setEnabled(false);

        // Start the download task in a new thread
        new Thread(fabricDownloadTask).start();
    }

    /**
     * Handles the click event for the retry button.
     *
     * @param v the view that was clicked
     */
    private void onClickRetry(View v) {
        mStartButton.setEnabled(false);
        mRetryView.setVisibility(View.GONE);

        // Reset the game version spinner
        if (mGameVersionArray == null) {
            mGameVersionSpinner.setAdapter(null);
            updateGameVersions();
            return;
        }

        // Reset the loader version spinner
        mLoaderVersionSpinner.setAdapter(null);
        updateLoaderVersions();
    }

    /**
     * Called when the download task has finished.
     *
     * @param downloadedFile the downloaded file
     */
    @Override
    public void onDownloadFinished(File downloadedFile) {
        Tools.runOnUiThread(() -> {
            // Detach the listener proxy
            getListenerProxy().detachListener();

            // Set the listener proxy to null
            setListenerProxy(null);

            // Enable the start button
            mStartButton.setEnabled(true);

            // Pop the back stack
            getParentFragmentManager().popBackStackImmediate();
        });
    }

    /**
     * Called when the download task has failed to read the metadata.
     */
    @Override
    public void onDataNotAvailable() {
        Tools.runOnUiThread(() -> {
            // Detach the listener proxy
            getListenerProxy().detachListener();

            // Set the listener proxy to null
            setListenerProxy(null);

            // Enable the start button
            mStartButton.setEnabled(true);

            // Show an error dialog
            Tools.dialog(requireContext(),
                    requireContext().getString(R.string.global_error),
                    requireContext().getString(R.string.fabric_dl_cant_read_meta,
                            mFabriclikeUtils.getName()));
        });
    }

    /**
     * Called when the download task has failed with an exception.
     *
     * @param e the exception
     */
    @Override
    public void onDownloadError(Exception e) {
        Tools.runOnUiThread(() -> {
            // Detach the listener proxy
            getListenerProxy().detachListener();

            // Set the listener proxy to null
            setListenerProxy(null);

            // Enable the start button
            mStartButton.setEnabled(true);

            // Show an error dialog
            Tools.showError(requireContext(), e);
        });
    }

    /**
     * Cancels the given future if it's not already cancelled.
     *
     * @param future the future to cancel
     */
    private void cancelFutureChecked(Future<?> future) {
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
    }

    /**
     * Starts the loading animation and disables the start button.
     */
    private void startLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mStartButton.setEnabled(false);
    }

    /**
     * Stops the loading animation and enables the start button.
     */
    private void stopLoading() {
        mProgressBar.setVisibility(View.GONE);
        mStartButton.setEnabled(true);
    }

    /**
     * Creates a new adapter for the given fabric versions.
     *
     * @param fabricVersions the fabric versions
     * @param onlyStable     whether to only show stable versions
     * @return the new adapter
     */
    private ArrayAdapter<FabricVersion> createAdapter(FabricVersion[] fabricVersions, boolean onlyStable) {
        ArrayList<FabricVersion> filteredVersions = new ArrayList<>(fabricVersions.length);
        for (FabricVersion fabricVersion : fabricVersions) {
            if (!onlyStable || fabricVersion.stable) {
                filteredVersions.add(fabricVersion);
            }
        }
        filteredVersions.trimToSize();
        return new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, filteredVersions);
    }

    /**
     * Handles an exception for the given future.
     *
     * @param myFuture the future
     * @param e       the exception
     */
    private void onException(Future<?> myFuture, Exception e) {
        Tools.runOnUiThread(() -> {
            if (!myFuture.isCancelled()) {
                stopLoading();
                if (e != null) {
                    Tools.showError(requireContext(), e);
                }
                mRetryView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        updateGameSpinner();
        updateLoaderSpinner();
    }

    /**
     * A listener for the game version spinner.
     */
    class GameVersionSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            mSelectedGameVersion = ((FabricVersion) adapterView.getAdapter().getItem(i)).version;
            cancelFutureChecked(mLoaderVersionFuture);
            updateLoaderVersions();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            mSelectedGameVersion = null;
            if (mLoaderVersionFuture != null) {
                mLoaderVersionFuture.cancel(true);
            }
            adapterView.setAdapter(null);
        }

    }

    /**
     * A task for loading the loader versions.
     */
    class LoadLoaderVersionsTask implements SelfReferencingFuture.FutureInterface {

        @Override
        public void run(Future<?> myFuture) {
            Log.i(TAG, "Starting to load loader versions...");
            try {
                mLoaderVersionArray = mFabriclikeUtils.downloadLoaderVersions(mSelectedGameVersion);
                if (mLoaderVersionArray != null) {
                    onFinished(myFuture);
                } else {
                    onException(myFuture, null);
                }
            } catch (IOException e) {
                onException(myFuture, e);
            }
        }

        private void onFinished(Future<?> myFuture) {
            Tools.runOnUiThread(() -> {
                if (myFuture.isCancelled()) {
                    return;
                }
                stopLoading();
                updateLoaderSpinner();
            });
        }

    }

    /**
     * Updates the loader versions.
     */
    private void updateLoaderVersions() {
        startLoading();
        mLoaderVersionFuture = new SelfReferencingFuture(new LoadLoaderVersionsTask()).startOnExecutor(PojavApplication.sExecutorService);
    }

    /**
     * Updates the loader spinner.
     */
    private void updateLoaderSpinner() {
        mLoaderVersionSpinner.setAdapter(createAdapter(mLoaderVersionArray, mOnlyStableCheckbox.isChecked()));
    }

    /**
     * A listener for the loader version spinner.
     */
    class LoaderVersionSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            mSelectedLoaderVersion = ((FabricVersion) adapterView.getAdapter().getItem(i)).version;
            mStartButton.setEnabled(mSelectedGameVersion != null);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            mSelectedLoaderVersion = null;
            mStartButton.setEnabled(false);
        }

    }

    /**
     * A task for loading the game versions.
     */
    class LoadGameVersionsTask implements SelfReferencingFuture.FutureInterface {

        @Override
        public void run(Future<?> myFuture) {
            try {
                mGameVersionArray = mFabriclikeUtils.downloadGameVersions();
                if (mGameVersionArray != null) {
                    onFinished(myFuture);
                } else {
                    onException(myFuture, null);
                }
            } catch (IOException e) {
                onException(myFuture, e);
            }
        }

        private void onFinished(Future<?> myFuture) {
            Tools.runOnUiThread(() -> {
                if (myFuture.isCancelled()) {
                    return;
                }
                stopLoading();
                updateGameSpinner();
            });
        }

    }

    /**
     * Updates the game versions.
     */
    private void updateGameVersions() {
        startLoading();
        mGameVersionFuture = new SelfReferencingFuture(new LoadGameVersionsTask()).startOnExecutor(PojavApplication.sExecutorService);
    }

    /**
     * Updates the game spinner.
     */
    private void updateGameSpinner() {
        mGameVersionSpinner.setAdapter(createAdapter(mGameVersionArray, mOnlyStableCheckbox.isChecked()));
    }

    /**
     * Gets the listener proxy.
     *
     * @return the listener proxy
     */
    protected abstract ModloaderListenerProxy getListenerProxy();

    /**
     * Sets the listener proxy.
     *
     * @param listenerProxy the listener proxy
     */
    protected abstract void setListenerProxy(ModloaderListenerProxy listenerProxy);
}
