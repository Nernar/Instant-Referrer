package com.zhekasmirnov.horizon.modloader.resource.runtime;

import com.zhekasmirnov.horizon.modloader.resource.ResourceOverride;
import java.io.File;

public class RuntimeResource {
    private final RuntimeResourceDirectory directory;
    private final String name;
    private final ResourceOverride override;

    public RuntimeResource(RuntimeResourceDirectory runtimeResourceDirectory, ResourceOverride resourceOverride, String str) {
        this.directory = runtimeResourceDirectory;
        this.override = resourceOverride;
        this.name = str;
    }

    public ResourceOverride getOverride() {
        return this.override;
    }

    public RuntimeResourceDirectory getDirectory() {
        return this.directory;
    }

    public String getName() {
        return this.name;
    }

    public File getFile() {
        File file = new File(this.override.override);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        return file;
    }
}
