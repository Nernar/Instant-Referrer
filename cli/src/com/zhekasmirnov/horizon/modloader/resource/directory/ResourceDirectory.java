package com.zhekasmirnov.horizon.modloader.resource.directory;

import com.zhekasmirnov.horizon.modloader.mod.Mod;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResourceDirectory;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ResourceDirectory {
    public final File directory;
    public final ResourceManager manager;
    public final Mod mod;
    private HashMap<String, List<Resource>> resources;
    public final RuntimeResourceDirectory runtimeDir;

    public ResourceDirectory(ResourceManager resourceManager, Mod mod, File file) {
        this.resources = new HashMap<>();
        this.mod = mod;
        if (!file.isDirectory()) {
            throw new IllegalStateException("non-directory file passed to ResourceDirectory constructor: " + file);
        }
        this.manager = resourceManager;
        this.runtimeDir = resourceManager.runtimeDir;
        this.directory = file;
    }

    public ResourceDirectory(ResourceManager resourceManager, File file) {
        this(resourceManager, null, file);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ResourceDirectory) {
            return ((ResourceDirectory) obj).directory.getAbsolutePath().equals(this.directory.getAbsolutePath());
        }
        return super.equals(obj);
    }

    private void initializeDirectory(List<Resource> list, File file) {
        for (File file2 : file.listFiles()) {
            if (file2.isDirectory()) {
                initializeDirectory(list, file2);
            } else {
                list.add(new Resource(this, file2));
            }
        }
    }

    private void addResourceByPath(String str, Resource resource) {
        List<Resource> list = this.resources.get(str);
        if (list == null) {
            list = new ArrayList<>();
            this.resources.put(str, list);
        }
        int i = 0;
        for (Resource resource2 : list) {
            i++;
            if (resource2.getIndex() < resource.getIndex()) {
                break;
            }
        }
        list.add(i, resource);
        this.manager.addResourcePath(str);
    }

    public void initialize() {
        ArrayList<Resource> arrayList = new ArrayList<>();
        initializeDirectory(arrayList, this.directory);
        this.resources.clear();
        Iterator<Resource> it = arrayList.iterator();
        while (it.hasNext()) {
            Resource resource = it.next();
            addResourceByPath(resource.getPath(), resource);
        }
    }

    public String getResourceName(File file) {
        String absolutePath = this.directory.getAbsolutePath();
        if (file.getAbsolutePath().startsWith(absolutePath)) {
            return FileUtils.cleanupPath(file.getAbsolutePath().substring(absolutePath.length()));
        }
        return null;
    }

    public List<Resource> getResource(String str) {
        return this.resources.get(str);
    }
}
