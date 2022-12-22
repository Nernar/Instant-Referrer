package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

public class WorldDataScopeRegistry {
    private static final WorldDataScopeRegistry instance = new WorldDataScopeRegistry();
    private final Map<String, SaverScope> scopeMap = new HashMap<>();

    public interface MissingScopeHandler {
        void handle(String str, Object obj);
    }

    public interface SaverScope {
        void readJson(Object obj) throws Exception;

        Object saveAsJson() throws Exception;
    }

    public interface SavesErrorHandler {
        void handle(String str, Throwable th);
    }

    static {
        instance.addScope("_legacy_global", new ScriptableSaverScope() {
            @Override
            public void read(Object object) {
                if (object == null) {
                    object = ScriptableObjectHelper.createEmpty();
                }
                Callback.invokeAPICallback("ReadSaves", object);
            }

            @Override
            public ScriptableObject save() {
                ScriptableObject scope = ScriptableObjectHelper.createEmpty();
                Callback.invokeAPICallback("WriteSaves", scope);
                return scope;
            }
        });
    }

    public static WorldDataScopeRegistry getInstance() {
        return instance;
    }

    public void addScope(String name, SaverScope scope) {
        if (scope == null) {
            return;
        }
        while (this.scopeMap.containsKey(name)) {
            name = String.valueOf(name) + (name.hashCode() & 255);
        }
        this.scopeMap.put(name, scope);
    }

    public void readAllScopes(JSONObject json, SavesErrorHandler errorHandler, MissingScopeHandler missingScopeHandler) {
        for (Map.Entry<String, SaverScope> entry : this.scopeMap.entrySet()) {
            String key = entry.getKey();
            Object data = json.opt(key);
            try {
                entry.getValue().readJson(data);
            } catch (Throwable err) {
                errorHandler.handle(key, err);
            }
        }
        Iterator<String> it = json.keys();
        while (it.hasNext()) {
            String key2 = it.next();
            if (!this.scopeMap.containsKey(key2)) {
                missingScopeHandler.handle(key2, json.opt(key2));
            }
        }
    }

    public void saveAllScopes(JSONObject json, SavesErrorHandler handler) {
        for (Map.Entry<String, SaverScope> entry : this.scopeMap.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue().saveAsJson());
            } catch (Throwable err) {
                handler.handle(entry.getKey(), err);
            }
        }
    }
}
