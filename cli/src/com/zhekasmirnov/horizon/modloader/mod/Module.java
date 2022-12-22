package com.zhekasmirnov.horizon.modloader.mod;

import java.util.HashMap;

public class Module {
    private static HashMap<Long, Module> moduleByHandle = new HashMap<>();
    private final long handle;

    private static native String nativeGetNameID(long j);

    private static native long nativeGetParent(long j);

    private static native String nativeGetType(long j);

    private static native boolean nativeIsInitialized(long j);

    private static native void nativeOnEvent(long j, String str);

    private Module(long j) {
        moduleByHandle.put(Long.valueOf(j), this);
        this.handle = j;
    }

    public static Module getInstance(long j) {
        Module module = moduleByHandle.get(Long.valueOf(j));
        return module != null ? module : new Module(j);
    }

    public Module getParent() {
        return getInstance(nativeGetParent(this.handle));
    }

    public String getNameID() {
        return nativeGetNameID(this.handle);
    }

    public String getType() {
        return nativeGetType(this.handle);
    }

    public boolean isInitialized() {
        return nativeIsInitialized(this.handle);
    }

    public void onEvent(String str) {
        nativeOnEvent(this.handle, str);
    }

    public boolean isMod() {
        return "MOD".equals(getType());
    }
}
