package net.kdt.pojavlaunch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AWTCanvasView extends TextureView implements TextureView.SurfaceTextureListener, Runnable {

    public static final int AWT_CANVAS_WIDTH = 720;
    public static final int AWT_CANVAS_HEIGHT = 600;
    private static final int MAX_SIZE = 100;
    private static final double NANOS = 1_000_000_000.0;
    private static final String TAG = "AWTCanvasView";

    private boolean isDestroyed = false;
    private final TextPaint fpsPaint;

    // Temporary count fps
    private final List<Long> times = Collections.synchronizedList(new LinkedList<Long>() {{
        add(System.nanoTime());
    }});

    public AWTCanvasView(Context context) {
        this(context, null);
    }

    public AWTCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        fpsPaint = new TextPaint();
        fpsPaint.setColor(Color.WHITE);
        fpsPaint.setTextSize(20);

        setSurfaceTextureListener(this);

        post(this::refreshSize);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        setDefaultBufferSize(AWT_CANVAS_WIDTH, AWT_CANVAS_HEIGHT);
        isDestroyed = false;
        new Thread(this, "AndroidAWTRenderer").start();
    }

    @Override
    public void onSurfaceTextureDestroyed(SurfaceTexture texture) {
        isDestroyed = true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        setDefaultBufferSize(AWT_CANVAS_WIDTH, AWT_CANVAS_HEIGHT);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        // No-op
    }

    @Override
    public void run() {
        Canvas canvas;
        Surface surface = new Surface(getSurfaceTexture());
        Bitmap rgbArrayBitmap = Bitmap.createBitmap(AWT_CANVAS_WIDTH, AWT_CANVAS_HEIGHT, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();

        try {
            while (!isDestroyed && surface.isValid()) {
                canvas = surface.lockCanvas(null);
                if (canvas == null) {
                    Log.e(TAG, "Canvas is null");
                    continue;
                }

                canvas.drawRGB(0, 0, 0);

                int[] rgbArray = JREUtils.renderAWTScreenFrame(/* canvas, mWidth, mHeight */);
                boolean isDrawing = rgbArray != null;

                if (rgbArray != null) {
                    canvas.save();
                    rgbArrayBitmap.setPixels(rgbArray, 0, AWT_CANVAS_WIDTH, 0, 0, AWT_CANVAS_WIDTH, AWT_CANVAS_HEIGHT);
                    canvas.drawBitmap(rgbArrayBitmap, 0, 0, paint);
                    canvas.restore();
                }

                String fpsText = "FPS: " + String.format("%.1f", fps()) + ", drawing=" + isDrawing;
                canvas.drawText(fpsText, 0, 20, fpsPaint);

                surface.unlockCanvasAndPost(canvas);
            }
        } catch (Throwable throwable) {
            Tools.showError(getContext(), throwable);
        } finally {
            rgbArrayBitmap.recycle();
            surface.release();
        }
    }

    /**
     * Calculates and returns frames per second
     */
    private double fps() {
        long lastTime = System.nanoTime();
        double difference = 0;
        synchronized (times) {
            if (!times.isEmpty()) {
                difference = (lastTime - times.getFirst()) / NANOS;
                times.addLast(lastTime);
            }
            int size = times.size();
            if (size > MAX_SIZE) {
                times.removeFirst();
            }
        }
        return difference > 0 ? size / difference : 0.0;
    }

    /**
     * Make the view fit the proper aspect ratio of the surface
     */
    private void refreshSize() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();

        if (getHeight() < getWidth()) {
            layoutParams.width = AWT_CANVAS_WIDTH * getHeight() / AWT_CANVAS_HEIGHT;
        } else {
            layoutParams.height = AWT_CANVAS_HEIGHT * getWidth() / AWT_CANVAS_WIDTH;
        }

        setLayoutParams(layoutParams);
    }
}
