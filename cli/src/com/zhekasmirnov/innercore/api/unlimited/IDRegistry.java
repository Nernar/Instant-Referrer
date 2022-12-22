package com.zhekasmirnov.innercore.api.unlimited;

import com.zhekasmirnov.apparatus.minecraft.version.VanillaIdConversionMap;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

public class IDRegistry {
    public static final int BLOCK_ID_OFFSET = 8192;
    public static final int ITEM_ID_OFFSET = 2048;
    public static final int MAX_ID = 65536;
    @SuppressWarnings("unused")
    private static final int MAX_UNAPPROVED_IDS = 750;
    @SuppressWarnings("unused")
    private static final String TYPE_BLOCK = "block";
    @SuppressWarnings("unused")
    private static final String TYPE_ITEM = "item";
    private static int blockIdIterator;
    private static boolean blockIdLooped;
    private static int itemIdIterator;
    private static boolean itemIdLooped;
    private static LinkedList<String> unapprovedBlocks;
    private static LinkedList<String> unapprovedItems;
    private static int unapprovedIds = 0;
    private static final HashMap<Integer, String> nameById = new HashMap<>();
    private static final HashMap<Integer, String> vanillaNameById = new HashMap<>();
    private static final HashMap<Integer, String> vanillaTileById = new HashMap<>();
    private static final HashMap<String, Boolean> approvedIds = new HashMap<>();
    private static final HashMap<String, Integer> itemIdShortcut = new HashMap<>();
    private static final HashMap<String, Integer> blockIdShortcut = new HashMap<>();
    private static final HashMap<String, Integer> vanillaIdShortcut = new HashMap<>();
    private static final ScriptableObject itemIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject blockIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject vanillaItemIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject vanillaBlockIds = ScriptableObjectHelper.createEmpty();
    private static final ScriptableObject vanillaTileIds = ScriptableObjectHelper.createEmpty();

    static {
        try {
            Map<String, Map<String, Integer>> scopedIdMap = VanillaIdConversionMap.getSingleton().loadScopedIdMapFromAssets();
            Map<String, Integer> blocksMap = scopedIdMap.get("blocks");
            Map<String, Integer> itemsMap = scopedIdMap.get("items");
            if (itemsMap != null) {
                for (Map.Entry<String, Integer> nameAndId : itemsMap.entrySet()) {
                    String stringId = nameAndId.getKey();
                    int numericId = nameAndId.getValue().intValue();
                    vanillaNameById.put(Integer.valueOf(numericId), stringId);
                    vanillaIdShortcut.put(stringId, Integer.valueOf(numericId));
                    if (blocksMap != null && blocksMap.containsKey(stringId)) {
                        vanillaBlockIds.put(stringId, vanillaBlockIds, Integer.valueOf(numericId));
                    } else {
                        vanillaItemIds.put(stringId, vanillaItemIds, Integer.valueOf(numericId));
                    }
                }
            }
            if (blocksMap != null) {
                for (Map.Entry<String, Integer> nameAndId2 : blocksMap.entrySet()) {
                    String stringId2 = nameAndId2.getKey();
                    int numericId2 = nameAndId2.getValue().intValue();
                    vanillaTileIds.put(stringId2, vanillaTileIds, Integer.valueOf(numericId2));
                    vanillaTileById.put(Integer.valueOf(numericId2), stringId2);
                    if (numericId2 > 255) {
                        numericId2 = 255 - numericId2;
                    }
                    if (!vanillaBlockIds.has(stringId2, vanillaBlockIds) && !vanillaNameById.containsKey(Integer.valueOf(numericId2))) {
                        vanillaBlockIds.put(stringId2, vanillaBlockIds, Integer.valueOf(numericId2));
                        vanillaNameById.put(Integer.valueOf(numericId2), stringId2);
                        vanillaIdShortcut.put(stringId2, Integer.valueOf(numericId2));
                    }
                }
            }
        } catch (Exception e) {
            ICLog.e("INNERCORE-BLOCKS", "Unable to read vanilla numeric IDs", e);
        }
        unapprovedBlocks = new LinkedList<>();
        blockIdIterator = 8192;
        blockIdLooped = false;
        unapprovedItems = new LinkedList<>();
        itemIdIterator = 2048;
        itemIdLooped = false;
    }

    static void approve(String name, String type) {
        HashMap<String, Boolean> hashMap = approvedIds;
        hashMap.put(String.valueOf(type) + "$" + name, true);
    }

    static boolean isApproved(String name, String type) {
        HashMap<String, Boolean> hashMap = approvedIds;
        return hashMap.containsKey(String.valueOf(type) + "$" + name);
    }

    static void putId(String name, int id) {
        if (id >= 8192) {
            blockIdShortcut.put(name, Integer.valueOf(id));
            blockIds.put(name, blockIds, Integer.valueOf(id));
        } else if (id >= 2048) {
            itemIdShortcut.put(name, Integer.valueOf(id));
            itemIds.put(name, itemIds, Integer.valueOf(id));
        }
        nameById.put(Integer.valueOf(id), name);
    }

    static boolean isOccupied(int id) {
        return nameById.containsKey(Integer.valueOf(id)) || vanillaNameById.containsKey(Integer.valueOf(id)) || vanillaTileById.containsKey(Integer.valueOf(id));
    }

    @JSStaticFunction
    public static int genBlockID(String name) {
        if (!NameTranslation.isAscii(name)) {
            ICLog.e("INNERCORE-BLOCKS", "block string id " + name + " contains unicode characters, it will not be created", new RuntimeException());
            return 0;
        } else if (vanillaNameById.values().contains("block_" + name) || vanillaTileById.values().contains(name)) {
            ICLog.e("INNERCORE-BLOCKS", "block string id " + name + " is a vanilla string ID, so the item won't be created", new RuntimeException());
            return 0;
        } else {
            approve(name, "block");
            if (blockIdShortcut.containsKey(name)) {
                return blockIdShortcut.get(name).intValue();
            }
            while (isOccupied(blockIdIterator)) {
                blockIdIterator++;
                if (blockIdIterator > 65536) {
                    if (blockIdLooped) {
                        throw new RuntimeException("ID LIMIT EXCEEDED while registring block string id " + name);
                    }
                    blockIdLooped = true;
                    blockIdIterator = 0;
                }
            }
            putId(name, blockIdIterator);
            int i = blockIdIterator;
            blockIdIterator = i + 1;
            return i;
        }
    }

    @JSStaticFunction
    public static int genItemID(String name) {
        if (!NameTranslation.isAscii(name)) {
            ICLog.e("INNERCORE-BLOCKS", "item string id " + name + " contains unicode characters, it will not be created", new RuntimeException());
            return 0;
        } else if (vanillaNameById.values().contains("item_" + name) || vanillaTileById.values().contains(name)) {
            ICLog.e("INNERCORE-BLOCKS", "item string id " + name + " is a vanilla string ID, so the item won't be created", new RuntimeException());
            return 0;
        } else {
            approve(name, "item");
            if (itemIdShortcut.containsKey(name)) {
                return itemIdShortcut.get(name).intValue();
            }
            while (isOccupied(itemIdIterator)) {
                itemIdIterator++;
                if (itemIdIterator > 65536) {
                    if (itemIdLooped) {
                        throw new RuntimeException("ID LIMIT EXCEEDED while registring item string id " + name);
                    }
                    itemIdLooped = true;
                    itemIdIterator = 0;
                }
            }
            putId(name, itemIdIterator);
            int i = itemIdIterator;
            itemIdIterator = i + 1;
            return i;
        }
    }

    @JSStaticFunction
    public static String getNameByID(int id) {
        return nameById.get(Integer.valueOf(id));
    }

    @JSStaticFunction
    public static String getStringIdAndTypeForItemId(int id) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static String getTypeForItemId(int id) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static String getStringIdForItemId(int id) {
        throw new UnsupportedOperationException();
    }

    public static int getIDByName(String name) {
        if (vanillaIdShortcut.containsKey(name)) {
            return vanillaIdShortcut.get(name).intValue();
        }
        return 0;
    }

    @JSStaticFunction
    public static boolean isVanilla(int id) {
        return id == 0 || vanillaNameById.containsKey(Integer.valueOf(id)) || vanillaTileById.containsKey(Integer.valueOf(id));
    }

    @JSStaticFunction
    public static int ensureBlockId(int id) {
        if ((vanillaNameById.containsKey(Integer.valueOf(id)) || vanillaTileById.containsKey(Integer.valueOf(id + 255))) && id < 0) {
            return id + 255;
        }
        return id;
    }

    @JSStaticFunction
    public static int ensureItemId(int id) {
        if (vanillaTileById.containsKey(Integer.valueOf(id)) && id > 255) {
            return 255 - id;
        }
        return id;
    }

    static JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            JSONObject blocks = new JSONObject();
            JSONObject items = new JSONObject();
            for (String name : blockIdShortcut.keySet()) {
                blocks.put(name, blockIdShortcut.get(name));
                if (!isApproved(name, "block")) {
                    unapprovedItems.add(name);
                    unapprovedIds++;
                }
            }
            for (String name2 : itemIdShortcut.keySet()) {
                items.put(name2, itemIdShortcut.get(name2));
                if (!isApproved(name2, "item")) {
                    unapprovedBlocks.add(name2);
                    unapprovedIds++;
                }
            }
            if (unapprovedIds > 750) {
                ICLog.d("INNERCORE-BLOCKS", "too many unused IDs, clearing...");
                Iterator<String> it = unapprovedItems.iterator();
                while (it.hasNext()) {
                    String name3 = it.next();
                    items.remove(name3);
                }
                Iterator<String> it2 = unapprovedBlocks.iterator();
                while (it2.hasNext()) {
                    String name4 = it2.next();
                    blocks.remove(name4);
                }
            }
            obj.put("blocks", blocks);
            obj.put("items", items);
            return obj;
        } catch (Exception e) {
            ICLog.e("INNERCORE-BLOCKS", "failed to save string id bindings", e);
            return null;
        }
    }

    static void fromJson(JSONObject obj) {
        JSONArray keys;
        JSONObject blocks = obj.optJSONObject("blocks");
        JSONObject items = obj.optJSONObject("items");
        if (blocks != null) {
            try {
                JSONArray keys2 = blocks.names();
                if (keys2 != null) {
                    for (int i = 0; i < keys2.length(); i++) {
                        String key = keys2.optString(i);
                        if (key != null) {
                            putId(key, blocks.optInt(key));
                        }
                    }
                }
            } catch (Exception e) {
                ICLog.e("INNERCORE-BLOCKS", "failed to load string id bindings", e);
                return;
            }
        }
        if (items != null && (keys = items.names()) != null) {
            for (int i2 = 0; i2 < keys.length(); i2++) {
                String key2 = keys.optString(i2);
                if (key2 != null) {
                    putId(key2, items.optInt(key2));
                }
            }
        }
    }

    public static void injectAPI(ScriptableObject scope) {
        scope.put("BlockID", scope, blockIds);
        scope.put("ItemID", scope, itemIds);
        scope.put("VanillaItemID", scope, vanillaItemIds);
        scope.put("VanillaBlockID", scope, vanillaBlockIds);
        scope.put("VanillaTileID", scope, vanillaTileIds);
    }

    public static void rebuildNetworkIdMap() {
        throw new UnsupportedOperationException();
    }
}
