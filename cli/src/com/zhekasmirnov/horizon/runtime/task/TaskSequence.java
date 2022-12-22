package com.zhekasmirnov.horizon.runtime.task;

import java.util.ArrayList;
import java.util.List;

public class TaskSequence {
    private static int nextId;
    private final Object lock;
    private int sequenceId;
    private List<Task> allTasks = new ArrayList<>();
    private List<TaskSequence> subSequences = new ArrayList<>();

    public static abstract class AnonymousTask extends Task {
        @Override
        public Object getLock() {
            return null;
        }
    }

    public TaskSequence(Object obj, Object... objArr) {
        int i = nextId;
        nextId = i + 1;
        this.sequenceId = i;
        this.lock = obj;
        for (Object obj2 : objArr) {
            if (obj2 instanceof Runnable) {
                addTask((Runnable) obj2);
            } else if (obj2 instanceof TaskSequence) {
                TaskSequence taskSequence = (TaskSequence) obj2;
                this.subSequences.add(taskSequence);
                for (Task task : taskSequence.allTasks) {
                    addTask(task);
                }
            }
        }
    }

    public List<Task> getAllTasks() {
        return this.allTasks;
    }

    public void addTask(final Runnable runnable) {
        this.allTasks.add(new Task(this.sequenceId) {
            @Override
            public Object getLock() {
                return TaskSequence.this.lock;
            }

            @Override
            public String getDescription() {
                Runnable runnable2 = runnable;
                if (runnable2 instanceof Task) {
                    return ((Task) runnable2).getDescription();
                }
                return null;
            }

            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    public Object getLock() {
        return this.lock;
    }

    public int getSequenceId() {
        return this.sequenceId;
    }

    public List<TaskSequence> getSubSequencies() {
        return this.subSequences;
    }
}
