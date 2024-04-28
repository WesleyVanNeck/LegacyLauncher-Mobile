package net.kdt.pojavlaunch.customcontrols.handleview;

import android.view.View;

import net.kdt.pojavlaunch.customcontrols.buttons.ControlInterface;

/**
 * Interface defining the behavior of action buttons.
 */
public interface ActionButtonInterface extends View.OnClickListener {

    /**
     * This method should be implemented by concrete classes to initialize any required state.
     */
    void init();

    /**
     * Called when the button should be made aware of the current target.
     *
     * @param view the target view
     */
    void setFollowedView(ControlInterface view);

    /**
     * Called when the button action should be executed on the target.
     */
    void onClick();

    /**
     * Whether the button should be shown, given the current contextual information that it has.
     *
     * @return true if the button should be visible, false otherwise
     */
    boolean shouldBeVisible();

    /**
     * Wrapper to remove the arg.
     */
    @Override
    default void onClick(View v) {
        onClick();
    }
}
