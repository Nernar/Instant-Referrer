package com.zhekasmirnov.horizon.runtime.task;

public abstract class Task implements Runnable {
    private int sequenceId;

    public abstract Object getLock();

    public String getDescription() {
        return null;
    }

    public int getQueuePriority() {
        return 0;
    }

    public int getThreadPriority() {
        return 5;
    }

    public Task() {
        this.sequenceId = -1;
    }

    public Task(int i) {
        this.sequenceId = -1;
        this.sequenceId = i;
    }

    public int getSequenceId() {
        return this.sequenceId;
    }
}
