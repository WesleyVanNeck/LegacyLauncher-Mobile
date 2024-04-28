package net.kdt.pojavlaunch;

import android.view.GestureDetector;

public record SingleTapConfirm() implements GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }
}
