package com.zhekasmirnov.horizon.compiler.exceptions;

public class NotEnoughCacheException extends Exception {
    private final long cacheAvailSize;
    private final int needMem;

    public NotEnoughCacheException(int i, long j) {
        this.needMem = i;
        this.cacheAvailSize = j;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public long getCacheAvailSize() {
        return this.cacheAvailSize;
    }

    public int getNeedMem() {
        return this.needMem;
    }
}
