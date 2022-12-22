package com.zhekasmirnov.horizon.activity.main;

import com.zhekasmirnov.horizon.runtime.task.TaskManager;

public class ProgressBarHolder implements TaskManager.StateCallback {
    @SuppressWarnings("unused")
    private final Object context;
    @SuppressWarnings("unused")
    private final Object label;
    @SuppressWarnings("unused")
    private final Object progressBar;

    public ProgressBarHolder(Object activity, Object view, Object view2) {
        this.context = activity;
        this.progressBar = view;
        this.label = view2;
    }

    @Override
    public void onStateUpdated(TaskManager taskManager, TaskManager.ThreadHolder threadHolder) {
        // TODO: taskManager.getNumRunningThreads() > 0;
        taskManager.getFormattedTaskDescriptions(1);
    }
}
