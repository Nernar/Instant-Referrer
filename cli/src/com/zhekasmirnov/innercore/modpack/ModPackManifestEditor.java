package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class ModPackManifestEditor {
    private final File file;
    private final JSONObject json;
    private final ModPackManifest manifest;

    ModPackManifestEditor(ModPackManifest manifest, File file) throws IOException, JSONException {
        this.manifest = manifest;
        this.file = file;
        if (file == null) {
            throw new IllegalStateException("Manifest wasn't loaded from a file");
        }
        if (file.isFile()) {
            this.json = FileUtils.readJSON(file);
        } else {
            this.json = new JSONObject();
        }
    }

    public ModPackManifestEditor addIfMissing(String key, Object value) throws JSONException {
        String current = this.json.optString(key, null);
        if (current == null || current.isEmpty()) {
            this.json.put(key, value);
        }
        return this;
    }

    public ModPackManifestEditor put(String key, Object value) throws JSONException {
        this.json.put(key, value);
        return this;
    }

    public void commit() throws IOException, JSONException {
        FileUtils.writeJSON(this.file, this.json);
        this.manifest.loadFile(this.file);
    }
}
