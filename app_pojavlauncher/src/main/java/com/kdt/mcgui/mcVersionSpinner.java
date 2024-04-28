package com.kdt.mcgui;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static net.kdt.pojavlaunch.fragments.ProfileEditorFragment.DELETED_PROFILE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.transition.Slide;
import android.transition.Transition;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.fragments.ProfileEditorFragment;
import net.kdt.pojavlaunch.fragments.ProfileTypeSelectFragment;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.profiles.ProfileAdapter;
import net.kdt.pojavlaunch.profiles.ProfileAdapterExtra;

import fr.spse.extended_view.ExtendedTextView;

/**
 * A custom spinner that displays a dropdown popup view with a custom direction.
 */
public class mcVersionSpinner extends ExtendedTextView {

    private static final int VERSION_SPINNER_PROFILE_CREATE = 0;

    // Variables for the list view and popup window
    private ListView mListView;
    private PopupWindow mPopupWindow;
    private Object mPopupAnimation;
    private int mSelectedIndex;

    // The profile adapter for the spinner
    private final ProfileAdapter mProfileAdapter = new ProfileAdapter(new ProfileAdapterExtra[]{
            new ProfileAdapterExtra(VERSION_SPINNER_PROFILE_CREATE,
                    R.string.create_profile,
                    ResourcesCompat.getDrawable(getResources(), R.drawable.ic_add, null)),
    });

    /**
     * Constructor with context.
     *
     * @param context The context.
     */
    public mcVersionSpinner(@NonNull Context context) {
        super(context);
        init();
    }

    /**
     * Constructor with context and attributes.
     *
     * @param context The context.
     * @param attrs   The attributes.
     */
    public mcVersionSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor with context, attributes, and def style.
     *
     * @param context   The context.
     * @param attrs     The attributes.
     * @param defStyle  The def style.
     */
    public mcVersionSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Sets the profile selection and saves it as a shared preference.
     *
     * @param position The position of the selected profile.
     */
    public void setProfileSelection(int position) {
        setSelection(position);
        LauncherPreferences.DEFAULT_PREF.edit()
                .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE,
                        mProfileAdapter.getItem(position).toString())
                .apply();
    }

    /**
     * Sets the selection of the spinner.
     *
     * @param position The position of the selected item.
     */
    public void setSelection(int position) {
        if (mListView != null) mListView.setSelection(position);
        mProfileAdapter.setView(this, mProfileAdapter.getItem(position), false);
        mSelectedIndex = position;
    }

    /**
     * Opens the profile editor.
     *
     * @param fragmentActivity The fragment activity.
     */
    public void openProfileEditor(FragmentActivity fragmentActivity) {
        Object currentSelection = mProfileAdapter.getItem(mSelectedIndex);
        if (currentSelection instanceof ProfileAdapterExtra) {
            performExtraAction((ProfileAdapterExtra) currentSelection);
        } else {
            Tools.swapFragment(fragmentActivity, ProfileEditorFragment.class, ProfileEditorFragment.TAG, null);
        }
    }

    /**
     * Reloads the profiles from the file.
     */
    public void reloadProfiles() {
        mProfileAdapter.reloadProfiles();
    }

    /**
     * Initializes various behaviors.
     */
    private void init() {
        // Setup various attributes
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen._12ssp));
        setGravity(Gravity.CENTER_VERTICAL);
        int startPadding = getContext().getResources().getDimensionPixelOffset(R.dimen._17sdp);
        int endPadding = getContext().getResources().getDimensionPixelOffset(R.dimen._5sdp);
        setPaddingRelative(startPadding, 0, endPadding, 0);
        setCompoundDrawablePadding(startPadding);

        int profileIndex = mProfileAdapter.resolveProfileIndex(
                LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, ""));

        setProfileSelection(Math.max(0, profileIndex));

        // Popup window behavior
        setOnClickListener(v -> {
            if (mPopupWindow == null) getPopupWindow();

            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
                return;
            }
            mPopupWindow.showAsDropDown(mcVersionSpinner.this, 0, -getContext().getResources().getDimensionPixelOffset(R.dimen._4sdp));
            // Post() is required for the layout inflation phase
            post(() -> mListView.setSelection(mSelectedIndex));
        });
    }

    /**
     * Performs the extra action based on the selected item.
     *
     * @param extra The extra object.
     */
    private void performExtraAction(ProfileAdapterExtra extra) {
        // Replace with switch-case if you want to add more extra actions
        if (extra.id == VERSION_SPINNER_PROFILE_CREATE) {
            Tools.swapFragment((FragmentActivity) getContext(), ProfileTypeSelectFragment.class,
                    ProfileTypeSelectFragment.TAG, null);
        }
    }

    /**
     * Creates the list view and popup window for the interface, and sets up the click behavior.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void getPopupWindow() {
        mListView = (ListView) inflate(getContext(), R.layout.spinner_mc_version, null);
        mListView.setAdapter(mProfileAdapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Object item = mProfileAdapter.getItem(position);
            if (item instanceof String) {
                hidePopup(true);
                setProfileSelection(position);
            } else if (item instanceof ProfileAdapterExtra) {
                hidePopup(false);
                performExtraAction((ProfileAdapterExtra) item);
            }
        });

        mPopupWindow = new PopupWindow(mListView, MATCH_PARENT, getContext().getResources().getDimensionPixelOffset(R.dimen._184sdp));
        mPopupWindow.setElevation(5);
        mPopupWindow.setClippingEnabled(false);

        // Block clicking outside of the popup window
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setTouchInterceptor((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                mPopupWindow.dismiss();
                return true;
            }
            return false;
        });

        // Custom animation, nice slide in
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPopupAnimation = new Slide(Gravity.BOTTOM);
            mPopupWindow.setEnterTransition((Transition) mPopupAnimation);
            mPopupWindow.setExitTransition((Transition) mPopupAnimation);
        }
    }

    /**
     * Hides the popup window.
     *
     * @param animate Whether to animate the dismissal.
     */
    private void hidePopup(boolean animate) {
        if (mPopupWindow == null) return;
        if (!animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPopupWindow.setEnterTransition(null);
            mPopupWindow.setExitTransition(null);
            mPopupWindow.dismiss();
            mPopupWindow.setEnterTransition((Transition) mPopupAnimation);
            mPopupWindow.setExitTransition((Transition) mPopupAnimation);
        } else {
            mPopupWindow.dismiss();
        }
    }

    public ProfileAdapter getProfileAdapter() {
        return mProfileAdapter;
    }
}
