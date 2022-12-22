package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.innercore.api.log.ICLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainThreadQueue {
    public static final MainThreadQueue localThread = new MainThreadQueue();
    public static final MainThreadQueue serverThread = new MainThreadQueue();
    private boolean queueLocked = false;
    private final ArrayList<Runnable> queue = new ArrayList<>();
    private final ArrayList<Runnable> nextTickQueue = new ArrayList<>();
    private final Map<Runnable, Integer> delayedQueue = new HashMap<>();

    public void clearQueue() {
        synchronized (this.queue) {
            this.queue.clear();
        }
    }

    public void executeQueue() {
        long start = System.currentTimeMillis();
        synchronized (this.queue) {
            this.queueLocked = true;
            Iterator<Runnable> iterator = this.queue.iterator();
            while (iterator.hasNext()) {
                iterator.next().run();
            }
            this.queueLocked = false;
            this.queue.clear();
            this.queue.addAll(this.nextTickQueue);
            this.nextTickQueue.clear();
            synchronized (this.delayedQueue) {
                Iterator<Map.Entry<Runnable, Integer>> iterator2 = this.delayedQueue.entrySet().iterator();
                while (iterator2.hasNext()) {
                    Map.Entry<Runnable, Integer> entry = iterator2.next();
                    int value = entry.getValue().intValue();
                    int value2 = value - 1;
                    if (value <= 0) {
                        entry.getKey().run();
                        iterator2.remove();
                    }
                    entry.setValue(Integer.valueOf(value2));
                }
                long end = System.currentTimeMillis();
                if (start - end > 8) {
                    ICLog.i("WARNING", "main thread tick taking too long: " + (start - end) + " ms");
                }
            }
        }
    }

    public void enqueue(Runnable action) {
        synchronized (this.queue) {
            if (this.queueLocked) {
                this.nextTickQueue.add(action);
            } else {
                this.queue.add(action);
            }
        }
    }

    public void enqueueDelayed(int ticks, Runnable action) {
        synchronized (this.delayedQueue) {
            this.delayedQueue.put(action, Integer.valueOf(ticks));
        }
    }
}
