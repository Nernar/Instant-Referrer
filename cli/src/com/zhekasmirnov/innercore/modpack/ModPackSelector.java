package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class ModPackSelector {
    @SuppressWarnings("unused")
    private static final String PACK_SELECTED = "pack_selected";
    private static final String PREFERENCES_PATH = String.valueOf(FileTools.DIR_WORK) + "preferences.json";

    public static void setSelected(ModPack pack) {
        ModPackContext packContext = ModPackContext.getInstance();
        JSONObject preferences = readPreferences();
        try {
            preferences.put("pack_selected", pack.getRootDirectory().getAbsolutePath());
            FileTools.writeJSON(PREFERENCES_PATH, preferences);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        packContext.setCurrentModPack(pack);
    }

    public static void restoreSelected() {
        ModPack pack;
        ModPackContext packContext = ModPackContext.getInstance();
        ModPackStorage packStorage = packContext.getStorage();
        packStorage.rebuildModPackList();
        JSONObject preferences = readPreferences();
        String root = preferences.optString("pack_selected", null);
        if (root != null && (pack = getPackByRoot(root)) != null) {
            packContext.setCurrentModPack(pack);
        }
        if (packContext.getCurrentModPack() == null) {
            packContext.setCurrentModPack(packStorage.getDefaultModPack());
        }
        ICLog.d("ModPackSelector", "selected modpack: " + packContext.getCurrentModPack().getRootDirectory());
    }

    private static ModPack getPackByRoot(String root) {
        ModPackContext packContext = ModPackContext.getInstance();
        ModPackStorage packStorage = packContext.getStorage();
        for (ModPack pack : packStorage.getAllModPacks()) {
            if (pack.getRootDirectory().equals(new File(root))) {
                return pack;
            }
        }
        return null;
    }

    private static JSONObject readPreferences() {
        try {
            JSONObject preferences = FileTools.readJSON(PREFERENCES_PATH);
            return preferences;
        } catch (IOException | JSONException e) {
            JSONObject preferences2 = new JSONObject();
            return preferences2;
        }
    }
}
