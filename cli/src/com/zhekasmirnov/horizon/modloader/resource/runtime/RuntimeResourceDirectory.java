package com.zhekasmirnov.horizon.modloader.resource.runtime;

import android.util.Pair;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.ResourceOverride;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.util.HashMap;

public class RuntimeResourceDirectory {
    public final File directory;
    public final ResourceManager resourceManager;
    private HashMap<String, Pair<RuntimeResource, RuntimeResourceHandler>> resources = new HashMap<>();

    public RuntimeResourceDirectory(ResourceManager resourceManager, File file) {
        this.resourceManager = resourceManager;
        this.directory = file;
        if (!file.exists()) {
            this.directory.mkdirs();
        }
        if (!this.directory.isDirectory()) {
            throw new IllegalArgumentException("Non-directory file passed to RuntimeResourceDirectory constructor");
        }
    }

    public void clear() {
        FileUtils.clearFileTree(this.directory, false);
    }

    public void addHandler(RuntimeResourceHandler runtimeResourceHandler) {
        String resourceName = runtimeResourceHandler.getResourceName();
        this.resources.put(resourceName, new Pair<>(new RuntimeResource(this, new ResourceOverride(runtimeResourceHandler.getResourcePath(), new File(this.directory, resourceName)), resourceName), runtimeResourceHandler));
    }

    private void handleResource(RuntimeResource runtimeResource, RuntimeResourceHandler runtimeResourceHandler) {
        runtimeResource.getOverride().deploy(this.resourceManager.getResourceOverridePrefixes());
        runtimeResourceHandler.handle(runtimeResource);
    }

    public void handleAll() {
        for (Pair<RuntimeResource, RuntimeResourceHandler> pair : this.resources.values()) {
            handleResource(pair.first, pair.second);
        }
    }
}
