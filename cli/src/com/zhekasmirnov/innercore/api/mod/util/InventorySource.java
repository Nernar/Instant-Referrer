package com.zhekasmirnov.innercore.api.mod.util;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class InventorySource {
    public static boolean isUpdating = false;
    private static final ScriptableObject[] slots = new ScriptableObject[36];

    public static void tick() {
        if (isUpdating) {
            throw new UnsupportedOperationException();
        }
    }

    public static ScriptableObject getSource(int slotId) {
        int slotId2 = slotId % 36;
        if (slots[slotId2] == null) {
            ScriptableObject slot = new ScriptableObject() {
                @Override
                public String getClassName() {
                    return "slot";
                }
            };
            slot.put("id", (Scriptable) slot, (Object) 0);
            slot.put("count", (Scriptable) slot, (Object) 0);
            slot.put("data", (Scriptable) slot, (Object) 0);
            slot.put("extra", slot, (Object) null);
            slots[slotId2] = slot;
        }
        return slots[slotId2];
    }

    public static void setSource(int slotId, int id, int count, int data, Object extra) {
        ScriptableObject slot = getSource(slotId);
        slot.put("id", slot, Integer.valueOf(id));
        slot.put("count", slot, Integer.valueOf(count));
        slot.put("data", slot, Integer.valueOf(data));
        slot.put("extra", slot, extra);
    }
}
