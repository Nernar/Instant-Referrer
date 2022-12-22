package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class ModPackPreferences {
    private final File file;
    private JSONObject json;
    private final ModPack modPack;

    public ModPackPreferences(ModPack modPack, String fileName) {
        this.modPack = modPack;
        this.file = new File(modPack.getRootDirectory(), fileName);
    }

    public ModPack getModPack() {
        return this.modPack;
    }

    public File getFile() {
        return this.file;
    }

    public ModPackPreferences reload() {
        try {
            this.json = FileUtils.readJSON(this.file);
        } catch (IOException | JSONException e) {
            if (this.json == null) {
                this.json = new JSONObject();
            }
        }
        return this;
    }

    public ModPackPreferences save() {
        reloadIfRequired();
        try {
            FileUtils.writeJSON(this.file, this.json);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return this;
    }

    private void reloadIfRequired() {
        if (this.json == null) {
            reload();
        }
    }

    public String getString(String key, String fallback) {
        reloadIfRequired();
        return this.json.optString(key, fallback);
    }

    public int getInt(String key, int fallback) {
        reloadIfRequired();
        return this.json.optInt(key, fallback);
    }

    public long getLong(String key, long fallback) {
        reloadIfRequired();
        return this.json.optLong(key, fallback);
    }

    public double getDouble(String key, double fallback) {
        reloadIfRequired();
        return this.json.optDouble(key, fallback);
    }

    public boolean getBoolean(String key, boolean fallback) {
        reloadIfRequired();
        return this.json.optBoolean(key, fallback);
    }

    public ModPackPreferences setString(String key, String value) {
        reloadIfRequired();
        try {
            this.json.put(key, value);
        } catch (JSONException e) {
        }
        return this;
    }

    public ModPackPreferences setInt(String key, int value) {
        reloadIfRequired();
        try {
            this.json.put(key, value);
        } catch (JSONException e) {
        }
        return this;
    }

    public ModPackPreferences setLong(String key, long value) {
        reloadIfRequired();
        try {
            this.json.put(key, value);
        } catch (JSONException e) {
        }
        return this;
    }

    public ModPackPreferences setDouble(String key, double value) {
        reloadIfRequired();
        try {
            this.json.put(key, value);
        } catch (JSONException e) {
        }
        return this;
    }

    public ModPackPreferences setBoolean(String key, boolean value) {
        reloadIfRequired();
        try {
            this.json.put(key, value);
        } catch (JSONException e) {
        }
        return this;
    }
}
