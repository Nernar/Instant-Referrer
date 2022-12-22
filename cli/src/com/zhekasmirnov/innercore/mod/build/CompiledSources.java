package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CompiledSources {
    private File dir;
    @SuppressWarnings("unused")
    private boolean isValid = true;
    private JSONObject sourceList;
    private File sourceListFile;

    private void invalidate() {
        this.isValid = false;
        validateJson();
        validateFiles();
    }

    private void validateFiles() {
        if (this.dir != null && !this.dir.isDirectory()) {
            this.dir.delete();
        }
        if (this.dir != null && !this.dir.exists()) {
            this.dir.mkdirs();
        }
        if (this.sourceListFile != null && !this.sourceListFile.exists()) {
            saveSourceList();
        }
    }

    private void validateJson() {
        if (this.sourceList == null) {
            this.sourceList = new JSONObject();
        }
    }

    public void saveSourceList() {
        try {
            FileTools.writeJSON(this.sourceListFile.getAbsolutePath(), this.sourceList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompiledSources(File dir) {
        this.sourceList = new JSONObject();
        this.dir = dir;
        if (!dir.exists()) {
            invalidate();
            return;
        }
        this.sourceListFile = new File(dir, "sources.json");
        if (!this.sourceListFile.exists()) {
            invalidate();
            return;
        }
        try {
            this.sourceList = FileTools.readJSON(this.sourceListFile.getAbsolutePath());
            validateJson();
            validateFiles();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            invalidate();
        }
    }

    public File[] getCompiledSourceFilesFor(String name) {
        JSONObject data = this.sourceList.optJSONObject(name);
        if (data == null) {
            return null;
        }
        JSONArray sources = data.optJSONArray("links");
        if (sources != null) {
            ArrayList<File> files = new ArrayList<>();
            for (int i = 0; i < sources.length(); i++) {
                String path = sources.optString(i);
                if (path != null) {
                    File file = new File(this.dir, path);
                    if (file.exists()) {
                        files.add(file);
                    } else {
                        ICLog.d("WARNING", "compiled dex file " + path + " related to source " + name + " has incorrect formatted path");
                    }
                } else {
                    ICLog.d("WARNING", "compiled dex file at index " + i + " related to source " + name + " has incorrect formatted path");
                }
            }
            int i2 = files.size();
            File[] _files = new File[i2];
            files.toArray(_files);
            return _files;
        }
        String path2 = data.optString("path");
        if (path2 != null) {
            return new File[]{new File(this.dir, path2)};
        }
        return null;
    }

    public Executable getCompiledExecutableFor(String name, CompilerConfig config) throws IOException {
        File[] dexFiles = getCompiledSourceFilesFor(name);
        if (dexFiles != null) {
            return Compiler.loadDexList(dexFiles, config);
        }
        return null;
    }

    public void addCompiledSource(String name, File file, String className) {
        JSONObject data = this.sourceList.optJSONObject(name);
        if (data == null) {
            data = new JSONObject();
        }
        try {
            JSONArray links = data.optJSONArray("links");
            if (links == null) {
                links = new JSONArray();
                data.put("links", links);
            }
            links.put(links.length(), file.getName());
            data.put("class_name", className);
            this.sourceList.put(name, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveSourceList();
    }

    public File getTargetCompilationFile(String sourcePath) {
        return new File(this.dir, sourcePath);
    }

    public void reset() {
        validateJson();
        validateFiles();
        File[] files = this.dir.listFiles();
        for (File file : files) {
            file.delete();
        }
        this.sourceList = null;
        validateJson();
        validateFiles();
    }
}
