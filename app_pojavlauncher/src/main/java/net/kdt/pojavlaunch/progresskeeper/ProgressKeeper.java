package net.kdt.pojavlaunch.progresskeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ProgressKeeper {
    private static final HashMap<String, ProgressState> progressStates = new HashMap<>();
    private static final HashMap<String, List<ProgressListener>> progressListeners = new HashMap<>();
    private static final List<TaskCountListener> taskCountListeners = new ArrayList<>();

    public static synchronized void submitProgress(String progressRecord, int progress, int resid, Object... va) {
        ProgressState progressState = progressStates.get(progressRecord);
        boolean shouldCallStarted = progressState == null;
        boolean shouldCallEnded = resid == -1 && progress == -1;

        if (shouldCallEnded) {
            shouldCallStarted = false;
            progressStates.remove(progressRecord);
            updateTaskCount();
        } else if (shouldCallStarted) {
            progressStates.put(progressRecord, new ProgressState());
            updateTaskCount();
        }

        if (progressState != null) {
            progressState.progress = progress;
            progressState.resid = resid;
            progressState.varArg = va;
        }

        List<ProgressListener> progressListenersList = progressListeners.get(progressRecord);
        if (progressListenersList != null) {
            for (ProgressListener listener : progressListenersList) {
                if (shouldCallStarted) {
                    listener.onProgressStarted();
                } else if (shouldCallEnded) {
                    listener.onProgressEnded();
                } else {
                    listener.onProgressUpdated(progress, resid, va);
                }
            }
        }
    }

    private static synchronized void updateTaskCount() {
        int count = progressStates.size();
        for (TaskCountListener listener : taskCountListeners) {
            listener.onUpdateTaskCount(count);
        }
    }

    public static synchronized void addListener(String progressRecord, ProgressListener listener) {
        ProgressState state = progressStates.get(progressRecord);
        if (state != null) {
            if (state.resid != -1 || state.progress != -1) {
                listener.onProgressStarted();
                listener.onProgressUpdated(state.progress, state.resid, state.varArg);
            } else {
                listener.onProgressEnded();
            }
        }

        List<ProgressListener> listenerList = progressListeners.get(progressRecord);
        if (listenerList == null) {
            progressListeners.put(progressRecord, new ArrayList<>());
        }
        progressListeners.get(progressRecord).add(listener);
    }

    public static synchronized void removeListener(String progressRecord, ProgressListener listener) {
        List<ProgressListener> listenerList = progressListeners.get(progressRecord);
        if (listenerList != null) {
            listenerList.remove(listener);
        }
    }

    public static synchronized void addTaskCountListener(TaskCountListener listener) {
        addTaskCountListener(listener, true);
    }

    public static synchronized void addTaskCountListener(TaskCountListener listener, boolean runUpdate) {
        if (runUpdate) {
            updateTaskCount();
        }
        if (!taskCountListeners.contains(listener)) {
            taskCountListeners.add(listener);
        }
    }

    public static synchronized void removeTaskCountListener(TaskCountListener listener) {
        taskCountListeners.remove(listener);
    }

    /**
     * Waits until all tasks are done and runs the runnable, or if there were no pending process remaining
     * The runnable runs from the thread that updated the task count last, and it might be the UI thread,
     * so don't put long running processes in it
     * @param runnable the runnable to run when no tasks are remaining
     */
    public static void waitUntilDone(Runnable runnable) {
        if (getTaskCount() == 0) {
            runnable.run();
            return;
        }

        TaskCountListener listener = new TaskCountListener() {
            @Override
            public void onUpdateTaskCount(int taskCount) {
                if (taskCount == 0) {
                    runnable.run();
                }
                removeTaskCountListener(this);
            }
        };

        addTaskCountListener(listener, false);
    }

    public static synchronized int getTaskCount() {
        return progressStates.size();
    }

    public static boolean hasOngoingTasks() {
        return getTaskCount() > 0;
    }

    public static ProgressState getProgressState(String progressRecord) {
        return progressStates.get(progressRecord);
    }

    public static List<ProgressState> getAllProgressStates() {
        return new ArrayList<>(progressStates.values());
    }

    public static class ProgressState {
        public int progress;
        public int resid;
        public Object[] varArg;

        public ProgressState() {
            this.progress = -1;
            this.resid = -1;
            this.varArg = new Object[0];
        }
    }

    public static interface ProgressListener {
        void onProgressStarted();

        void onProgressUpdated(int progress, int resid, Object... varArg);

        void onProgressEnded();
    }

    public static interface TaskCountListener {
        void onUpdateTaskCount(int taskCount);
    }
}
