package com.zhekasmirnov.apparatus.adapter.innercore.game.item;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

public class ItemStack {
    public int count;
    public int data;
    public Object extra;
    public int id;

    public ItemStack(int id, int count, int data, Object extra) {
        this.id = id;
        this.count = count;
        this.data = data;
        this.extra = extra;
    }

    public ItemStack(int id, int count, int data) {
        this(id, count, data, null);
    }

    public ItemStack() {
        this(0, 0, 0, null);
    }

    public ItemStack(ItemStack stack) {
        this(stack.id, stack.count, stack.data, stack.extra);
    }

    public ItemStack(Object itemInstance) {
    }

    public ItemStack(ScriptableObject scriptable) {
        this(ScriptableObjectHelper.getIntProperty(scriptable, "id", 0), ScriptableObjectHelper.getIntProperty(scriptable, "count", 0), ScriptableObjectHelper.getIntProperty(scriptable, "data", 0));
    }

    public static ItemStack fromPtr(long ptr) {
        return new ItemStack();
    }

    public static ItemStack parse(Object obj) {
        while (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }
        if (obj == null || "undefined".equals(obj.toString().toLowerCase())) {
            return null;
        }
        if (obj instanceof ScriptableObject) {
            return new ItemStack((ScriptableObject) obj);
        }
        if (obj instanceof ItemStack) {
            return new ItemStack((ItemStack) obj);
        }
        if ((obj instanceof JSONObject) || (obj instanceof CharSequence)) {
            try {
                JSONObject json = obj instanceof JSONObject ? (JSONObject) obj : new JSONObject(obj.toString());
                return new ItemStack(json.optInt("id", 0), json.optInt("count", 0), json.optInt("data", 0));
            } catch (JSONException e) {
                return null;
            }
        } else if (!(obj instanceof Long)) {
            return null;
        } else {
            return fromPtr(((Long) obj).longValue());
        }
    }

    public ScriptableObject asScriptable() {
        ScriptableObject object = ScriptableObjectHelper.createEmpty();
        object.put("id", object, Integer.valueOf(this.id));
        object.put("count", object, Integer.valueOf(this.count));
        object.put("data", object, Integer.valueOf(this.data));
        object.put("extra", object, this.extra);
        return object;
    }

    public JSONObject asJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", this.id);
            json.put("count", this.count);
            json.put("data", this.data);
        } catch (JSONException e) {
        }
        return json;
    }

    public boolean isEmpty() {
        return this.id == 0 && this.count == 0 && this.data == 0 && this.extra == null;
    }

    public long getExtraPtr() {
        throw new UnsupportedOperationException();
    }

    public int getMaxStackSize() {
        throw new UnsupportedOperationException();
    }

    public int getMaxDamage() {
        throw new UnsupportedOperationException();
    }

    public String getItemName() {
        throw new UnsupportedOperationException();
    }

    public boolean isGlint() {
        throw new UnsupportedOperationException();
    }

    public Object getItemModel() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("ItemStack{id=");
        sb.append(this.id);
        sb.append(", count=");
        sb.append(this.count);
        sb.append(", data=");
        sb.append(this.data);
        if (this.extra != null) {
            str = ", extra=" + this.extra;
        } else {
            str = "";
        }
        sb.append(str);
        sb.append('}');
        return sb.toString();
    }
}
