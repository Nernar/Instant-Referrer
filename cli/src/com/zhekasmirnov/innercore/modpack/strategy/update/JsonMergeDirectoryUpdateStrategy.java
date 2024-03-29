package com.zhekasmirnov.innercore.modpack.strategy.update;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonMergeDirectoryUpdateStrategy extends DirectoryUpdateStrategy {
    @Override
    public void beginUpdate() throws IOException {
    }

    private JSONObject readJson(File file, JSONObject fallback) {
        try {
            return FileUtils.readJSON(file);
        } catch (IOException | JSONException e) {
            return new JSONObject();
        }
    }

    private JSONObject mergeJson(JSONObject userData, JSONObject originalData, JSONObject updateData) {
        return updateData;
    }

    @Override
    public void updateFile(String path, InputStream stream) throws IOException {
        File root = getAssignedDirectory().getLocation();
        File userFile = new File(root, path);
        userFile.getParentFile().mkdirs();
        byte[] data = FileTools.convertStreamToBytes(stream);
        JSONObject userJson = readJson(userFile, null);
        if (userJson != null) {
            try {
                JSONObject updateJson = new JSONObject(new String(data));
                File originalFile = new File(new File(root, ".keep-unchanged"), path);
                originalFile.getParentFile().mkdirs();
                JSONObject originalJson = readJson(originalFile, null);
                if (originalJson != null) {
                    FileUtils.writeJSON(userFile, mergeJson(userJson, originalJson, updateJson));
                    FileUtils.writeJSON(originalFile, updateJson);
                    return;
                }
            } catch (JSONException e) {
            }
        }
        FileOutputStream outputStream = new FileOutputStream(userFile);
        try {
            outputStream.write(data);
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (outputStream != null && th != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable th3) {
                        outputStream.close();
                    }
                }
                throw th2;
            }
        }
    }

    @Override
    public void finishUpdate() throws IOException {
    }
}
