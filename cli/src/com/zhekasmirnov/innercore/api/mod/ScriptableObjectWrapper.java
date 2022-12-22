package com.zhekasmirnov.innercore.api.mod;

import com.zhekasmirnov.innercore.api.log.ICLog;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ScriptableObjectWrapper {
    private Scriptable scriptable;
    private NativeArray scriptableArray;
    private ScriptableObject scriptableObject;

    private void setWrappedObject(Scriptable scriptable) {
        this.scriptable = scriptable;
        this.scriptableObject = scriptable instanceof ScriptableObject ? (ScriptableObject) scriptable : null;
        this.scriptableArray = scriptable instanceof NativeArray ? (NativeArray) scriptable : null;
    }

    public ScriptableObjectWrapper(Scriptable scriptable) {
        setWrappedObject(scriptable);
    }

    public ScriptableObjectWrapper() {
        this(ScriptableObjectHelper.createEmpty());
    }

    private void parseJson(JSONObject json) {
        setWrappedObject(ScriptableObjectHelper.createEmpty());
        JSONArray keys = json.names();
        if (keys != null) {
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.optString(i);
                if (key != null) {
                    Object value = json.opt(key);
                    if (value instanceof JSONObject) {
                        put(key, new ScriptableObjectWrapper((JSONObject) value));
                    } else if (value instanceof JSONArray) {
                        put(key, new ScriptableObjectWrapper((JSONArray) value));
                    } else {
                        put(key, value);
                    }
                }
            }
        }
    }

    public ScriptableObjectWrapper(JSONObject json) {
        parseJson(json);
    }

    private void parseJson(JSONArray json) {
        setWrappedObject(ScriptableObjectHelper.createArray(new Object[json.length()]));
        for (int key = 0; key < json.length(); key++) {
            Object value = json.opt(key);
            if (value instanceof JSONObject) {
                put(Integer.valueOf(key), new ScriptableObjectWrapper((JSONObject) value));
            } else if (value instanceof JSONArray) {
                put(Integer.valueOf(key), new ScriptableObjectWrapper((JSONArray) value));
            } else {
                put(Integer.valueOf(key), value);
            }
        }
    }

    public ScriptableObjectWrapper(JSONArray json) {
        parseJson(json);
    }

    public ScriptableObjectWrapper(String json) {
        try {
            if (json.length() > 0) {
                if (json.startsWith("[")) {
                    parseJson(new JSONArray(json));
                } else {
                    parseJson(new JSONObject(json));
                }
            }
        } catch (JSONException error) {
            ICLog.e("ERROR", "failed to parse json string for ScriptableObjectWrapper", error);
        }
    }

    public boolean has(Object key) {
        if (key instanceof Integer) {
            return this.scriptable.has(((Integer) key).intValue(), this.scriptable);
        }
        return this.scriptable.has((String) key, this.scriptable);
    }

    public void putRaw(Object key, Object value) {
        if (key instanceof Integer) {
            this.scriptable.put(((Integer) key).intValue(), this.scriptable, value);
        } else {
            this.scriptable.put((String) key, this.scriptable, value);
        }
    }

    public void put(Object key, Object value) {
        if (value instanceof ScriptableObjectWrapper) {
            value = ((ScriptableObjectWrapper) value).getWrapped();
        }
        putRaw(key, value);
    }

    public boolean remove(Object key) {
        if (has(key)) {
            if (key instanceof Integer) {
                this.scriptable.delete(((Integer) key).intValue());
                return true;
            }
            this.scriptable.delete((String) key);
            return true;
        }
        return false;
    }

    public void insert(int index, Object value) {
        if (this.scriptableArray == null) {
            throw new UnsupportedOperationException("insert works only on arrays!");
        }
        if (has(Integer.valueOf(index))) {
            int id = index;
            Object element = value;
            while (has(Integer.valueOf(id))) {
                Object temp = this.scriptableArray.get(id);
                put(Integer.valueOf(id), element);
                element = temp;
                id++;
            }
            put(Integer.valueOf(id), element);
            return;
        }
        put(Integer.valueOf(index), value);
    }

    public Scriptable getWrapped() {
        return this.scriptable;
    }

    public ScriptableObject getWrappedObject() {
        return this.scriptableObject;
    }

    public Scriptable getWrappedArray() {
        return this.scriptableArray;
    }

    public boolean isArray() {
        return this.scriptableArray != null;
    }

    public boolean isObject() {
        return this.scriptableObject != null;
    }

    public Object get(Object key, Object defaultValue) {
        return key instanceof Integer ? this.scriptable.has(((Integer) key).intValue(), this.scriptable) ? this.scriptable.get(((Integer) key).intValue(), this.scriptable) : defaultValue : this.scriptable.has((String) key, this.scriptable) ? this.scriptable.get((String) key, this.scriptable) : defaultValue;
    }

    public Object get(Object key) {
        return get(key, null);
    }

    public boolean getBoolean(Object key, boolean defaultValue) {
        return key instanceof Integer ? this.scriptable.has(((Integer) key).intValue(), this.scriptable) ? Context.toBoolean(this.scriptable.get(((Integer) key).intValue(), this.scriptable)) : defaultValue : this.scriptable.has((String) key, this.scriptable) ? Context.toBoolean(this.scriptable.get((String) key, this.scriptable)) : defaultValue;
    }

    public boolean getBoolean(Object key) {
        return getBoolean(key, false);
    }

    public String getString(Object key, String defaultValue) {
        if (key instanceof Integer) {
            if (this.scriptable.has(((Integer) key).intValue(), this.scriptable)) {
                return new StringBuilder().append(this.scriptable.get(((Integer) key).intValue(), this.scriptable)).toString();
            }
            return defaultValue;
        } else if (this.scriptable.has((String) key, this.scriptable)) {
            return new StringBuilder().append(this.scriptable.get((String) key, this.scriptable)).toString();
        } else {
            return defaultValue;
        }
    }

    public String getString(Object key) {
        return getString(key, null);
    }

    public Scriptable getScriptable(Object key) {
        Object val = get(key, null);
        if (!(val instanceof Scriptable)) {
            return null;
        }
        return (Scriptable) val;
    }

    public ScriptableObjectWrapper getScriptableWrapper(Object key) {
        Scriptable scriptable = getScriptable(key);
        if (scriptable != null) {
            return new ScriptableObjectWrapper(scriptable);
        }
        return null;
    }

    public long getLong(Object key, long defaultValue) {
        Object val = get(key, Long.valueOf(defaultValue));
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return defaultValue;
    }

    public long getLong(Object key) {
        return getLong(key, 0L);
    }

    public double getDouble(Object key, double defaultValue) {
        Object val = get(key, Double.valueOf(defaultValue));
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return defaultValue;
    }

    public double getDouble(Object key) {
        return getDouble(key, 0.0d);
    }

    public float getFloat(Object key, float defaultValue) {
        return (float) getDouble(key, defaultValue);
    }

    public float getFloat(Object key) {
        return getFloat(key, 0.0f);
    }

    public int getInt(Object key, int defaultValue) {
        return (int) getDouble(key, defaultValue);
    }

    public int getInt(Object key) {
        return getInt(key, 0);
    }

    public Object[] asArray() {
        ArrayList<Object> values = new ArrayList<>();
        Object[] keys = this.scriptable.getIds();
        for (Object key : keys) {
            if (key instanceof String) {
                values.add(this.scriptable.get((String) key, this.scriptable));
            } else {
                values.add(this.scriptable.get(((Integer) key).intValue(), this.scriptable));
            }
        }
        return values.toArray();
    }

    public float[] getFloatArray(Object key, int minLength, float defaultValue) {
        if (getFloat(key, defaultValue) != defaultValue) {
            defaultValue = getFloat(key);
        }
        Scriptable scriptable = getScriptable(key);
        int i = 0;
        if (scriptable != null && (scriptable instanceof NativeArray)) {
            NativeArray array = (NativeArray) scriptable;
            float[] jArray = new float[(int) Math.max(array.getLength(), minLength)];
            while (i < jArray.length) {
                if (i < array.getLength()) {
                    Object val = array.get(i);
                    if (val instanceof Number) {
                        jArray[i] = ((Number) val).floatValue();
                        i++;
                    }
                }
                jArray[i] = defaultValue;
                i++;
            }
            return jArray;
        }
        float[] jArray2 = new float[minLength];
        while (i < minLength) {
            jArray2[i] = defaultValue;
            i++;
        }
        return jArray2;
    }

    public float[] getColorTemplate(Object key, float defaultValue) {
        float[] color = getFloatArray(key, 4, defaultValue);
        ScriptableObjectWrapper wrapper = getScriptableWrapper(key);
        if (wrapper != null) {
            color[0] = wrapper.getFloat("r", color[0]);
            color[1] = wrapper.getFloat("g", color[1]);
            color[2] = wrapper.getFloat("b", color[2]);
            color[3] = wrapper.getFloat("a", color[3]);
        }
        return color;
    }

    public float[] getMinMaxTemplate(Object key, float defaultValue) {
        float[] arr = getFloatArray(key, 2, defaultValue);
        ScriptableObjectWrapper wrapper = getScriptableWrapper(key);
        if (wrapper != null) {
            arr[0] = wrapper.getFloat("min", arr[0]);
            arr[1] = wrapper.getFloat("max", arr[1]);
        }
        if (arr[1] < arr[0]) {
            arr[1] = arr[0];
        }
        return arr;
    }

    public float[] getVec3Template(Object key, float defaultValue) {
        float[] arr = getFloatArray(key, 3, defaultValue);
        ScriptableObjectWrapper wrapper = getScriptableWrapper(key);
        if (wrapper != null) {
            arr[0] = wrapper.getFloat("x", arr[0]);
            arr[1] = wrapper.getFloat("y", arr[1]);
            arr[2] = wrapper.getFloat("z", arr[2]);
        }
        return arr;
    }

    public float[] getUVTemplate(Object key) {
        float[] arr = getFloatArray(key, 4, 0.0f);
        ScriptableObjectWrapper wrapper = getScriptableWrapper(key);
        if (wrapper != null) {
            arr[0] = wrapper.getFloat("u1", arr[0]);
            arr[1] = wrapper.getFloat("v1", arr[1]);
            arr[2] = wrapper.getFloat("u2", arr[2]);
            arr[3] = wrapper.getFloat("v2", arr[3]);
        }
        if (arr[0] == 0.0f && arr[1] == 0.0f && arr[2] == 0.0f && arr[3] == 0.0f) {
            arr[2] = 1.0f;
            arr[3] = 1.0f;
        }
        return arr;
    }
}
