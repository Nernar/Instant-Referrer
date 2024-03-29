package com.zhekasmirnov.horizon.modloader.resource.runtime;

public interface RuntimeResourceHandler {
    String getResourceName();

    String getResourcePath();

    void handle(RuntimeResource runtimeResource);
}
