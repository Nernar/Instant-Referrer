package com.zhekasmirnov.innercore.modpack.strategy.extract;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import java.io.File;
import java.util.List;

public abstract class DirectoryExtractStrategy {
    private ModPackDirectory directory;

    public abstract String getEntryName(String str, File file);

    public abstract List<File> getFilesToExtract();

    public void assignToDirectory(ModPackDirectory directory) {
        if (this.directory != null) {
            throw new IllegalStateException();
        }
        this.directory = directory;
    }

    public ModPackDirectory getAssignedDirectory() {
        return this.directory;
    }

    public String getFullEntryName(File file) {
        String path = file.getAbsolutePath();
        String dir = getAssignedDirectory().getLocation().getAbsolutePath();
        if (!path.startsWith(dir)) {
            throw new IllegalArgumentException("getEntryNameForFile got file, not contained in assigned directory");
        }
        String relative = FileUtils.cleanupPath(path.substring(dir.length()));
        return (String.valueOf(getAssignedDirectory().getPathPattern()) + "/" + FileUtils.cleanupPath(getEntryName(relative, file))).replaceAll("//", "/");
    }
}
