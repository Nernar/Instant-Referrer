package com.zhekasmirnov.horizon.launcher;

import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.modloader.ExecutionDirectory;
import com.zhekasmirnov.horizon.modloader.repo.storage.TemporaryStorage;
import com.zhekasmirnov.horizon.runtime.task.TaskManager;
import java.io.File;

public class ContextHolder {
    private Object context;
    private ExecutionDirectory executionDir;
    private TaskManager taskManager;
    private TemporaryStorage temporaryStorage;

    public ContextHolder(Object activity, ExecutionDirectory executionDirectory, TemporaryStorage temporaryStorage, TaskManager taskManager) {
        this.context = activity;
        this.executionDir = executionDirectory;
        this.temporaryStorage = temporaryStorage;
        this.taskManager = taskManager;
    }

    public ContextHolder(Object activity) {
        this(activity, new ExecutionDirectory(Environment.getPackExecutionDir(activity), true), new TemporaryStorage(new File(Environment.getDataDirFile(activity), "tmploc")), new TaskManager());
    }

    public Object getContext() {
        return this.context;
    }

    public TaskManager getTaskManager() {
        return this.taskManager;
    }

    public ExecutionDirectory getExecutionDir() {
        return this.executionDir;
    }

    public TemporaryStorage getTemporaryStorage() {
        return this.temporaryStorage;
    }
}
