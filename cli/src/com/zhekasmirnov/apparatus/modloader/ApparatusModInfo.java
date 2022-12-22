package com.zhekasmirnov.apparatus.modloader;

import com.zhekasmirnov.innercore.mod.build.Mod;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ApparatusModInfo {
    private final Map<String, Object> properties = new HashMap<>();

    public void putProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name, Class<T> type, T fallback) {
        T t = (T) this.properties.get(name);
        return type.isInstance(t) ? t : fallback;
    }

    public String getString(String name, String fallback) {
        return (String) getProperty(name, String.class, fallback);
    }

    public String getString(String name) {
        return getString(name, null);
    }

    public int getInt(String name, int fallback) {
        return ((Integer) getProperty(name, Integer.class, Integer.valueOf(fallback))).intValue();
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }

    public float getFloat(String name, float fallback) {
        return ((Float) getProperty(name, Float.class, Float.valueOf(fallback))).floatValue();
    }

    public float getFloat(String name) {
        return getFloat(name, 0.0f);
    }

    public boolean getBoolean(String name, boolean fallback) {
        return ((Boolean) getProperty(name, Boolean.class, Boolean.valueOf(fallback))).booleanValue();
    }

    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    public void pullLegacyModProperties(Mod innerCoreMod) {
        putProperty("multiplayer_supported", Boolean.valueOf(innerCoreMod.isConfiguredForMultiplayer()));
        putProperty("client_only", Boolean.valueOf(innerCoreMod.isClientOnly()));
        putProperty("name", innerCoreMod.getMultiplayerName());
        putProperty("displayed_name", innerCoreMod.getName());
        putProperty("version", innerCoreMod.getMultiplayerVersion());
        putProperty("description", innerCoreMod.getInfoProperty("description"));
        putProperty("developer", innerCoreMod.getInfoProperty("author"));
        putProperty("icon_path", new File(innerCoreMod.dir, "icon.png").getAbsolutePath());
        putProperty("icon_name", innerCoreMod.getGuiIcon());
        putProperty("directory_root", innerCoreMod.dir);
    }

    public String toString() {
        return "ApparatusModInfo" + this.properties;
    }
}
