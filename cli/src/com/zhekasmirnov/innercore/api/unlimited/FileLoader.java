package com.zhekasmirnov.innercore.api.unlimited;

import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import org.json.JSONObject;

public class FileLoader {
    private JSONObject data;
    private File file;
    private JSONObject uids;

    public FileLoader(File file) {
        this.file = file;
        try {
            this.data = FileTools.readJSON(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            this.data = new JSONObject();
        }
        this.uids = this.data.optJSONObject("id");
        if (this.uids != null) {
            IDRegistry.fromJson(this.uids);
        }
    }

    public void save() {
        try {
            this.data.put("id", IDRegistry.toJson());
            FileTools.writeJSON(this.file.getAbsolutePath(), this.data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
