package com.zhekasmirnov.horizon.modloader.library;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JSONUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LibraryManifest {
    private JSONObject content;
    private List<String> dependencies;
    private File file;
    private List<String> include;
    private boolean isSharedLibrary;
    private String name;
    private int version;

    public LibraryManifest(File file) throws IOException, JSONException {
        this.name = null;
        this.isSharedLibrary = false;
        this.version = 0;
        this.include = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.file = file;
        JSONObject readJSON = FileUtils.readJSON(file);
        this.content = readJSON;
        JSONObject optJSONObject = readJSON.optJSONObject("shared");
        if (optJSONObject != null) {
            String optString = optJSONObject.optString("name");
            this.name = optString;
            if ("unnamed".equals(optString)) {
                throw new IllegalArgumentException("name 'unnamed' of shared library is illegal");
            }
            if (this.name == null) {
                throw new IllegalArgumentException("name of shared library cannot be null");
            }
            JSONArray optJSONArray = optJSONObject.optJSONArray("include");
            if (optJSONArray != null) {
                this.include = JSONUtils.toList(optJSONArray);
            }
        }
        JSONObject optJSONObject2 = this.content.optJSONObject("library");
        if (optJSONObject2 != null) {
            int optInt = optJSONObject2.optInt("version", -1);
            this.version = optInt;
            if (optInt < 0) {
                throw new IllegalArgumentException("no library version is defined or its incorrect");
            }
            if (this.name == null) {
                throw new IllegalArgumentException("library must have a shared name");
            }
            this.isSharedLibrary = true;
        } else {
            this.isSharedLibrary = false;
        }
        JSONArray optJSONArray2 = this.content.optJSONArray("depends");
        if (optJSONArray2 != null) {
            this.dependencies = JSONUtils.toList(optJSONArray2);
        }
    }

    public File getFile() {
        return this.file;
    }

    public String getName() {
        return this.name;
    }

    public String getSoName() {
        if (this.name != null) {
            return "lib" + this.name + ".so";
        }
        return "unnamed";
    }

    public int getVersion() {
        return this.version;
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }

    public List<String> getInclude() {
        return this.include;
    }

    public boolean isSharedLibrary() {
        return this.isSharedLibrary;
    }

    public boolean isShared() {
        return this.name != null;
    }
}
