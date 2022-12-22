package com.zhekasmirnov.horizon.launcher.pack;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JsonIterator;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class PackInstallationInfo {
    public static final String KEY_CUSTOM_NAME = "customName";
    public static final String KEY_INTERNAL_ID = "internalId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_UUID = "uuid";
    private static final Object LOCK = new Object();
    public final File file;
    private final HashMap<String, String> valueMap = new HashMap<>();

    public PackInstallationInfo(File file) {
        this.file = file;
    }

    private void updateMapContents() {
        if (this.file.exists() && this.file.isFile()) {
            try {
                synchronized (LOCK) {
                    JSONObject readJSON = FileUtils.readJSON(this.file);
                    Iterator<Object> it = new JsonIterator<>(readJSON).iterator();
                    while (it.hasNext()) {
                        String str = (String) it.next();
                        String optString = readJSON.optString(str);
                        if (optString != null) {
                            this.valueMap.put(str, optString);
                        }
                    }
                }
            } catch (IOException | JSONException e) {
            }
        }
    }

    private void updateFileContents() {
        try {
            synchronized (LOCK) {
                JSONObject jSONObject = new JSONObject();
                try {
                    jSONObject = FileUtils.readJSON(this.file);
                } catch (IOException | JSONException e) {
                }
                for (String str : this.valueMap.keySet()) {
                    String str2 = this.valueMap.get(str);
                    if (str2 != null) {
                        jSONObject.put(str, str2.toString());
                    } else {
                        jSONObject.remove(str);
                    }
                }
                FileUtils.writeJSON(this.file, jSONObject);
            }
        } catch (IOException | JSONException e2) {
        }
    }

    public String getValue(String str) {
        updateMapContents();
        return this.valueMap.get(str);
    }

    public void setValue(String str, String str2) {
        updateMapContents();
        this.valueMap.put(str, str2);
        updateFileContents();
    }
}
