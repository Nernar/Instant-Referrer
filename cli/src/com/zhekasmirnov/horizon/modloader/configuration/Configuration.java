package com.zhekasmirnov.horizon.modloader.configuration;

import com.zhekasmirnov.horizon.util.JsonIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Configuration {
    public abstract Object delete(String str);

    public abstract Object get(String str);

    public abstract <T> T get(String str, Class<T> cls);

    public abstract Configuration getChild(String str);

    public abstract boolean isContainer(String str);

    public abstract boolean isReadOnly();

    public abstract void load();

    public abstract void refresh();

    public abstract void save();

    public abstract boolean set(String str, Object obj);

    public int getInt(String str) {
        Object obj = get(str);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return 0;
    }

    public float getFloat(String str) {
        Object obj = get(str);
        if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        }
        return 0.0f;
    }

    public double getDouble(String str) {
        Object obj = get(str);
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return 0.0d;
    }

    public long getLong(String str) {
        Object obj = get(str);
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return 0L;
    }

    public String getString(String str) {
        Object obj = get(str);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public boolean getBoolean(String str) {
        Object obj = get(str);
        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getArray(String str) {
        JSONArray jSONArray = (JSONArray) get(str, JSONArray.class);
        if (jSONArray != null) {
            ArrayList<T> arrayList = new ArrayList<>();
            Iterator<T> it = (Iterator<T>) new JsonIterator<>(jSONArray).iterator();
            while (it.hasNext()) {
                try {
                    arrayList.add(it.next());
                } catch (ClassCastException e) {
                }
            }
            return arrayList;
        }
        return null;
    }

    private static boolean checkSameType(Object obj, Object obj2) {
        return (obj == null || obj2 == null || ((!(obj instanceof Number) || !(obj2 instanceof Number)) && ((!(obj instanceof CharSequence) || !(obj2 instanceof CharSequence)) && obj.getClass() != obj2.getClass()))) ? false : true;
    }

    private void checkAndRestore(String str, JSONObject jSONObject) {
        Iterator<Object> it = new JsonIterator<>(jSONObject).iterator();
        while (it.hasNext()) {
            String str2 = (String) it.next();
            Object opt = jSONObject.opt(str2);
            String str3 = String.valueOf(str) + str2;
            if (opt instanceof JSONObject) {
                if (!isContainer(str3)) {
                    delete(str3);
                }
                checkAndRestore(String.valueOf(str3) + ".", (JSONObject) opt);
            } else {
                if (isContainer(str3)) {
                    delete(str3);
                }
                if (!checkSameType(get(str3), opt)) {
                    set(str3, opt);
                }
            }
        }
    }

    public void checkAndRestore(JSONObject jSONObject) {
        if (isReadOnly()) {
            return;
        }
        checkAndRestore("", jSONObject);
        save();
    }

    public void checkAndRestore(String str) {
        try {
            checkAndRestore(new JSONObject(str));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
