package com.zhekasmirnov.horizon.modloader.mod;

import com.zhekasmirnov.horizon.modloader.repo.location.LocalModLocation;
import com.zhekasmirnov.horizon.modloader.repo.location.ModLocation;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JsonIterator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModManifest {
    private final File file;
    private Module mainModule;
    private List<Directory> directories = new ArrayList<>();
    private List<Module> modules = new ArrayList<>();

    public enum DirectoryType {
        LIBRARY,
        RESOURCE,
        SUBMOD,
        JAVA;

        public static DirectoryType[] valuesCustom() {
            DirectoryType[] valuesCustom = values();
            int length = valuesCustom.length;
            DirectoryType[] directoryTypeArr = new DirectoryType[length];
            System.arraycopy(valuesCustom, 0, directoryTypeArr, 0, length);
            return directoryTypeArr;
        }

        public static DirectoryType byName(String str) {
            char c;
            String lowerCase = str.toLowerCase();
            switch (lowerCase.hashCode()) {
                case -1052618729:
                    if (lowerCase.equals("native")) {
                        c = 2;
                        break;
                    } else {
                        c = 65535;
                        break;
                    }
                case -891535166:
                    if (lowerCase.equals("submod")) {
                        c = 4;
                        break;
                    } else {
                        c = 65535;
                        break;
                    }
                case -341064690:
                    if (lowerCase.equals("resource")) {
                        c = 3;
                        break;
                    } else {
                        c = 65535;
                        break;
                    }
                case 3254818:
                    if (lowerCase.equals("java")) {
                        c = 6;
                        break;
                    } else {
                        c = 65535;
                        break;
                    }
                case 103785528:
                    if (lowerCase.equals("merge")) {
                        c = 5;
                        break;
                    } else {
                        c = 65535;
                        break;
                    }
                case 166208699:
                    if (lowerCase.equals("library")) {
                        c = 0;
                        break;
                    } else {
                        c = 65535;
                        break;
                    }
                case 539264074:
                    if (lowerCase.equals("executable")) {
                        c = 1;
                        break;
                    } else {
                        c = 65535;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                case 2:
                    return LIBRARY;
                case 3:
                    return RESOURCE;
                case 4:
                case 5:
                    return SUBMOD;
                case 6:
                    return JAVA;
                default:
                    return null;
            }
        }
    }

    public class Directory {
        public final File file;
        public final DirectoryType type;

        public Directory(File file, DirectoryType directoryType) {
            this.file = file;
            this.type = directoryType;
        }

        public Directory(JSONObject jSONObject) {
            String optString = jSONObject.optString("path");
            if (optString == null) {
                throw new RuntimeException("mod manifest directory parameter is missing: path");
            }
            String optString2 = jSONObject.optString("type");
            if (optString2 == null) {
                throw new RuntimeException("mod manifest directory parameter is missing: type");
            }
            File file = new File(ModManifest.this.file.getParent(), optString);
            if (!file.exists()) {
                throw new RuntimeException("mod manifest directory parameter is invalid: path " + optString + " (" + file + ") does not exist");
            }
            if (!file.isDirectory()) {
                throw new RuntimeException("mod manifest directory parameter is invalid: path " + optString + " (" + file + ") is not a directory");
            }
            DirectoryType byName = DirectoryType.byName(optString2);
            if (byName == null) {
                throw new RuntimeException("mod manifest directory parameter is invalid: type " + optString2 + " does not exist");
            }
            this.file = file;
            this.type = byName;
        }

        public ModLocation asModLocation() {
            return new LocalModLocation(this.file);
        }
    }

    public class Module {
        public final String author;
        public final String description;
        public final String name;
        public final String nameId;
        public final int versionCode;
        public final String versionName;

        public Module(String str, JSONObject jSONObject) {
            this.nameId = str;
            this.name = jSONObject.optString("name");
            this.author = jSONObject.optString("author");
            this.description = jSONObject.optString("description");
            JSONObject optJSONObject = jSONObject.optJSONObject("version");
            if (optJSONObject != null) {
                this.versionName = optJSONObject.optString("name");
                this.versionCode = optJSONObject.optInt("code");
                return;
            }
            this.versionName = "unknown";
            this.versionCode = 0;
        }

        public String getDisplayedDescription() {
            String str = this.description;
            return (str == null || str.length() <= 0) ? "No description provided" : this.description;
        }
    }

    public ModManifest(File file) throws IOException, JSONException {
        this.mainModule = null;
        this.file = file;
        JSONObject readJSON = FileUtils.readJSON(file);
        JSONArray optJSONArray = readJSON.optJSONArray("directories");
        if (optJSONArray != null) {
            Iterator<Object> it = new JsonIterator<>(optJSONArray).iterator();
            while (it.hasNext()) {
                Object next = it.next();
                if (next instanceof JSONObject) {
                    this.directories.add(new Directory((JSONObject) next));
                }
            }
        }
        JSONObject optJSONObject = readJSON.optJSONObject("modules");
        if (optJSONObject != null) {
            Iterator<Object> it2 = new JsonIterator<>(optJSONObject).iterator();
            while (it2.hasNext()) {
                String str = (String) it2.next();
                JSONObject optJSONObject2 = optJSONObject.optJSONObject(str);
                if (optJSONObject2 != null) {
                    this.modules.add(new Module(str, optJSONObject2));
                }
            }
        }
        JSONObject optJSONObject3 = readJSON.optJSONObject("info");
        if (optJSONObject3 != null) {
            this.mainModule = new Module("<mod-info>", optJSONObject3);
        } else if (this.modules.size() > 0) {
            this.mainModule = this.modules.get(0);
        } else {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("name", file.getParentFile().getName());
            this.mainModule = new Module("<default-mod-info>", jSONObject);
        }
    }

    public File getParentDirectory() {
        return this.file.getParentFile();
    }

    public List<Directory> getDirectories() {
        return this.directories;
    }

    public List<Module> getModules() {
        return this.modules;
    }

    public Module getMainModule() {
        return this.mainModule;
    }

    public String getName() {
        return this.mainModule.name;
    }
}
