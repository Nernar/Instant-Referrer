package com.zhekasmirnov.apparatus.job;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

public class InstantJobExecutor implements JobExecutor {
    private final String name;

    public InstantJobExecutor(String name) {
        this.name = name;
    }

    public InstantJobExecutor() {
        this("Unknown Instant Executor");
    }

    @Override
    public void add(Job job) {
        try {
            job.run();
        } catch (Throwable th) {
            Logger.error("NON-FATAL ERROR", "Main thread job executor \"" + this.name + "\" failed to execute job with pending exception.");
        }
    }
}
