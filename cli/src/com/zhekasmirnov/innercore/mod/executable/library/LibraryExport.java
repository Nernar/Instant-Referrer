package com.zhekasmirnov.innercore.mod.executable.library;

public class LibraryExport {
    public final String name;
    private int targetVersion = -1;
    public final Object value;

    public LibraryExport(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public void setTargetVersion(int targetVersion) {
        this.targetVersion = targetVersion;
    }

    public int getTargetVersion() {
        return this.targetVersion;
    }

    public boolean hasTargetVersion() {
        return this.targetVersion != -1;
    }
}
