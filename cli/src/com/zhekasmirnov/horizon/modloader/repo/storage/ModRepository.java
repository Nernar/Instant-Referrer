package com.zhekasmirnov.horizon.modloader.repo.storage;

import com.zhekasmirnov.horizon.modloader.repo.location.ModLocation;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import java.util.List;

public abstract class ModRepository {
    public abstract List<ModLocation> getAllLocations();

    public abstract void refresh(EventLogger eventLogger);
}
