package com.zhekasmirnov.horizon.runtime.task;

public class TaskWatcher {
    private final Callback callback;
    private boolean isComplete = false;
    private final Task task;

    public static abstract class Callback {
        public abstract void complete(Task task);

        public boolean error(Task task, Throwable th) {
            return false;
        }
    }

    TaskWatcher(Task task, Callback callback) {
        this.task = task;
        this.callback = callback;
    }

    void onComplete() {
        this.isComplete = true;
        Callback callback = this.callback;
        if (callback != null) {
            callback.complete(this.task);
        }
    }

    void onError(Throwable th) {
        Callback callback = this.callback;
        if (callback == null || !callback.error(this.task, th)) {
            throw new RuntimeException("Exception in thread " + Thread.currentThread() + " while executing task " + this.task, th);
        }
    }

    public boolean isComplete() {
        return this.isComplete;
    }
}
