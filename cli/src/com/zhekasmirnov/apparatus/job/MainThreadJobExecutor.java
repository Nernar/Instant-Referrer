package com.zhekasmirnov.apparatus.job;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;

public class MainThreadJobExecutor implements JobExecutor {
    private final String name;
    private final MainThreadQueue threadQueue;

    public MainThreadJobExecutor(MainThreadQueue threadQueue, String name) {
        this.threadQueue = threadQueue;
        this.name = name;
    }

    public MainThreadJobExecutor(MainThreadQueue threadQueue) {
        this(threadQueue, "UnnamedJobExecutor");
    }

    @Override
    public void add(final Job job) {
        this.threadQueue.enqueue(new Runnable() {
            @Override
            public final void run() {
                MainThreadJobExecutor.lambda$add$0(MainThreadJobExecutor.this, job);
            }
        });
    }

    static void lambda$add$0(MainThreadJobExecutor mainThreadJobExecutor, Job job) {
        try {
            job.run();
        } catch (Throwable th) {
            Logger.error("NON-FATAL NETWORK ERROR", "Main thread job executor \"" + mainThreadJobExecutor.name + "\" failed to execute job with pending exception.");
        }
    }
}
