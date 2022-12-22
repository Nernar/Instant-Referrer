package com.zhekasmirnov.apparatus.minecraft.enums;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersion;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class EnumsContainer {
    private final Map<String, Scope> scopeMap = new HashMap<>();
    private final MinecraftVersion version;

    public static class Scope {
        private final Map<String, Object> map = new HashMap<>();
        private final Map<Object, String> inverseMap = new HashMap<>();

        public Set<String> getAllEnumNames() {
            return this.map.keySet();
        }

        public Object getEnum(String name) {
            return this.map.get(name);
        }

        public String getKeyForEnum(Object value) {
            return this.inverseMap.get(value);
        }

        public void put(String name, Object value) {
            this.map.put(name, value);
            this.inverseMap.put(value, name);
        }

        public void addEnumsFromJson(JSONObject scopeJson) {
            Iterator<String> it = scopeJson.keys();
            while (it.hasNext()) {
                String name = it.next();
                Object value = scopeJson.opt(name);
                if (value != null) {
                    put(name, value);
                }
            }
        }
    }

    public EnumsContainer(MinecraftVersion version) {
        this.version = version;
    }

    public MinecraftVersion getVersion() {
        return this.version;
    }

    public Set<String> getAllScopeNames() {
        return this.scopeMap.keySet();
    }

    public Scope getScope(String scopeName) {
        return this.scopeMap.get(scopeName);
    }

    public Scope getOrAddScope(String scopeName) {
        Scope scope = this.scopeMap.get(scopeName);
        if (scope == null) {
            Map<String, Scope> map = this.scopeMap;
            Scope scope2 = new Scope();
            map.put(scopeName, scope2);
            return scope2;
        }
        return scope;
    }

    public Object getEnum(String scopeName, String name) {
        Scope scope = this.scopeMap.get(scopeName);
        if (scope != null) {
            return scope.getEnum(name);
        }
        return null;
    }

    public String getKeyForEnum(String scopeName, Object value) {
        Scope scope = this.scopeMap.get(scopeName);
        if (scope != null) {
            return scope.getKeyForEnum(value);
        }
        return null;
    }

    public void addEnumsFromJson(JSONObject enumsJson) {
        Iterator<String> it = enumsJson.keys();
        while (it.hasNext()) {
            String scopeName = it.next();
            JSONObject scopeJson = enumsJson.optJSONObject(scopeName);
            if (scopeJson != null) {
                getOrAddScope(scopeName).addEnumsFromJson(scopeJson);
            }
        }
    }
}
