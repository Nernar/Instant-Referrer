package com.zhekasmirnov.innercore.mod.executable;

import com.zhekasmirnov.innercore.api.mod.API;

public class CompilerConfig {
    private API apiInstance;
    private int optimizationLevel = -1;
    private String name = "Unknown Executable";
    private String modName = null;
    public boolean isLibrary = false;

    public CompilerConfig(API apiInstance) {
        this.apiInstance = apiInstance;
    }

    public API getApiInstance() {
        return this.apiInstance;
    }

    public int getOptimizationLevel() {
        return this.optimizationLevel;
    }

    public void setOptimizationLevel(int level) {
        this.optimizationLevel = level;
    }

    public String getName() {
        return this.name;
    }

    public void setModName(String modName) {
        this.modName = modName;
    }

    public String getFullName() {
        if (this.modName != null) {
            return String.valueOf(this.modName) + "$" + this.name;
        }
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
