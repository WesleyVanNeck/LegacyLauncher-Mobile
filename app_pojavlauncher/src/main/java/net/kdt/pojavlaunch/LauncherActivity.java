package net.kdt.pojavlaunch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kdt.mcgui.ProgressLayout;
import com.kdt.mcgui.mcAccountSpinner;

import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.extra.ExtraListener;
import net.kdt.pojavlaunch.fragments.MainMenuFragment;
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment;
import net.kdt.pojavlaunch.fragments.SelectAuthFragment;
import net.kdt.pojavlaunch.lifecycle.ContextExecutor;
import net.kdt.pojavlaunch.modloaders.modpacks.ModloaderInstallTracker;
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.IconCacheJanitor;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;
import net.kdt.pojavlaunch.services.ProgressServiceKeeper;
import net.kdt.pojavlaunch.tasks.AsyncMinecraftDownloader;
import net.kdt.pojavlaunch.tasks.AsyncVersionList;
import net.kdt.pojavlaunch.tasks.MinecraftDownloader;
import net.kdt.pojavlaunch.utils.NotificationUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

public class LauncherActivity extends AppCompatActivity implements TaskCountListener {

    public static final String SETTING_FRAGMENT_TAG = "SETTINGS_FRAGMENT";

    private FragmentContainerView mFragmentView;
    private ImageButton mSettingsButton, mDeleteAccountButton;
    private ProgressLayout mProgressLayout;
    private ProgressServiceKeeper mProgressServiceKeeper;
    private ModloaderInstallTracker mInstallTracker;
    private NotificationManager mNotificationManager;

    private final FragmentManager.FragmentLifecycleCallbacks mFragmentCallbackListener = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            updateSettingButtonImage(f);
        }
    };

    private final ExtraListener<String> mBackPreferenceListener = (key, value) -> {
        if (value.equals("true")) onBackPressed();
        return false;
    };

    private final ExtraListener<Boolean> mSelectAuthMethod = (key, value) -> {
        Fragment fragment = getSupportFragmentManager().findFragmentById(mFragmentView.getId());
        if (!(fragment instanceof MainMenuFragment)) return false;

        Tools.swapFragment(LauncherActivity.this, SelectAuthFragment.class, SelectAuthFragment.TAG, null);
        return false;
    };

    private final View.OnClickListener mSettingButtonListener = v -> {
        Fragment fragment = getSupportFragmentManager().findFragmentById(mFragmentView.getId());
        if (fragment instanceof MainMenuFragment) {
            Tools.swapFragment(LauncherActivity.this, LauncherPreferenceFragment.class,
                    SETTING_FRAGMENT_TAG, null);
        } else {
            Tools.backToMainMenu(LauncherActivity.this);
        }
    };

    private final View.OnClickListener mAccountDeleteButtonListener = v -> {
        if (mAccountSpinner.getSelectedAccount() == null) {
            Toast.makeText(LauncherActivity.this, R.string.no_saved_accounts, Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(LauncherActivity.this)
                .setMessage(R.string.warning_remove_account)
                .setPositiveButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.global_delete, (dialog, which) ->
                        mAccountSpinner.removeCurrentAccount())
                .show();
    };

    private final ExtraListener<Boolean> mLaunchGameListener = (key, value) -> {
        if (mProgressLayout.hasProcesses()) {
            Toast.makeText(LauncherActivity.this, R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
            return false;
        }

        String selectedProfile = LauncherPreferences.DEFAULT_PREF.getString(
                LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "");
        if (LauncherProfiles.mainProfileJson == null || !LauncherProfiles.mainProfileJson.profiles.containsKey(
                selectedProfile)) {
            Toast.makeText(LauncherActivity.this, R.string.error_no_version, Toast.LENGTH_LONG).show();
            return false;
        }
        MinecraftProfile prof = LauncherProfiles.mainProfileJson.profiles.get(selectedProfile);
        if (prof == null || prof.lastVersionId == null || "Unknown".equals(prof.lastVersionId)) {
            Toast.makeText(LauncherActivity.this, R.string.error_no_version, Toast.LENGTH_LONG).show();
            return false;
        }

        if (mAccountSpinner.getSelectedAccount() == null) {
            Toast.makeText(LauncherActivity.this, R.string.no_saved_accounts, Toast.LENGTH_LONG).show();
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
            return false;
        }

        String normalizedVersionId = AsyncMinecraftDownloader.normalizeVersionId(prof.lastVersionId);
        JMinecraftVersionList.Version mcVersion = AsyncMinecraftDownloader.getListedVersion(normalizedVersionId);
        new MinecraftDownloader().start(
                LauncherActivity.this,
                mcVersion,
                normalizedVersionId,
                new ContextAwareDoneListener(LauncherActivity.this, normalizedVersionId)
        );
        return false;
    };

    private final ActivityResultLauncher<String> mRequestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isAllowed -> {
                if (!isAllowed) handleNoNotificationPermission();
                else {
                    Runnable runnable = Tools.getWeakReference(mRequestNotificationPermissionRunnable);
                    if (runnable != null) runnable.run();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pojav_launcher);

        mFragmentView = findViewById(R.id.container_fragment);
        mSettingsButton = findViewById(R.id.setting_button);
        mDeleteAccountButton = findViewById(R.id.delete_account_button);
        mProgressLayout = findViewById(R.id.progress_layout);

        IconCacheJanitor.runJanitor();
        checkNotificationPermission();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ProgressKeeper.addTaskCountListener(this);
        ProgressKeeper.addTaskCountListener((mProgressServiceKeeper = new ProgressServiceKeeper(this)));

        mSettingsButton.setOnClickListener(mSettingButtonListener);
        mDeleteAccountButton.setOnClickListener(mAccountDeleteButtonListener);

        ExtraCore.addExtraListener(ExtraConstants.BACK_PREFERENCE, mBackPreferenceListener);
        ExtraCore.addExtraListener(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod);
        ExtraCore.addExtraListener(ExtraConstants.LAUNCH_GAME, mLaunchGameListener);

        new AsyncVersionList().getVersionList(versions -> ExtraCore.setValue(ExtraConstants.RELEASE_TABLE, versions),
                false);

        mInstallTracker = new ModloaderInstallTracker(this);

        mProgressLayout.observe(ProgressLayout.DOWNLOAD_MINECRAFT);
        mProgressLayout.observe(ProgressLayout.UNPACK_RUNTIME);
        mProgressLayout.observe(ProgressLayout.INSTALL_MODPACK);
        mProgressLayout.observe(ProgressLayout.AUTHENTICATE_MICROSOFT);
        mProgressLayout.observe(ProgressLayout.DOWNLOAD_VERSION_LIST);

        if (savedInstanceState == null) {
            loadMainMenuFragment();
        }
    }

    @Override
    public void onTaskCountChanged(int taskCount) {
        // Hide the notification that starts the game if there are tasks executing.
        // Prevents the user from trying to launch the game with tasks ongoing.
        if (taskCount > 0) {
            Tools.runOnUiThread(() -> mNotificationManager.cancel(
                    NotificationUtils.NOTIFICATION_ID_GAME_START));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContextExecutor.setActivity(this);
        mInstallTracker.attach();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ContextExecutor.clearActivity();
        mInstallTracker.detach();
    }

    @Override
    public boolean setFullscreen() {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(mFragmentCallbackListener, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressLayout.cleanUpObservers();
        ProgressKeeper.removeTaskCountListener(this);
        ProgressKeeper.removeTaskCountListener(mProgressServiceKeeper);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.BACK_PREFERENCE, mBackPreferenceListener);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.LAUNCH_GAME, mLaunchGameListener);

        getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(mFragmentCallbackListener);
    }

    @Override
    public void onBackPressed() {
        MicrosoftLoginFragment fragment = (MicrosoftLoginFragment) getVisibleFragment(MicrosoftLoginFragment.TAG);
        if (fragment != null) {
            if (fragment.canGoBack()) {
                fragment.goBack();
                return;
            }
        }

        // Check if we are at the root then
        if (getVisibleFragment("ROOT") != null) {
            finish();
        }

        super.onBackPressed();
    }

    @Override
    public void onAttachedToWindow() {
        LauncherPreferences.computeNotchSize(this);
    }

    private void loadMainMenuFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.addToBackStack("ROOT");
        transaction.add(R.id.container_fragment, MainMenuFragment.class, null, "ROOT").commit();
    }

    private Fragment getVisibleFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null && fragment.isVisible()) {
            return fragment;
        }
        return null;
    }

    private void checkNotificationPermission() {
        if (LauncherPreferences.PREF_SKIP_NOTIFICATION_PERMISSION_CHECK ||
                checkForNotificationPermission()) {
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS)) {
            showNotificationPermissionReasoning();
            return;
        }
        askForNotificationPermission(null);
    }

    private void showNotificationPermissionReasoning() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.notification_permission_dialog_title)
                .setMessage(R.string.notification_permission_dialog_text)
                .setPositiveButton(android.R.string.ok, (d, w) -> askForNotificationPermission(null))
                .setNegativeButton(android.R.string.cancel, (d, w) -> handleNoNotificationPermission())
                .show();
    }

    private void handleNoNotificationPermission() {
        LauncherPreferences.PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = true;
        LauncherPreferences.DEFAULT_PREF.edit()
                .putBoolean(LauncherPreferences.PREF_KEY_SKIP_NOTIFICATION_CHECK, true)
                .apply();
        Toast.makeText(this, R.string.notification_permission_toast, Toast.LENGTH_LONG).show();
    }

    public boolean checkForNotificationPermission() {
        return Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_DENIED;
    }

    public void askForNotificationPermission(Runnable onSuccessRunnable) {
        if (Build.VERSION.SDK_INT < 33) return;
        if (onSuccessRunnable != null) {
            mRequestNotificationPermissionRunnable = new WeakReference<>(onSuccessRunnable);
        }
        mRequestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void updateSettingButtonImage(Fragment fragment) {
        if (fragment instanceof MainMenuFragment) {
            mSettingsButton.setImageDrawable(
                    ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_menu_settings));
        } else {
            mSettingsButton.setImageDrawable(
                    ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_menu_home));
        }
    }
}
