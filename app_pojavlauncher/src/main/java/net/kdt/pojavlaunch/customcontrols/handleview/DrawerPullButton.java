package net.kdt.pojavlaunch.customcontrols.handleview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import net.kdt.pojavlaunch.R;

public class DrawerPullButton extends View {
    private Paint mPaint;
    private VectorDrawableCompat mDrawable;

    public DrawerPullButton(Context context) {
        super(context);
        init(context);
    }

    public DrawerPullButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_sharp_settings_24, null);
        setAlpha(0.33f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawable.setBounds(0, 0, h, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(Color.BLACK);
        canvas.drawArc(0, 0, getWidth(), getHeight(), 0, 180, true, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.save();
        canvas.translate((getWidth() - getHeight()) / 2f, 0);
        mDrawable.draw(canvas);
        canvas.restore();
    }
}
