package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.innercore.api.log.ICLog;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TickExecutor {
    private static final TickExecutor instance = new TickExecutor();
    private int threadCount = 0;
    private int additionalThreadPriority = 3;
    private ThreadPoolExecutor executor = null;

    public static TickExecutor getInstance() {
        return instance;
    }

    public void setAdditionalThreadPriority(int priority) {
        this.additionalThreadPriority = Math.max(1, Math.min(10, priority));
        ICLog.d("TickExecutor", "set additional thread priority to " + this.additionalThreadPriority);
    }

    public void setAdditionalThreadCount(int count) {
        int count2 = Math.max(0, Math.min(7, count));
        if (this.threadCount != count2) {
            this.threadCount = count2;
            if (this.executor != null) {
                this.executor.shutdown();
            }
            if (count2 > 0) {
                final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
                this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.threadCount, new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = defaultThreadFactory.newThread(r);
                        thread.setPriority(TickExecutor.this.additionalThreadPriority);
                        return thread;
                    }
                });
            } else {
                this.executor = null;
            }
        }
        ICLog.d("TickExecutor", "set additional thread count to " + this.threadCount);
    }

    public boolean isAvailable() {
        return this.executor != null;
    }

    public void execute(Runnable runnable) {
        if (this.executor != null) {
            this.executor.execute(runnable);
        } else {
            runnable.run();
        }
    }

    public void execute(Collection<Runnable> runnables) {
        if (this.executor != null) {
            for (Runnable runnable : runnables) {
                this.executor.execute(runnable);
            }
            return;
        }
        for (Runnable runnable2 : runnables) {
            runnable2.run();
        }
    }

    public void blockUntilExecuted() {
        Runnable runnable;
        if (this.executor != null) {
            try {
                BlockingQueue<Runnable> queue = this.executor.getQueue();
                while (queue.size() > 0 && (runnable = queue.poll(0L, TimeUnit.MILLISECONDS)) != null) {
                    runnable.run();
                }
                while (this.executor.getActiveCount() > 0) {
                    Thread.sleep(2L);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
