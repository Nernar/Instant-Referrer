package com.zhekasmirnov.horizon.modloader.repo.location;

import com.zhekasmirnov.horizon.modloader.repo.storage.TemporaryStorage;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import java.io.File;

public class LocalModLocation extends ModLocation {
    private final File mod;

    public LocalModLocation(File file) {
        this.mod = file;
    }

    @Override
    public File initializeInLocalStorage(TemporaryStorage temporaryStorage, EventLogger eventLogger) {
        return this.mod;
    }

    public String toString() {
        return "[LocalModLocation path=" + this.mod + "]";
    }
}
