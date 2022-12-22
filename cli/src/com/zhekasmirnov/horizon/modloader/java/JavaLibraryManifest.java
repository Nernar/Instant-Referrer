package com.zhekasmirnov.horizon.modloader.java;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JSONUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JavaLibraryManifest {
    public final String[] arguments;
    private final JSONObject content;
    private final File directory;
    @SuppressWarnings("unused")
    private final File file;
    public final boolean verbose;
    public final List<File> sourceDirs = new ArrayList<>();
    public final List<File> libraryDirs = new ArrayList<>();
    public final List<File> libraryPaths = new ArrayList<>();
    public final List<String> bootClasses = new ArrayList<>();

    public JavaLibraryManifest(File file) throws IOException, JSONException {
        this.file = file;
        this.directory = file.getParentFile();
        JSONObject readJSON = FileUtils.readJSON(file);
        this.content = readJSON;
        this.verbose = readJSON.optBoolean("verbose");
        JSONArray optJSONArray = this.content.optJSONArray("options");
        if (optJSONArray != null) {
            this.arguments = (String[]) JSONUtils.toList(optJSONArray).toArray(new String[optJSONArray.length()]);
        } else {
            this.arguments = new String[0];
        }
        JSONArray optJSONArray2 = this.content.optJSONArray("boot-classes");
        if (optJSONArray2 != null) {
            this.bootClasses.addAll(JSONUtils.toList(optJSONArray2));
        }
        JSONArray optJSONArray3 = this.content.optJSONArray("source-dirs");
        if (optJSONArray3 != null) {
            for (Object str : JSONUtils.toList(optJSONArray3)) {
                File file2 = new File(this.directory, (String) str);
                if (file2.exists() && file2.isDirectory()) {
                    this.sourceDirs.add(file2);
                }
            }
        }
        JSONArray optJSONArray4 = this.content.optJSONArray("library-dirs");
        if (optJSONArray4 != null) {
            for (Object str2 : JSONUtils.toList(optJSONArray4)) {
                File file3 = new File(this.directory, (String) str2);
                if (file3.exists() && file3.isDirectory()) {
                    this.libraryDirs.add(file3);
                    for (File file4 : file3.listFiles()) {
                        String name = file4.getName();
                        if (name.endsWith(".zip") || name.endsWith(".jar") || name.endsWith(".dex")) {
                            this.libraryPaths.add(file4);
                        } else {
                            throw new IllegalArgumentException("illegal java library, it can be dex file, zip or jar archive: " + file4);
                        }
                    }
                    continue;
                }
            }
        }
    }
}
