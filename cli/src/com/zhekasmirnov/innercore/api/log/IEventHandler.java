package com.zhekasmirnov.innercore.api.log;

public interface IEventHandler {
    void onDebugEvent(String str, String str2);

    void onErrorEvent(String str, String str2, Throwable th);

    void onImportantEvent(String str, String str2);

    void onLogEvent(String str, String str2);
}
