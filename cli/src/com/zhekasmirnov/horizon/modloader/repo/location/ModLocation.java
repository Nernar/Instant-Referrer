package com.zhekasmirnov.horizon.modloader.repo.location;

import com.zhekasmirnov.horizon.modloader.repo.storage.TemporaryStorage;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import java.io.File;

public abstract class ModLocation {
    public abstract File initializeInLocalStorage(TemporaryStorage temporaryStorage, EventLogger eventLogger);
}
