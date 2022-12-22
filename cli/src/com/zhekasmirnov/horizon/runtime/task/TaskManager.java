package com.zhekasmirnov.horizon.runtime.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private final HashMap<Object, ThreadHolder> threads = new HashMap<>();
    private List<StateCallback> stateCallbacks = new ArrayList<>();
    private int numRunningThreads = 0;
    private List<Task> activeTasks = new ArrayList<>();

    public interface StateCallback {
        void onStateUpdated(TaskManager taskManager, ThreadHolder threadHolder);
    }

    public class ThreadHolder {
        private QueuedTask currentTask;
        private final Object lock;
        private final List<QueuedTask> queue;
        private Thread thread;

        private class QueuedTask {
            final Task task;
            final TaskWatcher watcher;

            private QueuedTask(Task task, TaskWatcher taskWatcher) {
                this.task = task;
                this.watcher = taskWatcher;
            }

            QueuedTask(ThreadHolder threadHolder, Task task, TaskWatcher taskWatcher, QueuedTask queuedTask) {
                this(task, taskWatcher);
            }

            public void run() {
                try {
                    this.task.run();
                    this.watcher.onComplete();
                } catch (Throwable th) {
                    this.watcher.onError(th);
                }
            }
        }

        private ThreadHolder(Object obj) {
            this.thread = null;
            this.currentTask = null;
            this.queue = new ArrayList<>();
            this.lock = obj;
        }

        ThreadHolder(TaskManager taskManager, Object obj, ThreadHolder threadHolder) {
            this(obj);
        }

        private void queueTask(QueuedTask queuedTask) {
            int queuePriority = queuedTask.task.getQueuePriority();
            for (int i = 0; i < this.queue.size(); i++) {
                if (queuePriority > this.queue.get(i).task.getQueuePriority()) {
                    this.queue.add(i, queuedTask);
                    return;
                }
            }
            this.queue.add(queuedTask);
        }

        void runInQueue(Task task, TaskWatcher taskWatcher) {
            synchronized (this.queue) {
                queueTask(new QueuedTask(this, task, taskWatcher, null));
                if (this.thread == null) {
                    this.thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                synchronized (ThreadHolder.this.queue) {
                                    ThreadHolder.this.currentTask = null;
                                    while (ThreadHolder.this.queue.size() > 0 && ThreadHolder.this.currentTask == null) {
                                        ThreadHolder.this.currentTask = (QueuedTask) ThreadHolder.this.queue.remove(0);
                                    }
                                    if (ThreadHolder.this.currentTask == null) {
                                        ThreadHolder.this.thread = null;
                                        TaskManager.this.registerStateChange(ThreadHolder.this.lock);
                                        return;
                                    }
                                }
                                TaskManager.this.registerStateChange(ThreadHolder.this.lock);
                                ThreadHolder.this.currentTask.run();
                            }
                        }
                    });
                    TaskManager.this.registerStateChange(this.lock);
                    this.thread.start();
                }
            }
        }

        public boolean isRunning() {
            return this.thread != null;
        }

        public Task getCurrentTask() {
            QueuedTask queuedTask = this.currentTask;
            if (queuedTask != null) {
                return queuedTask.task;
            }
            return null;
        }

        public String getCurrentTaskDescription() {
            QueuedTask queuedTask = this.currentTask;
            if (queuedTask != null) {
                return queuedTask.task.getDescription();
            }
            return null;
        }
    }

    private ThreadHolder getHolder(Object obj) {
        synchronized (this.threads) {
            ThreadHolder threadHolder = this.threads.get(obj);
            if (threadHolder == null) {
                threadHolder = new ThreadHolder(this, obj, null);
                this.threads.put(obj, threadHolder);
            }
            return threadHolder;
        }
    }

    public synchronized void registerStateChange(Object obj) {
        this.numRunningThreads = 0;
        this.activeTasks.clear();
        synchronized (this.threads) {
            for (Map.Entry<Object, ThreadHolder> entry : this.threads.entrySet()) {
                ThreadHolder value = entry.getValue();
                if (value.isRunning()) {
                    this.numRunningThreads++;
                    Task currentTask = value.getCurrentTask();
                    if (currentTask != null) {
                        this.activeTasks.add(currentTask);
                    }
                }
            }
            for (StateCallback stateCallback : this.stateCallbacks) {
                stateCallback.onStateUpdated(this, getHolder(obj));
            }
        }
    }

    public List<Task> getActiveTasks() {
        return this.activeTasks;
    }

    public int getNumRunningThreads() {
        return this.numRunningThreads;
    }

    public String getFormattedTaskDescriptions(int i) {
        StringBuilder sb = new StringBuilder();
        if (this.activeTasks.size() > i) {
            sb.append(this.activeTasks.size());
            sb.append(" tasks are running...");
        } else {
            boolean z = true;
            for (Task task : this.activeTasks) {
                String description = task.getDescription();
                if (description != null) {
                    if (z) {
                        z = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(description);
                }
            }
        }
        return sb.toString();
    }

    public void addStateCallback(StateCallback stateCallback) {
        this.stateCallbacks.add(stateCallback);
    }

    public TaskWatcher addTask(Task task, TaskWatcher.Callback callback) {
        ThreadHolder holder = getHolder(task.getLock());
        TaskWatcher taskWatcher = new TaskWatcher(task, callback);
        holder.runInQueue(task, taskWatcher);
        return taskWatcher;
    }

    public TaskWatcher addTask(Task task) {
        return addTask(task, null);
    }

    public TaskWatcher addTaskSequence(final TaskSequence taskSequence, TaskWatcher.Callback callback, final Runnable runnable) {
        TaskWatcher taskWatcher = null;
        for (Task task : taskSequence.getAllTasks()) {
            taskWatcher = addTask(task, callback);
        }
        return runnable != null ? addTask(new Task(taskSequence.getSequenceId()) {
            @Override
            public Object getLock() {
                return taskSequence.getLock();
            }

            @Override
            public void run() {
                runnable.run();
            }
        }) : taskWatcher;
    }

    public void interruptTaskSequence(TaskSequence taskSequence) {
        for (ThreadHolder threadHolder : this.threads.values()) {
            synchronized (threadHolder.queue) {
                int i = 0;
                while (true) {
                    if (i >= threadHolder.queue.size()) {
                        break;
                    }
                    boolean isSequenceTask = isSequenceTask(((ThreadHolder.QueuedTask) threadHolder.queue.get(i)).task.getSequenceId(), taskSequence);
                    if (isSequenceTask) {
                        threadHolder.queue.set(i, null);
                    }
                    i++;
                }
            }
        }
    }

    private boolean isSequenceTask(int i, TaskSequence taskSequence) {
        if (i == taskSequence.getSequenceId()) {
            return true;
        }
        for (TaskSequence taskSequence2 : taskSequence.getSubSequencies()) {
            if (isSequenceTask(i, taskSequence2)) {
                return true;
            }
        }
        return false;
    }

    public void addTaskSequence(TaskSequence taskSequence, Runnable runnable) {
        addTaskSequence(taskSequence, null, runnable);
    }

    public void addTaskSequence(TaskSequence taskSequence) {
        addTaskSequence(taskSequence, null, null);
    }
}
