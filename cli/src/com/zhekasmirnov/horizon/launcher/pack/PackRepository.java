package com.zhekasmirnov.horizon.launcher.pack;

import java.util.ArrayList;
import java.util.List;

public abstract class PackRepository {
    public abstract void fetch();

    public abstract List<String> getAllPacksUUIDs();

    public abstract IPackLocation getLocationForUUID(String str);

    public List<String> getPackSuggestions() {
        return new ArrayList<>();
    }
}
