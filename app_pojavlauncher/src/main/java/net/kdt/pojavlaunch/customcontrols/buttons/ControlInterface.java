package net.kdt.pojavlaunch.customcontrols.buttons;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import net.kdt.pojavlaunch.GrabListener;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.handleview.EditControlPopup;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.MathUtils;

import org.lwjgl.glfw.CallbackBridge;

import java.util.Objects;

/**
 * An interface that injects custom behavior to a View.
 * Most of the injected behavior is editing behavior,
 * sending keys has to be implemented by sub classes.
 */
@FunctionalInterface
public interface ControlInterface extends View.OnLongClickListener, GrabListener {

    /**
     * Gets the ControlView associated with this interface.
     *
     * @return the ControlView associated with this interface
     */
    View getControlView();

    /**
     * Gets the properties associated with this interface.
     *
     * @return the properties associated with this interface
     */
    ControlData getProperties();

    /**
     * Sets the properties associated with this interface.
     *
     * @param properties the properties to set
     */
    default void setProperties(ControlData properties) {
        setProperties(properties, true);
    }

    /**
     * Sets the properties associated with this interface.
     *
     * @param properties the properties to set
     * @param changePos  whether to change the position of the associated View
     */
    void setProperties(ControlData properties, boolean changePos);

    /**
     * Removes the button presence from the CustomControl object
     * You need to use {getControlParent()} for this.
     */
    void removeButton();

    /**
     * Duplicates the data of the button and adds a view with the duplicated data
     * Relies on the ControlLayout for the implementation.
     */
    void cloneButton();

    /**
     * Sets the visibility of the associated View.
     *
     * @param isVisible whether the View should be visible
     */

