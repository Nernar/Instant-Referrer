package com.zhekasmirnov.innercore.api.runtime.saver.serializer;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class ScriptableSerializer {

    public interface SerializationErrorHandler {
        void handle(Exception exc);
    }

    private static Object avoidInvalidJsonValues(Object object) {
        if (object instanceof Undefined) {
            return null;
        }
        if (object instanceof Number) {
            Number number = (Number) object;
            double d = number.doubleValue();
            float f = number.floatValue();
            if (Double.isNaN(d) || Float.isNaN(f) || Double.isInfinite(d) || Float.isInfinite(f)) {
                return Float.valueOf(0.0f);
            }
        }
        return object;
    }

    private static Object scriptableToJson0(Object input, SerializationErrorHandler handler, Set<ScriptableObject> iteratedObjects) {
        Object input2 = ObjectSaverRegistry.saveOrSkipObject(input);
        if (input2 instanceof ScriptableObject) {
            ScriptableObject scriptableObject = (ScriptableObject) input2;
            if (iteratedObjects.contains(scriptableObject)) {
                return null;
            }
            iteratedObjects.add(scriptableObject);
            int i = 0;
            if (scriptableObject instanceof NativeArray) {
                JSONArray json = new JSONArray();
                Object[] array = ((NativeArray) scriptableObject).toArray();
                int length = array.length;
                while (i < length) {
                    Object element = array[i];
                    json.put(scriptableToJson0(element, handler, iteratedObjects));
                    i++;
                }
                iteratedObjects.remove(scriptableObject);
                return json;
            }
            JSONObject json2 = new JSONObject();
            Object[] ids = scriptableObject.getIds();
            int length2 = ids.length;
            while (i < length2) {
                Object key = ids[i];
                Object value = scriptableObject.get(key);
                try {
                    json2.put(new StringBuilder().append(key).toString(), scriptableToJson0(value, handler, iteratedObjects));
                } catch (JSONException e) {
                    if (handler != null) {
                        handler.handle(e);
                    }
                }
                i++;
            }
            iteratedObjects.remove(scriptableObject);
            return json2;
        }
        return avoidInvalidJsonValues(input2);
    }

    public static Object scriptableToJson(Object object, SerializationErrorHandler handler) {
        return scriptableToJson0(object, handler, new HashSet<>());
    }

    public static Object scriptableFromJson(Object object) {
        if (object instanceof JSONObject) {
            JSONObject json = (JSONObject) object;
            ScriptableObject scriptable = ScriptableObjectHelper.createEmpty();
            Iterator<String> it = json.keys();
            while (it.hasNext()) {
                String key = it.next();
                scriptable.put(key, scriptable, scriptableFromJson(json.opt(key)));
            }
            return ObjectSaverRegistry.readObject(scriptable);
        } else if (object instanceof JSONArray) {
            JSONArray json2 = (JSONArray) object;
            List<Object> array = new ArrayList<>();
            for (int i = 0; i < json2.length(); i++) {
                array.add(scriptableFromJson(json2.opt(i)));
            }
            return ScriptableObjectHelper.createArray(array);
        } else {
            return object;
        }
    }

    public static String jsonToString(Object json) {
        return new StringBuilder().append(json).toString();
    }

    public static Object stringToJson(String str) throws JSONException {
        if (str == null) {
            return null;
        }
        return new JSONObject("{\"a\": " + str + "}").get("a");
    }
}
