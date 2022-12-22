package com.zhekasmirnov.horizon.modloader.resource;

import com.zhekasmirnov.horizon.launcher.env.AssetPatch;
import com.zhekasmirnov.horizon.modloader.resource.directory.Resource;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResourceOverride {
    private final List<String> injectedOverrides;
    public final String override;
    public final String path;

    public ResourceOverride(String str, String str2) {
        this.injectedOverrides = new ArrayList<>();
        this.path = FileUtils.cleanupPath(str);
        this.override = FileUtils.cleanupPath(str2);
    }

    public ResourceOverride(String str, File file) {
        this(str, file.getAbsolutePath());
    }

    public ResourceOverride(Resource resource, String str) {
        this(resource.getPath(), str);
    }

    public ResourceOverride(Resource resource, File file) {
        this(resource.getPath(), file.getAbsolutePath());
    }

    public ResourceOverride(Resource resource) {
        this(resource, resource.file);
    }

    public boolean isActive() {
        return this.injectedOverrides.size() > 0 && AssetPatch.getSingleOverride(this.injectedOverrides.get(0)) != null;
    }

    public boolean deploy() {
        return deploy(new String[]{""});
    }

    public boolean deploy(List<String> list) {
        return deploy((String[]) list.toArray(new String[0]));
    }

    public boolean deploy(String[] strArr) {
        remove();
        for (String str : strArr) {
            String str2 = String.valueOf(str) + this.path;
            this.injectedOverrides.add(str2);
            AssetPatch.addSingleOverride(str2, this.override);
        }
        return true;
    }

    public boolean remove() {
        for (String str : this.injectedOverrides) {
            AssetPatch.removeSingleOverride(str);
        }
        this.injectedOverrides.clear();
        return true;
    }
}
