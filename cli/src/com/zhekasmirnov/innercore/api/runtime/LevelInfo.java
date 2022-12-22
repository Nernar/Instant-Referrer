package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;

public class LevelInfo {
    public static final int STATE_IN_WORLD = 2;
    public static final int STATE_LOADING = 1;
    public static final int STATE_OFFLINE = 0;
    public static String levelDir;
    public static String levelName;
    public static String worldsPathOverride = null;
    public static int state = 0;

    public static int getState() {
        return state;
    }

    public static boolean isOnline() {
        return getState() != 0;
    }

    public static boolean isLoaded() {
        return getState() == 2;
    }

    public static String getLevelName() {
        if (isOnline()) {
            return levelName;
        }
        return null;
    }

    public static String getLevelDir() {
        if (isOnline()) {
            return levelDir;
        }
        return null;
    }

    public static String getAbsoluteDir() {
        if (levelDir != null && isOnline()) {
            if (worldsPathOverride != null) {
                String path = new File(worldsPathOverride, levelDir).getAbsolutePath();
                if (!path.endsWith("/")) {
                    return String.valueOf(path) + "/";
                }
                return path;
            }
            return String.valueOf(FileTools.DIR_ROOT) + "games/horizon/minecraftWorlds/" + levelDir + "/";
        }
        return null;
    }

    public static void onEnter(String name, String dir) {
        state = 1;
        levelName = name;
        levelDir = dir;
    }

    public static void onLoaded() {
        state = 2;
    }

    public static void onLeft() {
        state = 0;
    }
}
