package com.zhekasmirnov.horizon.modloader.repo.storage;

import java.io.File;

public class TemporaryStorage {
    private final File directory;
    @SuppressWarnings("unused")
    private final long initializationTime = System.currentTimeMillis();

    public TemporaryStorage(File file) {
        this.directory = file;
    }

    private static String hash(String str) {
        return "T#" + ((str.hashCode() << 32) | ("hash prefix" + str).hashCode());
    }

    public File allocate(String str) {
        File file = new File(this.directory, hash(str));
        file.mkdir();
        file.setLastModified(System.currentTimeMillis());
        return file;
    }

    public boolean recycle(String str) {
        return new File(this.directory, hash(str)).delete();
    }
}
