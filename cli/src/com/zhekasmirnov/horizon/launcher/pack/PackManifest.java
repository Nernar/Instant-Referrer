package com.zhekasmirnov.horizon.launcher.pack;

import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JsonIterator;
import com.zhekasmirnov.horizon.util.LocaleUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PackManifest {
    public final List<ClassInfo> activities;
    private JSONObject content;
    public final String description;
    public final String developer;
    public final List<ClassInfo> environmentClasses;
    public final String game;
    public final String gameVersion;
    public final List<String> keepDirectories;
    public final boolean optClearActivityStack;
    public final String pack;
    public final String packUUID;
    public final String packVersion;
    public final int packVersionCode;
    public final HashMap<String, String> savesHoldersInfo;

    public PackManifest(JSONObject jSONObject) throws JSONException {
        this.activities = new ArrayList<>();
        this.environmentClasses = new ArrayList<>();
        this.keepDirectories = new ArrayList<>();
        this.savesHoldersInfo = new HashMap<>();
        this.content = jSONObject;
        this.game = jSONObject.getString("game");
        this.gameVersion = this.content.optString("gameVersion", null);
        this.pack = this.content.getString("pack");
        this.packVersion = this.content.optString("packVersion", null);
        this.packUUID = this.content.optString("uuid", null);
        this.packVersionCode = this.content.getInt("packVersionCode");
        this.developer = LocaleUtils.resolveLocaleJsonProperty(this.content, "developer");
        this.description = LocaleUtils.resolveLocaleJsonProperty(this.content, "description");
        this.optClearActivityStack = this.content.optBoolean("clearActivityStack", true);
        Object opt = this.content.opt("activity");
        if (opt != null) {
            this.activities.add(ClassInfo.fromObject(opt));
        }
        JSONArray optJSONArray = this.content.optJSONArray("activities");
        if (optJSONArray != null) {
            Iterator<Object> it = new JsonIterator<>(optJSONArray).iterator();
            while (it.hasNext()) {
                this.activities.add(ClassInfo.fromObject(it.next()));
            }
        }
        Object opt2 = this.content.opt("environmentClass");
        if (opt2 != null) {
            this.environmentClasses.add(ClassInfo.fromObject(opt2));
        }
        JSONArray optJSONArray2 = this.content.optJSONArray("environmentClasses");
        if (optJSONArray2 != null) {
            Iterator<Object> it2 = new JsonIterator<>(optJSONArray2).iterator();
            while (it2.hasNext()) {
                this.environmentClasses.add(ClassInfo.fromObject(it2.next()));
            }
        }
        JSONArray optJSONArray3 = this.content.optJSONArray("keepDirectories");
        if (optJSONArray3 != null) {
            Iterator<Object> it3 = new JsonIterator<>(optJSONArray3).iterator();
            while (it3.hasNext()) {
                String str = (String) it3.next();
                if (str != null) {
                    this.keepDirectories.add(str);
                }
            }
        }
        JSONArray optJSONArray4 = this.content.optJSONArray("saves");
        if (optJSONArray4 != null) {
            Iterator<Object> it4 = new JsonIterator<>(optJSONArray4).iterator();
            while (it4.hasNext()) {
                JSONObject jSONObject2 = (JSONObject) it4.next();
                if (jSONObject2 != null) {
                    String optString = jSONObject2.optString("name");
                    String optString2 = jSONObject2.optString("path");
                    if (optString != null && optString2 != null && !optString.equals("data")) {
                        this.savesHoldersInfo.put(optString, optString2.replaceAll("\\{storage\\}", new File(System.getProperty("user.dir")).getAbsolutePath()).replaceAll("\\{internal\\}", Environment.getDataDirFile().getAbsolutePath()).replaceAll("\\{package_name\\}", "com.zheka.horizon"));
                    }
                }
            }
        }
    }

    public PackManifest(File file) throws IOException, JSONException {
        this(FileUtils.readJSON(file));
    }

    public static class ClassInfo {
        public final String clazz;
        public final String description;
        public final String name;

        public ClassInfo(String str) {
            this.clazz = str;
            this.name = str;
            this.description = null;
        }

        public ClassInfo(JSONObject jSONObject) {
            String optString = jSONObject.optString("class");
            this.clazz = optString;
            if (optString != null) {
                this.name = jSONObject.optString("name", optString);
                this.description = jSONObject.optString("description");
                return;
            }
            throw new IllegalArgumentException("class info missing class package ('class' field)");
        }

        public static ClassInfo fromObject(Object obj) {
            if (obj instanceof JSONObject) {
                return new ClassInfo((JSONObject) obj);
            }
            if (obj instanceof String) {
                return new ClassInfo((String) obj);
            }
            throw new IllegalArgumentException("failed to parse class info: " + obj);
        }

        public Class<?> getDeclaredClass(ClassLoader classLoader) {
            try {
                return classLoader.loadClass(this.clazz);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("failed to load declared class: " + this.clazz, e);
            }
        }

        public Class<?> getDeclaredClass() {
            return getDeclaredClass(getClass().getClassLoader());
        }
    }

    public ClassInfo getActivityInfoForName(String s) {
        for (final ClassInfo classInfo : this.activities) {
            if (s == null || s.equals(classInfo.name)) {
                return classInfo;
            }
        }
        return null;
    }

    public JSONObject getContent() {
        return this.content;
    }

    public String getPackVersionString() {
        String str = this.packVersion;
        if (str != null) {
            return str;
        }
        return new StringBuilder().append(this.packVersionCode).toString();
    }
}
