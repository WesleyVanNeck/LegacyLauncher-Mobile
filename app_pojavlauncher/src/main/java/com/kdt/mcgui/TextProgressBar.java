package com.kdt.mcgui;

import android.content.Context;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import net.kdt.pojavlaunch.R;

public class TextProgressBar extends ProgressBar {

    /**
     * The padding between the text and the progress bar.
     */
    private int mTextPadding = 0;

    /**
     * The paint used to draw the text.
     */
    private Paint mTextPaint;

    /**
     * The text to be displayed on the progress bar.
     */
    private String mText = "";

    /**
     * Constructs a new TextProgressBar instance with the given context and default style attributes.
     *
     * @param context The context.
     * @param attrs   The attribute set.
     * @param defStyleAttr The default style attribute.
     */
    public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Constructs a new TextProgressBar instance with the given context, attribute set, and default style resource.
     *
     * @param context      The context.
     * @param attrs        The attribute set.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * Constructs a new TextProgressBar instance with the given context and default style.
     *
     * @param context The context.
     */
    public TextProgressBar(Context context) {
        super(context, null, android.R.attr.progressBarStyleHorizontal);
        init();
    }

    /**
     * Constructs a new TextProgressBar instance with the given context and attribute set.
     *
     * @param context The context.
     * @param attrs   The attribute set.
     */
    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.progressBarStyleHorizontal);
        init();
    }

    /**
     * Initializes the TextProgressBar instance.
     */
    private void init() {
        setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.view_text_progressbar, null));
        setProgress(35);
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        mTextPaint.setAntiAlias(true);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Calculate the text size based on the progress bar height
        mTextPaint.setTextSize((float) ((getHeight() - getPaddingBottom() - getPaddingTop()) * 0.55));

        // Calculate the text bounds
        int textWidth = (int) mTextPaint.measureText(mText);

        // Calculate the x position of the text
        int xPos = (int) Math.max(Math.min(((getProgress() * getWidth() / (float) getMax())) + mTextPadding,
                getWidth() - textWidth - mTextPadding), mTextPadding);

        // Calculate the y position of the text
        int yPos = (getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2);

        // Draw the text centered horizontally and vertically
        canvas.drawText(mText, xPos - (textWidth / 2), yPos, mTextPaint);
    }

    /**
     * Sets the text to be displayed on the progress bar.
     *
     * @param text The text.
     */
    public final void setText(String text) {
        mText = text;
        invalidate();
    }

    /**
     * Sets the text to be displayed on the progress bar.
     *
     * @param resid The resource ID of the text.
     */
    public final void setText(@StringRes int resid) {
        setText(getContext().getResources().getText(resid).toString());
    }

    /**
     * Sets the padding between the text and the progress bar.
     *
     * @param padding The padding.
     */
    public final void setTextPadding(int padding) {
        mTextPadding = padding;
    }

    /**
     * Sets the color of the text.
     *
     * @param color The color.
     */
    public final void setTextColor(@NonNull ColorDrawable color) {
        mTextPaint.setColor(color.getColor());
        invalidate();
    }

    /**
     * Sets the color of the text.
     *
     * @param color The color.
     */
    public final void setTextColor(@NonNull Integer color) {
        mTextPaint.setColor(color);
        invalidate();
    }
}
