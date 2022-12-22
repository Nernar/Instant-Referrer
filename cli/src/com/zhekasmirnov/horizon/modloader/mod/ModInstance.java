package com.zhekasmirnov.horizon.modloader.mod;

public class ModInstance {
    private final Module module;

    public ModInstance(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return this.module;
    }
}
