package com.zhekasmirnov.innercore.api.unlimited;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import java.util.HashMap;
import org.mozilla.javascript.ScriptableObject;

public class BlockRegistry {
    public static final String LOGGER_TAG = "INNERCORE-BLOCKS";
    private static HashMap<IDDataPair, BlockVariant> blockVariantMap = new HashMap<>();
    private static FileLoader loader;

    public static void onInit() {
        ICLog.d("INNERCORE-BLOCKS", "reading saved mappings...");
        loader = new FileLoader(ModPackContext.getInstance().getCurrentModPack().getRequestHandler(ModPackDirectory.DirectoryType.CONFIG).get("innercore", "ids.json"));
    }

    public static void onModsLoaded() {
        Callback.invokeAPICallback("PreBlocksDefined", new Object[0]);
        loader.save();
        Callback.invokeAPICallback("BlocksDefined", new Object[0]);
        ICLog.d("INNERCORE-BLOCKS", "complete");
    }

    @SuppressWarnings("unused")
    private static void addBlockVariants(int uid, String inputNameId, Object block, ScriptableObject variantsScriptable) {
        int data = 0;
        Object[] keys = variantsScriptable.getAllIds();
        if (keys.length == 0) {
            throw new IllegalArgumentException("no variants found in variant array while creating block " + inputNameId + ", variants must be formatted as [{name: 'name', textures:[['name', index], ...], inCreative: true/false}, ...]");
        }
        for (Object key : keys) {
            Object _val = null;
            if (key instanceof Integer) {
                _val = variantsScriptable.get(((Integer) key).intValue(), variantsScriptable);
            }
            if (key instanceof String) {
                _val = variantsScriptable.get((String) key, variantsScriptable);
            }
            if (_val != null && (_val instanceof ScriptableObject)) {
                int data2 = data + 1;
                BlockVariant variant = new BlockVariant(uid, data, (ScriptableObject) _val);
                blockVariantMap.put(new IDDataPair(uid, variant.data), variant);
                data = data2;
            }
        }
    }

    public static void createBlock(int uid, String nameId, ScriptableObject variantsScriptable, SpecialType type) {
        if (!IDRegistry.getNameByID(uid).equals(nameId)) {
            throw new IllegalArgumentException("numeric uid " + uid + IDRegistry.getNameByID(uid) + " doesn't match string id " + nameId);
        }
        if (IDRegistry.isVanilla(uid)) {
            ICLog.e("INNERCORE-BLOCKS", "cannot create block with vanilla id " + uid, new RuntimeException());
            return;
        }
        throw new UnsupportedOperationException();
    }

    public static void createLiquidBlockPair(int id1, String nameId1, int id2, String nameId2, ScriptableObject variantsScriptable, SpecialType type, int tickDelay, boolean isRenewable) {
        throw new UnsupportedOperationException();
    }

    public static void createBlock(int uid, String nameId, ScriptableObject variants) {
        createBlock(uid, nameId, variants, SpecialType.DEFAULT);
    }

    public static void setShape(int uid, int data, float x1, float y1, float z1, float x2, float y2, float z2) {
        BlockShape shape = new BlockShape(x1, y1, z1, x2, y2, z2);
        shape.setToBlock(uid, data);
        BlockVariant variant = getBlockVariant(uid, data);
        if (variant != null) {
            variant.shape = shape;
        }
    }

    public static BlockVariant getBlockVariant(int uid, int data) {
        IDDataPair key = new IDDataPair(uid, data);
        return blockVariantMap.get(key);
    }
}
