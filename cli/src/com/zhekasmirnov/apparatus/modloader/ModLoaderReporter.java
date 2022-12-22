package com.zhekasmirnov.apparatus.modloader;

public interface ModLoaderReporter {
    void reportError(String str, Throwable th);
}
