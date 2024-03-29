package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.build.Config;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;

public class InnerCoreConfig {
    public static Config config = new Config(getConfigFile());

    static {
        try {
            String contents = FileTools.readFileText(String.valueOf(FileTools.DIR_PACK) + "assets/innercore/innercore_default_config.json");
            config.checkAndRestore(contents);
        } catch (IOException | JSONException e) {
            ICLog.e("CONFIG", "cannot load and validate default config.", e);
        }
    }

    public static void reload() {
        ICLog.d("CONFIG", "reloading inner core config");
        config.reload();
    }

    public static File getConfigFile() {
        return new File(FileTools.DIR_WORK, "config.json");
    }

    public static Object get(String name) {
        return config.get(name);
    }

    public static boolean getBool(String name) {
        Object b = get(name);
        return (b instanceof Boolean) && ((Boolean) b).booleanValue();
    }

    public static int getInt(String name) {
        return ((Number) config.get(name)).intValue();
    }

    public static int getInt(String name, int fallback) {
        try {
            return getInt(name);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static int convertThreadPriority(int val) {
        return 20 - Math.min(40, Math.max(1, val));
    }

    public static void set(String name, Object val) {
        config.set(name, val);
    }
}
