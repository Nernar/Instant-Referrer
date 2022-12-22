package com.zhekasmirnov.innercore.utils;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.other.PrintStacking;

public class OperationTimeLogger {
    @SuppressWarnings("unused")
    private final String logTag;
    private final boolean showToast;
    private long start;

    public OperationTimeLogger(String logTag, boolean showToast) {
        this.start = 0L;
        this.logTag = logTag;
        this.showToast = showToast;
    }

    public OperationTimeLogger(boolean showToast) {
        this("Time-Logger", showToast);
    }

    public OperationTimeLogger start() {
        this.start = System.currentTimeMillis();
        return this;
    }

    public OperationTimeLogger finish(String message) {
        String message2 = String.format(message, Double.valueOf((System.currentTimeMillis() - this.start) / 1000.0d));
        if (this.showToast) {
            PrintStacking.print(message2);
        }
        ICLog.d("Time-Logger", message2);
        return this;
    }
}
