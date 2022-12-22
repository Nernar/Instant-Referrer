package com.zhekasmirnov.innercore.api.commontypes;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.ScriptableObject;

public class ItemInstance extends ScriptableObject {
    @Override
    public String getClassName() {
        return "Item";
    }

    public ItemInstance(int id, int count, int data) {
        put("id", this, Integer.valueOf(id));
        put("count", this, Integer.valueOf(count));
        put("data", this, Integer.valueOf(data));
    }

    public ItemInstance(int id, int count, int data, Object extra) {
        this(id, count, data);
        put("extra", this, extra);
    }

    public ItemInstance(Object nativeItemInstance) {
        this(0, 0, 0);
    }

    public ItemInstance(long ptr) {
        this((Object) null);
    }

    public int getId() {
        return ((Number) get("id")).intValue();
    }

    public int getCount() {
        return ((Number) get("count")).intValue();
    }

    public int getData() {
        return ((Number) get("data")).intValue();
    }

    public Object getExtra() {
        return ScriptableObjectHelper.getProperty(this, "extra", null);
    }

    public long getExtraValue() {
        return ((Long) ScriptableObjectHelper.getProperty(this, "extra", null)).longValue();
    }
}
