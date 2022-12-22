package com.zhekasmirnov.apparatus.adapter.innercore;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class PackInfo {
    private static File packRootDirectory;
    private static boolean isInitialized = false;
    private static String packName = "Inner Core";
    private static String packBuildVersionName = "unknown version";
    private static int packBuildVersionCode = 0;

    private static synchronized void initializeIfRequired() {
        if (!isInitialized) {
            packRootDirectory = new File(FileTools.DIR_PACK);
            try {
                JSONObject manifest = FileUtils.readJSON(new File(packRootDirectory, "manifest.json"));
                packName = new StringBuilder(String.valueOf(manifest.optString("pack", packName))).toString();
                packBuildVersionName = new StringBuilder(String.valueOf(manifest.optString("packVersion", packBuildVersionName))).toString();
                packBuildVersionCode = manifest.optInt("packVersionCode", packBuildVersionCode);
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            isInitialized = true;
        }
    }

    public static String getPackName() {
        initializeIfRequired();
        return packName;
    }

    public static String getPackVersionName() {
        initializeIfRequired();
        return packBuildVersionName;
    }

    public static int getPackVersionCode() {
        initializeIfRequired();
        return packBuildVersionCode;
    }

    public static String getNetworkPackIdentifier() {
        return String.valueOf(getPackName()) + "#" + getPackVersionName();
    }
}
