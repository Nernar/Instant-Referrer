package com.zhekasmirnov.apparatus.minecraft.version;

import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class VanillaIdConversionMap {
    private static final VanillaIdConversionMap singleton = new VanillaIdConversionMap();

    public static VanillaIdConversionMap getSingleton() {
        return singleton;
    }

    private VanillaIdConversionMap() {
    }

    private boolean tryLoadFromAsset(String asset, Map<String, Map<String, Integer>> scopedIdMap, boolean override) {
        try {
            JSONObject json = FileTools.getAssetAsJSON(asset);
            loadJsonIntoMap(json, scopedIdMap, override);
            ICLog.d("VanillaIdConversionMap", "loaded ids from asset " + asset);
            return true;
        } catch (NullPointerException | IOException | JSONException e) {
            return false;
        }
    }

    public Map<String, Map<String, Integer>> loadScopedIdMapFromAssets() {
        Map<String, Map<String, Integer>> scopedIdMap = new HashMap<>();
        tryLoadFromAsset("innercore/id/numeric_ids.json", scopedIdMap, false);
        for (int index = 0; tryLoadFromAsset("innercore/id/numeric_ids_override_" + index + ".json", scopedIdMap, false); index++) {
        }
        tryLoadFromAsset("innercore/numeric_ids.json", scopedIdMap, false);
        fixMissingItemIds(scopedIdMap);
        return scopedIdMap;
    }

    public void reloadFromAssets() {
        reloadFrom(loadScopedIdMapFromAssets());
    }

    private void fixMissingItemIds(Map<String, Map<String, Integer>> scopedIdMap) {
        Map<String, Integer> scopeBlocks = scopedIdMap.get("blocks");
        Map<String, Integer> scopeItems = scopedIdMap.get("items");
        if (scopeBlocks == null || scopeItems == null) {
            return;
        }
        for (Map.Entry<String, Integer> entry : scopeBlocks.entrySet()) {
            int blockId = entry.getValue().intValue();
            String key = entry.getKey();
            Integer itemId = scopeItems.get(key);
            int newItemId = blockId > 255 ? 255 - blockId : blockId;
            if (itemId == null || itemId.intValue() != newItemId) {
                if (!scopeItems.containsValue(Integer.valueOf(newItemId)) && !scopeItems.containsKey(key)) {
                    scopeItems.put(key, Integer.valueOf(newItemId));
                }
            }
        }
    }

    public void loadJsonIntoMap(JSONObject json, Map<String, Map<String, Integer>> scopedIdMap, boolean override) {
        Iterator<String> it = json.keys();
        while (it.hasNext()) {
            String key = it.next();
            JSONObject scopeJson = json.optJSONObject(key);
            if (scopeJson != null) {
                Map<String, Integer> scope = Java8BackComp.computeIfAbsent(scopedIdMap, key, C$$Lambda$VanillaIdConversionMap$nVjPDSo0oHtc8aR6oRcKKe7Bf8.INSTANCE);
                Iterator<String> iter = scopeJson.keys();
                while (iter.hasNext()) {
                    String name = iter.next();
                    int id = scopeJson.optInt(name);
                    if (id != 0 && (override || !scope.containsKey(name))) {
                        scope.put(name, Integer.valueOf(id));
                    }
                }
            }
        }
    }

    static Map<String, Integer> lambda$loadJsonIntoMap$0(String key0) {
        return new HashMap<>();
    }

    public synchronized void reloadFrom(Map<String, Map<String, Integer>> scopedIdMap) {
        if (!MinecraftVersions.getCurrent().isFeatureSupported("vanilla_id_mapping")) {
            ICLog.d("VanillaIdConversionMap", "vanilla id remapping is not required on this version of the game");
            return;
        }
        for (Map.Entry<String, Map<String, Integer>> scope : scopedIdMap.entrySet()) {
            scope.getKey().hashCode();
            scope.getValue().entrySet();
        }
    }
}
