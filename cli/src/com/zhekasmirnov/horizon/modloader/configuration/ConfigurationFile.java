package com.zhekasmirnov.horizon.modloader.configuration;

import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigurationFile extends AbstractJsonConfiguration {
    private JSONObject data;
    public final File file;
    private boolean isReadOnly;

    public ConfigurationFile(File file, boolean z) {
        this.file = file;
        this.isReadOnly = z;
    }

    @Override
    public void refresh() {
        load();
    }

    @Override
    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    @Override
    public void save() {
        try {
            FileUtils.writeJSON(this.file, this.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        this.data = new JSONObject();
        try {
            this.data = FileUtils.readJSON(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
    }

    @Override
    protected JSONObject getData() {
        return this.data;
    }
}
