package org.mineprogramming.horizon.innercore.model;

import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class ModPreferences {
    private File file;
    private JSONObject json;

    public ModPreferences(File root) {
        this.file = new File(root, "preferences.json");
        try {
            this.json = FileTools.readJSON(this.file.getAbsolutePath());
        } catch (IOException | JSONException e) {
            this.json = new JSONObject();
        }
    }

    public void setIcmodsData(int id, int version) {
        try {
            this.json.put("icmods_id", id);
            this.json.put("icmods_version", version);
            FileTools.writeJSON(this.file.getAbsolutePath(), this.json);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public int getIcmodsId() {
        return this.json.optInt("icmods_id", 0);
    }

    public int getIcmodsVersion() {
        return this.json.optInt("icmods_version", 0);
    }
}
