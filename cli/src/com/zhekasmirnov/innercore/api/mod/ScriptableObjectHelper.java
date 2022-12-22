package com.zhekasmirnov.innercore.api.mod;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.utils.UIUtils;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

public class ScriptableObjectHelper {
    private static ScriptableObject defaultScope = Context.enter().initStandardObjects();

    public static Object getProperty(ScriptableObject object, String key, Object fallback) {
        if (object.has(key, object)) {
            return object.get(key);
        }
        return fallback;
    }

    public static Object getJavaProperty(ScriptableObject object, String key, Class<?> clazz, Object fallback) {
        try {
            return Context.jsToJava(getProperty(object, key, fallback), clazz);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static ScriptableObject getScriptableObjectProperty(ScriptableObject object, String key, ScriptableObject fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof ScriptableObject) {
            return (ScriptableObject) result;
        }
        return fallback;
    }

    public static NativeArray getNativeArrayProperty(ScriptableObject object, String key, NativeArray fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof NativeArray) {
            return (NativeArray) result;
        }
        return fallback;
    }

    public static String getStringProperty(ScriptableObject object, String key, CharSequence fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof CharSequence) {
            return result.toString();
        }
        if (fallback != null) {
            return fallback.toString();
        }
        return null;
    }

    public static boolean getBooleanProperty(ScriptableObject object, String key, boolean fallback) {
        Object result = getProperty(object, key, Boolean.valueOf(fallback));
        if (result instanceof Boolean) {
            return ((Boolean) result).booleanValue();
        }
        return fallback;
    }

    public static float getFloatProperty(ScriptableObject object, String key, float fallback) {
        Object result = getProperty(object, key, Float.valueOf(fallback));
        if (result instanceof Double) {
            return (float) ((Double) result).doubleValue();
        }
        if (result instanceof Integer) {
            return ((Integer) result).intValue();
        }
        if (result instanceof Float) {
            return ((Float) result).floatValue();
        }
        return fallback;
    }

    public static int getIntProperty(ScriptableObject object, String key, int fallback) {
        Object result = getProperty(object, key, Integer.valueOf(fallback));
        if (result instanceof Double) {
            return (int) ((Double) result).doubleValue();
        }
        if (result instanceof Integer) {
            return ((Integer) result).intValue();
        }
        if (result instanceof Float) {
            return (int) ((Float) result).floatValue();
        }
        return fallback;
    }

    public static int getLongProperty(ScriptableObject object, String key, int fallback) {
        Object result = getProperty(object, key, Integer.valueOf(fallback));
        if (result instanceof Double) {
            return (int) ((Double) result).doubleValue();
        }
        if (result instanceof Integer) {
            return ((Integer) result).intValue();
        }
        if (result instanceof Float) {
            return (int) ((Float) result).floatValue();
        }
        return fallback;
    }

    public static Object getPropByPath(ScriptableObject object, String path, Object fallback) {
        int dotIndex = path.indexOf(".");
        if (dotIndex == -1) {
            if (object.has(path, object)) {
                return object.get(path);
            }
        } else {
            String name = path.substring(0, dotIndex);
            if (object.has(name, object)) {
                Object obj = object.get(name);
                if (obj instanceof ScriptableObject) {
                    return getPropByPath((ScriptableObject) obj, path.substring(dotIndex + 1), fallback);
                }
            } else {
                Logger.debug("INNERCORE", String.valueOf(name) + " not found");
            }
        }
        return fallback;
    }

    public ScriptableObject createFromString(String str) {
        try {
            Context ctx = Compiler.assureContextForCurrentThread();
            ScriptableObject obj = (ScriptableObject) ctx.evaluateString(new ScriptableObject() {
                @Override
                public String getClassName() {
                    return "Empty Scope";
                }
            }, str, "Scriptable From String", 0, null);
            return obj;
        } catch (Exception e) {
            UIUtils.processError(e);
            return null;
        }
    }

    public static ScriptableObject getDefaultScope() {
        return defaultScope;
    }

    public static ScriptableObject createEmpty() {
        return (ScriptableObject) Context.enter().newObject(defaultScope);
    }

    public static <K, V> ScriptableObject createFromMap(Map<K, V> map) {
        if (map == null) {
            return null;
        }
        ScriptableObject object = (ScriptableObject) Context.enter().newObject(defaultScope);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            object.put(new StringBuilder().append(entry.getKey()).toString(), object, entry.getValue());
        }
        return object;
    }

    public static NativeArray createEmptyArray() {
        return (NativeArray) Context.enter().newArray(defaultScope, 0);
    }

    public static NativeArray createArray(Object[] arr) {
        return (NativeArray) Context.enter().newArray(defaultScope, arr);
    }

    public static NativeArray createArray(List<?> arr) {
        return (NativeArray) Context.enter().newArray(defaultScope, arr.toArray());
    }

    public static JSONObject toJSON(ScriptableObject obj) {
        try {
            return toJSON(obj, new JSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject toJSON(ScriptableObject obj, JSONObject json) throws JSONException {
        if (obj == null) {
            return null;
        }
        Object[] keys = obj.getAllIds();
        for (Object key : keys) {
            Object val = obj.get(key);
            if (val.getClass().isPrimitive()) {
                json.put(key.toString(), val);
            } else if (val instanceof CharSequence) {
                json.put(key.toString(), val.toString());
            } else if (val instanceof ScriptableObject) {
                json.put(key.toString(), toJSON((ScriptableObject) val, new JSONObject()));
            }
        }
        return json;
    }
}
