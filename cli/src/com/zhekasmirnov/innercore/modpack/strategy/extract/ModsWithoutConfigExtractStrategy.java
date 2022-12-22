package com.zhekasmirnov.innercore.modpack.strategy.extract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModsWithoutConfigExtractStrategy extends AllFilesDirectoryExtractStrategy {
    @Override
    public List<File> getFilesToExtract() {
        List<File> files = new ArrayList<>();
        addAllRecursive(getAssignedDirectory().getLocation(), files, C$$Lambda$ModsWithoutConfigExtractStrategy$qqnuqbFL3_OxQOYN52aLAQ139oI.INSTANCE);
        return files;
    }

    static boolean lambda$getFilesToExtract$0(File file) {
        return (file.getName().toLowerCase().matches("(config(.info)?.json|.staticids)") || file.getParentFile().getName().equalsIgnoreCase("config")) ? false : true;
    }
}
