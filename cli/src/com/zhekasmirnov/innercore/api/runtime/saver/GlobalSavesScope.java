package com.zhekasmirnov.innercore.api.runtime.saver;

public abstract class GlobalSavesScope {
    private String name;

    public abstract void read(Object obj);

    public abstract Object save();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
