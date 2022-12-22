package com.zhekasmirnov.innercore.modpack.strategy.extract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigDirectoryExtractStrategy extends AllFilesDirectoryExtractStrategy {
    @Override
    public List<File> getFilesToExtract() {
        List<File> files = new ArrayList<>();
        addAllRecursive(getAssignedDirectory().getLocation(), files, C$$Lambda$ConfigDirectoryExtractStrategy$nCTTs9UwvBynBjowc5uZMa0.INSTANCE);
        return files;
    }

    static boolean lambda$getFilesToExtract$0(File file) {
        return !file.getAbsolutePath().toLowerCase().contains(".keep-unchanged");
    }
}
