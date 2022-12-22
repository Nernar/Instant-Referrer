package com.zhekasmirnov.innercore.modpack.strategy.request;

import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class DirectoryRequestStrategy {
    private ModPackDirectory directory;

    public abstract File get(String str);

    public abstract File get(String str, String str2);

    public abstract List<File> getAll(String str);

    public abstract List<String> getAllLocations();

    public void assignToDirectory(ModPackDirectory directory) {
        if (this.directory != null) {
            throw new IllegalStateException();
        }
        this.directory = directory;
    }

    public ModPackDirectory getAssignedDirectory() {
        return this.directory;
    }

    public File assure(String location, String name) {
        File file = get(location, name);
        file.getParentFile().mkdirs();
        return file;
    }

    public boolean remove(String location, String name) {
        File file = get(location, name);
        return file.delete();
    }

    public List<File> getAllFiles() {
        List<File> result = new ArrayList<>();
        for (String location : getAllLocations()) {
            result.addAll(getAll(location));
        }
        return result;
    }
}
