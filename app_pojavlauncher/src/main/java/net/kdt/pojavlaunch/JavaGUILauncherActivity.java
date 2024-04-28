package net.kdt.pojavlaunch;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kdt.LoggerView;

import net.kdt.pojavlaunch.customcontrols.keyboard.AwtCharSender;
import net.kdt.pojavlaunch.customcontrols.keyboard.TouchCharInput;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.JREUtils;
import net.kdt.pojavlaunch.utils.MathUtils;
import net.kdt.pojavlaunch.utils.Tools;

import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.CallbackBridge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JavaGUILauncherActivity extends AppCompatActivity implements View.OnTouchListener {

    private AWTCanvasView mTextureView;
    private LoggerView mLoggerView;
    private TouchCharInput mTouchCharInput;

    private LinearLayout mTouchPad;
    private ImageView mMousePointerImageView;
    private GestureDetector mGestureDetector;

    private boolean mIsVirtualMouseEnabled;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_gui_launcher);

        initViews();
        initListeners();
        initData();
    }

    private void initViews() {
        mTextureView = findViewById(R.id.installmod_surfaceview);
        mLoggerView = findViewById(R.id.launcherLoggerView);
        mTouchCharInput = findViewById(R.id.awt_touch_char);
        mTouchPad = findViewById(R.id.main_touchpad);
        mMousePointerImageView = findViewById(R.id.main_mouse_pointer);

        mGestureDetector = new GestureDetector(this, new SingleTapConfirm());
    }

    private void initListeners() {
        mTouchPad.setOnTouchListener(new TouchPadListener());
        mTextureView.setOnTouchListener(new TextureViewListener());

        findViewById(R.id.installmod_mouse_pri).setOnTouchListener(this);
        findViewById(R.id.installmod_mouse_sec).setOnTouchListener(this);
        findViewById(R.id.installmod_window_moveup).setOnTouchListener(this);
        findViewById(R.id.installmod_window_movedown).setOnTouchListener(this);
        findViewById(R.id.installmod_window_moveleft).setOnTouchListener(this);
        findViewById(R.id.installmod_window_moveright).setOnTouchListener(this);
    }

    private void initData() {
        try {
            File latestLogFile = new File(Tools.DIR_GAME_HOME, "latestlog.txt");
            if (!latestLogFile.exists() && !latestLogFile.createNewFile()) {
                throw new IOException("Failed to create a new log file");
            }
            Logger.begin(latestLogFile.getAbsolutePath());
        } catch (IOException e) {
            Tools.showError(this, e, true);
        }

        MainActivity.GLOBAL_CLIPBOARD = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mTouchCharInput.setCharacterSender(new AwtCharSender());

        mMousePointerImageView.post(() -> {
            ViewGroup.LayoutParams params = mMousePointerImageView.getLayoutParams();
            params.width = (int) (36 / 100f * LauncherPreferences.PREF_MOUSESCALE);
            params.height = (int) (54 / 100f * LauncherPreferences.PREF_MOUSESCALE);
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        return handleTouchEvent(v, e);
    }

    private boolean handleTouchEvent(View v, MotionEvent e) {
        boolean isDown;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: // 0
            case MotionEvent.ACTION_POINTER_DOWN: // 5
                isDown = true;
                break;
            case MotionEvent.ACTION_UP: // 1
            case MotionEvent.ACTION_CANCEL: // 3
            case MotionEvent.ACTION_POINTER_UP: // 6
                isDown = false;
                break;
            default:
                return false;
        }

        switch (v.getId()) {
            case R.id.installmod_mouse_pri:
                AWTInputBridge.sendMousePress(AWTInputEvent.BUTTON1_DOWN_MASK, isDown);
                break;
            case R.id.installmod_mouse_sec:
                AWTInputBridge.sendMousePress(AWTInputEvent.BUTTON3_DOWN_MASK, isDown);
                break;
            default:
                break;
        }

        if (isDown) {
            switch (v.getId()) {
                case R.id.installmod_window_moveup:
                    AWTInputBridge.nativeMoveWindow(0, -10);
                    break;
                case R.id.installmod_window_movedown:
                    AWTInputBridge.nativeMoveWindow(0, 10);
                    break;
                case R.id.installmod_window_moveleft:
                    AWTInputBridge.nativeMoveWindow(-10, 0);
                    break;
                case R.id.installmod_window_moveright:
                    AWTInputBridge.nativeMoveWindow(10, 0);
                    break;
                default:
                    break;
            }
        }

        return true;
    }

    private class TouchPadListener implements View.OnTouchListener {

        private float prevX = 0, prevY = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mGestureDetector.onTouchEvent(event)) {
                sendScaledMousePosition(mMousePointerImageView.getX(), mMousePointerImageView.getY());
                AWTInputBridge.sendMousePress(AWTInputEvent.BUTTON1_DOWN_MASK);
                return true;
            }

            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_MOVE) {
                float x = event.getX();
                float y = event.getY();

                float mouseX = mMousePointerImageView.getX();
                float mouseY = mMousePointerImageView.getY();

                mouseX = Math.max(0, Math.min(CallbackBridge.physicalWidth, mouseX + x - prevX));
                mouseY = Math.max(0, Math.min(CallbackBridge.physicalHeight, mouseY + y - prevY));

                placeMouseAt(mouseX, mouseY);
                sendScaledMousePosition(mouseX, mouseY);
            }

            prevY = y;
            prevX = x;
            return true;
        }
    }

    private class TextureViewListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mGestureDetector.onTouchEvent(event)) {
                sendScaledMousePosition(event.getX() + mTextureView.getX(), event.getY());
                AWTInputBridge.sendMousePress(AWTInputEvent.BUTTON1_DOWN_MASK);
                return true;
            }

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_UP: // 1
                case MotionEvent.ACTION_CANCEL: // 3
                case MotionEvent.ACTION_POINTER_UP: // 6
                    break;
                case MotionEvent.ACTION_MOVE: // 2
                    sendScaledMousePosition(event.getX() + mTextureView.getX(), event.getY());
                    break;
            }
            return true;
        }
    }

    private void placeMouseAt(float x, float y) {
        mMousePointerImageView.setX(x);
        mMousePointerImageView.setY(y);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    void sendScaledMousePosition(float x, float y) {
        // Clamp positions to the borders of the usable view, then scale them
        x = MathUtils.clamp(x, mTextureView.getX(), mTextureView.getX() + mTextureView.getWidth());
        y = MathUtils.clamp(y, mTextureView.getY(), mTextureView.getY() + mTextureView.getHeight());

        AWTInputBridge.sendMousePos(
                MathUtils.map(x, mTextureView.getX(), mTextureView.getX() + mTextureView.getWidth(), 0, AWTCanvasView.AWT_CANVAS_WIDTH),
                MathUtils.map(y, mTextureView.getY(), mTextureView.getY() + mTextureView.getHeight(), 0, AWTCanvasView.AWT_CANVAS_HEIGHT)
        );
    }

    public void forceClose(View v) {
        MainActivity.dialogForceClose(this);
    }

    public void openLogOutput(View v) {
        mLoggerView.setVisibility(View.VISIBLE);
    }

    public void toggleVirtualMouse(View v) {
        mIsVirtualMouseEnabled = !mIsVirtualMouseEnabled;
        mTouchPad.setVisibility(mIsVirtualMouseEnabled ? View.VISIBLE : View.GONE);
        Toast.makeText(this,
                mIsVirtualMouseEnabled ? R.string.control_mouseon : R.string.control_mouseoff,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mTextureView.onConfigurationChanged(newConfig);
    }

    private static class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }
    }
}
