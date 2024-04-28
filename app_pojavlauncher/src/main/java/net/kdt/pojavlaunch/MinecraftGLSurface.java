package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.MainActivity.touchCharInput;
import static net.kdt.pojavlaunch.utils.MCOptionUtils.getMcScale;
import static org.lwjgl.glfw.CallbackBridge.sendMouseButton;
import static org.lwjgl.glfw.CallbackBridge.windowHeight;
import static org.lwjgl.glfw.CallbackBridge.windowWidth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.gamepad.Gamepad;
import net.kdt.pojavlaunch.customcontrols.mouse.AbstractTouchpad;
import net.kdt.pojavlaunch.customcontrols.mouse.InGUIEventProcessor;
import net.kdt.pojavlaunch.customcontrols.mouse.InGameEventProcessor;
import net.kdt.pojavlaunch.customcontrols.mouse.TouchEventProcessor;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.JREUtils;
import net.kdt.pojavlaunch.utils.MCOptionUtils;

import fr.spse.gamepad_remapper.RemapperManager;
import fr.spse.gamepad_remapper.RemapperView;

/**
 * Class dealing with showing minecraft surface and taking inputs to dispatch them to minecraft
 */
public class MinecraftGLSurface extends View implements GrabListener {

    //... (rest of the code remains the same)

    /**
     * Inner class for SurfaceView-based implementation
     */
    private class SurfaceViewImplementation implements SurfaceHolder.Callback {
        private SurfaceView surfaceView;

        public SurfaceViewImplementation(SurfaceView surfaceView) {
            this.surfaceView = surfaceView;
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            try (Surface surface = surfaceView.getHolder().getSurface()) {
                realStart(surface);
            } catch (Exception e) {
                Tools.showError(getContext(), e, true);
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            refreshSize();
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            // Do nothing
        }
    }

    /**
     * Inner class for TextureView-based implementation
     */
    private class TextureViewImplementation implements TextureView.SurfaceTextureListener {
        private TextureView textureView;

        public TextureViewImplementation(TextureView textureView) {
            this.textureView = textureView;
        }

        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            try (Surface tSurface = new Surface(surface)) {
                realStart(tSurface);
            } catch (Exception e) {
                Tools.showError(getContext(), e, true);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            refreshSize();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            // Do nothing
        }
    }

    /**
     * Initializes the view and all its settings
     * @param isAlreadyRunning set to true to tell the view that the game is already running
     *                         (only updates the window without calling the start listener)
     * @param touchpad the optional cursor-emulating touchpad, used for touch event processing
     *                 when the cursor is not grabbed
     */
    public void start(boolean isAlreadyRunning, AbstractTouchpad touchpad) {
        //... (rest of the code remains the same)

        if (LauncherPreferences.PREF_USE_ALTERNATE_SURFACE) {
            SurfaceView surfaceView = new SurfaceView(getContext());
            surfaceView.getHolder().addCallback(new SurfaceViewImplementation(surfaceView));
            mSurface = surfaceView;

            ((ViewGroup) getParent()).addView(surfaceView);
        } else {
            TextureView textureView = new TextureView(getContext());
            textureView.setOpaque(true);
            textureView.setAlpha(1.0f);
            textureView.setSurfaceTextureListener(new TextureViewImplementation(textureView));
            mSurface = textureView;

            ((ViewGroup) getParent()).addView(textureView);
        }

        //... (rest of the code remains the same)
    }

    //... (rest of the code remains the same)
}
