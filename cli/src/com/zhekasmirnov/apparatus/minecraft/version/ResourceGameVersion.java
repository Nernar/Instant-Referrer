package com.zhekasmirnov.apparatus.minecraft.version;

import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class ResourceGameVersion {
    private final int maxVersion;
    private final int minVersion;
    private final int targetVersion;

    public ResourceGameVersion(int minVersion, int maxVersion, int targetVersion) {
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.targetVersion = targetVersion;
    }

    public ResourceGameVersion() {
        this(-1, -1, MinecraftVersions.getCurrent().getCode());
    }

    public ResourceGameVersion(JSONObject json) {
        if (json != null) {
            int minVersion = json.optInt("minGameVersion", -1);
            int maxVersion = json.optInt("maxGameVersion", -1);
            int targetVersion = json.optInt("targetGameVersion", -1);
            if (minVersion == -1 && maxVersion == -1 && targetVersion == -1) {
                this.minVersion = -1;
                this.maxVersion = -1;
                this.targetVersion = MinecraftVersions.getCurrent().getCode();
                return;
            } else if (minVersion == -1 && maxVersion == -1) {
                this.targetVersion = targetVersion;
                this.maxVersion = targetVersion;
                this.minVersion = targetVersion;
                return;
            } else {
                if (targetVersion != -1) {
                    targetVersion = maxVersion != -1 ? Math.min(maxVersion, targetVersion) : targetVersion;
                    if (minVersion != -1) {
                        targetVersion = Math.max(minVersion, targetVersion);
                    }
                }
                this.minVersion = minVersion;
                this.maxVersion = maxVersion;
                this.targetVersion = targetVersion != -1 ? targetVersion : Math.max(minVersion, maxVersion);
                return;
            }
        }
        this.minVersion = -1;
        this.maxVersion = -1;
        this.targetVersion = MinecraftVersions.getCurrent().getCode();
    }

    private static JSONObject readJsonFile(File file) {
        try {
            return FileUtils.readJSON(file);
        } catch (IOException | JSONException e) {
            return new JSONObject();
        }
    }

    public ResourceGameVersion(File file) {
        this(readJsonFile(file));
    }

    public int getMinVersion() {
        return this.minVersion;
    }

    public int getMaxVersion() {
        return this.maxVersion;
    }

    public int getTargetVersion() {
        return this.targetVersion;
    }

    public boolean isCompatibleWithAnyVersion() {
        return this.minVersion == -1 && this.maxVersion == -1;
    }

    public boolean isCompatible(MinecraftVersion version) {
        int code = version.getCode();
        if (this.minVersion == -1 || code >= this.minVersion) {
            return this.maxVersion == -1 || code <= this.maxVersion;
        }
        return false;
    }

    public boolean isCompatible() {
        return isCompatible(MinecraftVersions.getCurrent());
    }

    public String toString() {
        return "ResourceVersion{minVersion=" + this.minVersion + ", maxVersion=" + this.maxVersion + ", targetVersion=" + this.targetVersion + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceGameVersion that = (ResourceGameVersion) o;
        return this.minVersion == that.minVersion && this.maxVersion == that.maxVersion && this.targetVersion == that.targetVersion;
    }
}
