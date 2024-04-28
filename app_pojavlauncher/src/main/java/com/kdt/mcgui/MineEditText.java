package com.kdt.mcgui;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Color;
import androidx.appcompat.widget.AppCompatEditText;

public class MineEditText extends AppCompatEditText {

    public MineEditText(Context context) {
        super(context);
        init();
    }

    public MineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MineEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.parseColor("#131313"));
        setPadding(5, 5, 5, 5);
    }
}
