package com.zhekasmirnov.apparatus.minecraft.enums;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersion;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class EnumsScopeInfo {
    private static final Map<String, EnumsScopeInfo> scopeInfoMap = new HashMap<>();
    private MinecraftVersion jsScopeVersion;
    private String typeName;

    static {
        try {
            JSONObject scopeInfoJson = FileTools.getAssetAsJSON("innercore/enum/enum-scopes.json");
            Iterator<String> it = scopeInfoJson.keys();
            while (it.hasNext()) {
                String name = it.next();
                JSONObject json = scopeInfoJson.optJSONObject(name);
                if (json != null) {
                    scopeInfoMap.put(name, new EnumsScopeInfo(json));
                }
            }
        } catch (JSONException | IOException e) {
            ICLog.e("ERROR", "EnumsScopeInfo failed to get scope info from assets", e);
        }
    }

    public static EnumsScopeInfo getForScope(String name) {
        return scopeInfoMap.get(name);
    }

    public static Set<String> getAllScopesWithInfo() {
        return scopeInfoMap.keySet();
    }

    public EnumsScopeInfo() {
    }

    public EnumsScopeInfo(JSONObject json) {
        if (json != null) {
            String typeName = json.optString("typename");
            if (typeName != null) {
                setTypeName(typeName);
            }
            MinecraftVersion jsScopeVersion = MinecraftVersions.getVersionByCode(json.optInt("jsScopeVersion"));
            setJsScopeVersion(jsScopeVersion != null ? jsScopeVersion : MinecraftVersions.getCurrent());
        }
    }

    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public MinecraftVersion getJsScopeVersion() {
        return this.jsScopeVersion;
    }

    public void setJsScopeVersion(MinecraftVersion jsScopeVersion) {
        this.jsScopeVersion = jsScopeVersion;
    }
}
