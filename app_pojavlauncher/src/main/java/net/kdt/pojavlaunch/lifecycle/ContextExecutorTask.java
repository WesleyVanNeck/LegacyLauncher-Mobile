package net.kdt.pojavlaunch.lifecycle;

import android.app.Activity;
import android.content.Context;

import javax.annotation.Nullable;

/**
 * A {@code ContextExecutorTask} is a task that can dynamically change its behavior based on the context
 * used for its execution. This can be used to implement, for example, error/finish notifications from
 * background threads that may continue to live with the Service after the activity that started them
 * has died.
 */
@FunctionalInterface
public interface ContextExecutorTask {
    /**
     * This method will be executed first if a foreground {@code Activity} that was attached to the
     * {@code ContextExecutor} is available.
     *
     * @param activity the activity
     */
    void executeWithForegroundActivity(@Nullable Activity activity);

    /**
     * This method will be executed if a foreground {@code Activity} is not available, but the app
     * is still running.
     *
     * @param context the application context
     */
    void executeWithApplicationContext(@Nullable Context context);
}

/**
 * A {@code ContextExecutor} is an object that can execute tasks based on the current context of the app.
 * It can be used to implement, for example, error/finish notifications from background threads that may
 * continue to live with the Service after the activity that started them has died.
 */
public interface ContextExecutor {
    /**
     * Executes the given {@code ContextExecutorTask} based on the current context of the app.
     *
     * @param task the task to execute
     */
    void execute(ContextExecutorTask task);
}
