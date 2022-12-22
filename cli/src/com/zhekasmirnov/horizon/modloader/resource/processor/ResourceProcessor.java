package com.zhekasmirnov.horizon.modloader.resource.processor;

import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.Resource;
import java.util.Collection;

public interface ResourceProcessor {
    void initialize(ResourceManager resourceManager);

    void process(Resource resource, Collection<Resource> collection);
}
