package net.kdt.pojavlaunch.customcontrols.mouse;

import android.os.Handler;

/**
 * This class implements an abstract "validator gesture", meant as a base for implementation of
 * more complex gestures with finger position tracking and such.
 */
public abstract class ValidatorGesture implements Runnable {
    private final Handler handler;
    private boolean gestureActive;
    private final int requiredDuration;

    /**
     * @param handler the Handler that will be used for calling back the checkAndTrigger() method.
     *                This Handler should run on the same thread as the callee of submit()/cancel()
     * @param requiredDuration the duration after which the class will call checkAndTrigger().
     */
    public ValidatorGesture(Handler handler, int requiredDuration) {
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null");
        }
        this.handler = handler;
        this.requiredDuration = requiredDuration;
    }

    /**
     * Submit the gesture, starting the timer and marking this gesture as "active".
     * If the gesture was already active, this call will be ignored
     * @return true if the gesture was submitted, false if the call was ignored
     */
    public final boolean submit() {
        if (gestureActive) {
            return false;
        }
        handler.postDelayed(this, requiredDuration);
        gestureActive = true;
        return true;
    }

    /**
     * Cancel the gesture, stopping the timer and marking this gesture as "inactive".
     * If the gesture was already inactive, this call will be ignored.
     * @param isSwitching true if this gesture was cancelled due to user interaction (the user let go of the finger)
     *                    false if this gesture is cancelled due a request from the programmer or the OS.
     *                    Note that returning false from checkAndTrigger() counts as user interaction.
     */
    public final void cancel(boolean isSwitching) {
        if (!gestureActive) {
            return;
        }
        handler.removeCallbacks(this);
        onGestureCancelled(isSwitching);
        gestureActive = false;
    }

    @Override
    public final void run() {
        if (!checkAndTrigger()) {
            gestureActive = false;
            onGestureCancelled(false);
        }
    }

    /**
     * This method will be called after requiredDuration milliseconds, if the gesture was not cancelled.
     * @return false if you want to mark this gesture as "inactive"
     *         true otherwise
     */
    public abstract boolean checkAndTrigger();

    /**
     * This method will be called if the gesture was cancelled using the cancel() method or by returning false
     * from checkAndTrigger().
     * @param isSwitching true if this gesture was cancelled due to user interaction (the user let go of the finger)
     *                    false if this gesture is cancelled due a request from the programmer or the OS.
     */
    public abstract void onGestureCancelled(boolean isSwitching);
}
