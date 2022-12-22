package com.zhekasmirnov.horizon.modloader.library;

import com.zhekasmirnov.horizon.modloader.mod.Module;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Library {
    private static HashMap<String, Library> loadedLibraries = new HashMap<>();
    private final long handle;
    private List<Module> modules = new ArrayList<>();
    private final int result;

    private native long[] nativeGetModules(long j);

    private native int nativeGetResult(long j);

    private static native long nativeLoad(String str, boolean z);

    public static Library load(String str, boolean z) {
        if (loadedLibraries.containsKey(str)) {
            return loadedLibraries.get(str);
        }
        String name = new File(str).getName();
        if (name.startsWith("lib") && name.endsWith(".so")) {
            System.loadLibrary(name.substring(3, name.length() - 3));
        } else {
            System.load(str);
        }
        long nativeLoad = nativeLoad(str, z);
        Library library = nativeLoad != 0 ? new Library(nativeLoad) : null;
        loadedLibraries.put(str, library);
        return library;
    }

    private Library(long j) {
        this.handle = j;
        this.result = nativeGetResult(j);
        refreshModuleList();
    }

    public int getResult() {
        return this.result;
    }

    public void refreshModuleList() {
        this.modules.clear();
        for (long j : nativeGetModules(this.handle)) {
            this.modules.add(Module.getInstance(j));
        }
    }

    public List<Module> getModules() {
        return this.modules;
    }
}
