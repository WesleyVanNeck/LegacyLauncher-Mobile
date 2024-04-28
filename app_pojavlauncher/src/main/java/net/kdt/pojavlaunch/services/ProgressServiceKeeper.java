package net.kdt.pojavlaunch.services;

import android.content.Context;

import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

public abstract class ProgressServiceKeeper implements TaskCountListener {
    // Prevent direct instantiation of this class
    private ProgressServiceKeeper() {}

    // Encourage the use of subclasses by making this class abstract
    public abstract int getTaskCount();

    private final Context context;

    // Constructor to initialize the context
    public ProgressServiceKeeper(Context ctx) {
        this.context = ctx;
    }

    // Override the onUpdateTaskCount() method to start the ProgressService if the task count is greater than zero
    @Override
    public void onUpdateTaskCount(int taskCount) {
        if (context != null && taskCount > 0) {
            ProgressService.startService(context);
        }
    }
}
