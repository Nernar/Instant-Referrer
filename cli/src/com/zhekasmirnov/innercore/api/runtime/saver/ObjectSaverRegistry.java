package com.zhekasmirnov.innercore.api.runtime.saver;

import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaver;
import java.util.HashMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

public class ObjectSaverRegistry {
    public static final String PROPERTY_IGNORE_SAVE = "_json_ignore";
    public static final String PROPERTY_SAVER_ID = "_json_saver_id";
    private static HashMap<Integer, ObjectSaver> saverMap = new HashMap<>();
    private static HashMap<Integer, ObjectSaver> saverByObjectHash = new HashMap<>();
    private static HashMap<Integer, String> saverNameById = new HashMap<>();
    private static final ScriptableObject scope = Context.enter().initStandardObjects();

    public static int registerSaver(String name, ObjectSaver saver) {
        int saverId = name.hashCode();
        while (saverMap.containsKey(Integer.valueOf(saverId))) {
            saverId++;
        }
        saverMap.put(Integer.valueOf(saverId), saver);
        saverNameById.put(Integer.valueOf(saverId), name);
        saver.setSaverId(saverId);
        return saverId;
    }

    public static String getSaverName(int saverId) {
        return saverNameById.get(Integer.valueOf(saverId));
    }

    static Object unwrapIfNeeded(Object object) {
        if (object instanceof Wrapper) {
            return ((Wrapper) object).unwrap();
        }
        return object;
    }

    public static ObjectSaver getSaverFor(Object object) {
        ObjectSaver saver = saverByObjectHash.get(Integer.valueOf(object.hashCode()));
        if (saver != null) {
            return saver;
        }
        if (object instanceof ScriptableObject) {
            Object val = ((ScriptableObject) object).get("_json_saver_id");
            if (val instanceof Number) {
                int id = ((Number) val).intValue();
                return saverMap.get(Integer.valueOf(id));
            }
            return null;
        }
        return null;
    }

    public static ScriptableObject saveObject(Object object) {
        Object object2 = unwrapIfNeeded(object);
        ObjectSaver saver = getSaverFor(object2);
        if (saver != null) {
            try {
                ScriptableObject result = saver.save(object2);
                if (result != null) {
                    result.put("_json_saver_id", result, Integer.valueOf(saver.getSaverId()));
                }
                return result;
            } catch (Throwable err) {
                WorldDataSaver.logErrorStatic("error in saving object of saver type " + getSaverName(saver.getSaverId()), err);
                return null;
            }
        } else if (object2 instanceof ScriptableObject) {
            return (ScriptableObject) object2;
        } else {
            return null;
        }
    }

    public static Object saveOrSkipObject(Object object) {
        Object object2 = unwrapIfNeeded(object);
        if (object2 == null || object2 == Undefined.instance) {
            return null;
        }
        ObjectSaver saver = getSaverFor(object2);
        if (saver != null) {
            try {
                ScriptableObject result = saver.save(object2);
                if (result != null) {
                    result.put("_json_saver_id", result, Integer.valueOf(saver.getSaverId()));
                }
                return result;
            } catch (Throwable err) {
                WorldDataSaver.logErrorStatic("error in saving object of saver type " + getSaverName(saver.getSaverId()), err);
                return null;
            }
        }
        return object2;
    }

    public static ScriptableObject saveObjectAndCheckSaveIgnoring(Object object) {
        if (object instanceof ScriptableObject) {
            Object val = ((ScriptableObject) object).get("_json_ignore");
            if ((val instanceof Boolean) && ((Boolean) val).booleanValue()) {
                return null;
            }
        }
        return saveObject(object);
    }

    public static Scriptable readObject(ScriptableObject object) {
        ObjectSaver saver = getSaverFor(object);
        if (saver != null) {
            try {
                Object result = saver.read(object);
                if (!(result instanceof Scriptable)) {
                    result = Context.javaToJS(result, scope);
                    if (!(result instanceof Scriptable)) {
                        return null;
                    }
                }
                return (Scriptable) result;
            } catch (Throwable err) {
                WorldDataSaver.logErrorStatic("error in reading object of saver type " + getSaverName(saver.getSaverId()), err);
                return null;
            }
        }
        return object;
    }

    public static void registerObject(Object object, int saverId) {
        if (!saverMap.containsKey(Integer.valueOf(saverId))) {
            throw new IllegalArgumentException("no saver found for id " + saverId + " use only registerObjectSaver return values");
        }
        saverByObjectHash.put(Integer.valueOf(unwrapIfNeeded(object).hashCode()), saverMap.get(Integer.valueOf(saverId)));
    }

    public static void setObjectIgnored(ScriptableObject object, boolean ignore) {
        object.put("_json_ignore", object, Boolean.valueOf(ignore));
    }
}
