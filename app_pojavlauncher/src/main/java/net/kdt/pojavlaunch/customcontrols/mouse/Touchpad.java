package net.kdt.pojavlaunch.customcontrols.mouse;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import net.kdt.pojavlaunch.GrabListener;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import org.lwjgl.glfw.CallbackBridge;

public class Touchpad extends View implements GrabListener, AbstractTouchpad {

    private static final float SCALE_FACTOR = DEFAULT_PREF.getInt("resolutionRatio", 100) / 100f;

    private boolean mDisplayState;
    private Drawable mMousePointerDrawable;
    private float mMouseX, mMouseY;

    public Touchpad(@NonNull Context context) {
        this(context, null);
    }

    public Touchpad(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mMousePointerDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_mouse_pointer, getContext().getTheme());
        assert mMousePointerDrawable != null;
        mMousePointerDrawable.setBounds(
                0, 0,
                (int) (36 / 100f * LauncherPreferences.PREF_MOUSESCALE),
                (int) (54 / 100f * LauncherPreferences.PREF_MOUSESCALE)
        );
        setFocusable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDefaultFocusHighlightEnabled(false);
        }

        disable();
        mDisplayState = false;

        setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                CallbackBridge.setGrabbing(true);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                CallbackBridge.setGrabbing(false);
            }
            return true;
        });
    }

    public void enable() {
        setVisibility(VISIBLE);
        placeMouseAt(currentDisplayMetrics.widthPixels / 2f, currentDisplayMetrics.heightPixels / 2f);
    }

    public void disable() {
        setVisibility(GONE);
        mMouseX = 0;
        mMouseY = 0;
    }

    public boolean switchState() {
        mDisplayState = !mDisplayState;
        if (!CallbackBridge.isGrabbing()) {
            if (mDisplayState) enable();
            else disable();
        }
        return mDisplayState;
    }

    public void placeMouseAt(float x, float y) {
        mMouseX = x;
        mMouseY = y;
        updateMousePosition();
    }

    private void sendMousePosition() {
        CallbackBridge.sendCursorPos((mMouseX * SCALE_FACTOR), (mMouseY * SCALE_FACTOR));
    }

    private void updateMousePosition() {
        sendMousePosition();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mMouseX, mMouseY);
        mMousePointerDrawable.draw(canvas);
    }

    @Override
    public void onGrabState(boolean isGrabbing) {
        updateGrabState(isGrabbing);
    }

    private void updateGrabState(boolean isGrabbing) {
        if (!isGrabbing) {
            if (mDisplayState && getVisibility() != VISIBLE) enable();
            if (!mDisplayState && getVisibility() == VISIBLE) disable();
        } else {
            if (getVisibility() != View.GONE) disable();
        }
    }

    @Override
    public boolean getDisplayState() {
        return mDisplayState;
    }

    @Override
    public void applyMotionVector(float[] vector) {
        mMouseX = Math.max(0, Math.min(currentDisplayMetrics.widthPixels, mMouseX + vector[0] * LauncherPreferences.PREF_MOUSESPEED));
        mMouseY = Math.max(0, Math.min(currentDisplayMetrics.heightPixels, mMouseY + vector[1] * LauncherPreferences.PREF_MOUSESPEED));
        updateMousePosition();
    }
}
