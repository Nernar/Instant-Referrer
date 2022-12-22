package com.zhekasmirnov.horizon.modloader.repo.storage;

import com.zhekasmirnov.horizon.modloader.repo.location.LocalModLocation;
import com.zhekasmirnov.horizon.modloader.repo.location.ModLocation;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryRepository extends ModRepository {
    private final File directory;
    private final List<ModLocation> locations = new ArrayList<>();

    public DirectoryRepository(File file) {
        this.directory = file;
        if (file.exists()) {
            return;
        }
        file.mkdirs();
    }

    @Override
    public void refresh(EventLogger eventLogger) {
        this.locations.clear();
        File[] listFiles = this.directory.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory() && new File(file, "manifest").exists()) {
                    this.locations.add(new LocalModLocation(file));
                }
            }
        }
    }

    @Override
    public List<ModLocation> getAllLocations() {
        return this.locations;
    }
}
