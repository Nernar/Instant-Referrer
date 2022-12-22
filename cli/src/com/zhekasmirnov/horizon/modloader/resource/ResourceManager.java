package com.zhekasmirnov.horizon.modloader.resource;

import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.modloader.resource.directory.Resource;
import com.zhekasmirnov.horizon.modloader.resource.directory.ResourceDirectory;
import com.zhekasmirnov.horizon.modloader.resource.processor.ResourceProcessor;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResourceDirectory;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResourceHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ResourceManager {
    public final Object context;
    public final RuntimeResourceDirectory runtimeDir;
    private List<String> resourceOverridePrefixes = new ArrayList<>();
    private List<ResourceProcessor> processors = new ArrayList<>();
    private List<ResourceDirectory> directories = new ArrayList<>();
    private HashSet<String> resourcePaths = new HashSet<>();

    public ResourceManager(Object context) {
        this.context = context;
        this.runtimeDir = new RuntimeResourceDirectory(this, new File(Environment.getDataDirFile(context), "resovd"));
        addResourcePrefixes("");
    }

    public Object getAssets() {
        return null;
    }

    public void addResourcePrefixes(String... strArr) {
        for (String str : strArr) {
            if (!this.resourceOverridePrefixes.contains(str)) {
                this.resourceOverridePrefixes.add(str);
            }
        }
    }

    public List<String> getResourceOverridePrefixes() {
        return this.resourceOverridePrefixes;
    }

    public void addResourceProcessor(ResourceProcessor resourceProcessor) {
        this.processors.add(resourceProcessor);
        resourceProcessor.initialize(this);
    }

    public void addRuntimeResourceHandler(RuntimeResourceHandler runtimeResourceHandler) {
        this.runtimeDir.addHandler(runtimeResourceHandler);
    }

    public void addResourceDirectory(ResourceDirectory resourceDirectory) {
        for (ResourceDirectory resourceDirectory2 : this.directories) {
            if (resourceDirectory2.equals(resourceDirectory)) {
                return;
            }
        }
        this.directories.add(resourceDirectory);
        resourceDirectory.initialize();
    }

    public void clear() {
        this.directories.clear();
        this.runtimeDir.clear();
    }

    public List<Resource> getProcessedResources(List<Resource> list) {
        for (ResourceProcessor resourceProcessor : this.processors) {
            ArrayList<Resource> arrayList = new ArrayList<>();
            for (Resource resource : list) {
                resourceProcessor.process(resource, arrayList);
            }
            list = arrayList;
        }
        return list;
    }

    private static void mergeResourceArrays(List<Resource> list, List<Resource> list2) {
        int i2 = 0;
        for (int i = 0; i < list2.size(); i++) {
            Resource resource = list.get(i2);
            Resource resource2 = list2.get(i);
            if (resource.getIndex() > resource2.getIndex()) {
                list.add(i2, resource2);
            } else if (resource.getIndex() < resource2.getIndex()) {
                if (i >= list.size()) {
                    list.add(resource2);
                }
                i2++;
            }
        }
    }

    public List<Resource> getResource(String str) {
        ArrayList<Resource> arrayList = null;
        for (ResourceDirectory resourceDirectory : this.directories) {
            List<Resource> resource = resourceDirectory.getResource(str);
            if (resource != null) {
                if (arrayList != null) {
                    mergeResourceArrays(arrayList, resource);
                } else {
                    arrayList = new ArrayList<>(resource);
                }
            }
        }
        return arrayList;
    }

    public void addResourcePath(String str) {
        this.resourcePaths.add(str);
    }

    public void deployAllOverrides() {
        ArrayList<ResourceOverride> arrayList = new ArrayList<>();
        Iterator<String> it = this.resourcePaths.iterator();
        while (it.hasNext()) {
            List<Resource> resource = getResource(it.next());
            if (resource != null) {
                for (Resource resource2 : getProcessedResources(resource)) {
                    resource2.addOverrides(arrayList);
                }
            }
        }
        Iterator<ResourceOverride> it2 = arrayList.iterator();
        while (it2.hasNext()) {
            ResourceOverride resourceOverride = it2.next();
            resourceOverride.deploy(this.resourceOverridePrefixes);
        }
        this.runtimeDir.handleAll();
    }
}
