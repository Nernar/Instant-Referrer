package com.zhekasmirnov.innercore.core;

import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.util.HashMap;

public class AssetOverrideManager {
    private static HashMap<String, String> pathOverrides = new HashMap<>();

    public static void addOverride(String original, String override) {
        pathOverrides.put(original, override);
    }

    public static String getPathOverride(String path) {
        if (pathOverrides.containsKey(path)) {
            String override = pathOverrides.get(path);
            return getFileOverride(override);
        }
        String override2 = getFileOverride(path);
        return override2;
    }

    public static String getFileOverride(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(FileTools.DIR_MINECRAFT);
        sb.append("innercore/assets");
        sb.append(path.startsWith("/") ? "" : "/");
        sb.append(path);
        String filepath = sb.toString();
        File file = new File(filepath);
        if (file.exists()) {
            return filepath;
        }
        return path;
    }
}
