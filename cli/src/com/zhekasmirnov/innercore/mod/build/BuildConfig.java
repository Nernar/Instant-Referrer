package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.apparatus.minecraft.version.ResourceGameVersion;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.mod.build.enums.BuildConfigError;
import com.zhekasmirnov.innercore.mod.build.enums.BuildType;
import com.zhekasmirnov.innercore.mod.build.enums.ResourceDirType;
import com.zhekasmirnov.innercore.mod.build.enums.SourceType;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BuildConfig {
    public ArrayList<BuildableDir> buildableDirs;
    @SuppressWarnings("unused")
    private BuildConfigError configError;
    private File configFile;
    private JSONObject configJson;
    public DefaultConfig defaultConfig;
    private boolean isValid;
    public ArrayList<DeclaredDirectory> javaDirectories;
    public ArrayList<DeclaredDirectory> nativeDirectories;
    public ArrayList<ResourceDir> resourceDirs;
    public ArrayList<Source> sourcesToCompile;

    public static class DeclaredDirectory {
        public final String path;
        public final ResourceGameVersion version;

        public DeclaredDirectory(String path, ResourceGameVersion version) {
            this.path = path;
            this.version = version;
        }

        public File getFile(File root) {
            return new File(root, this.path);
        }

        public static DeclaredDirectory fromJson(JSONObject json, String pathPropertyName) {
            String path = json.optString(pathPropertyName);
            if (path == null) {
                return null;
            }
            ResourceGameVersion version = new ResourceGameVersion(json);
            return new DeclaredDirectory(path, version);
        }
    }

    public BuildConfig() {
        this.isValid = false;
        this.configError = BuildConfigError.NONE;
        this.buildableDirs = new ArrayList<>();
        this.resourceDirs = new ArrayList<>();
        this.sourcesToCompile = new ArrayList<>();
        this.javaDirectories = new ArrayList<>();
        this.nativeDirectories = new ArrayList<>();
        this.configJson = new JSONObject();
        this.isValid = true;
    }

    public BuildConfig(JSONObject obj) {
        this.isValid = false;
        this.configError = BuildConfigError.NONE;
        this.buildableDirs = new ArrayList<>();
        this.resourceDirs = new ArrayList<>();
        this.sourcesToCompile = new ArrayList<>();
        this.javaDirectories = new ArrayList<>();
        this.nativeDirectories = new ArrayList<>();
        this.configJson = obj;
        this.isValid = true;
    }

    public BuildConfig(File file) {
        this.isValid = false;
        this.configError = BuildConfigError.NONE;
        this.buildableDirs = new ArrayList<>();
        this.resourceDirs = new ArrayList<>();
        this.sourcesToCompile = new ArrayList<>();
        this.javaDirectories = new ArrayList<>();
        this.nativeDirectories = new ArrayList<>();
        this.configFile = file;
        this.isValid = false;
        try {
            this.configJson = FileTools.readJSON(file.getAbsolutePath());
            this.isValid = true;
        } catch (IOException e) {
            this.configError = BuildConfigError.FILE_ERROR;
            e.printStackTrace();
        } catch (JSONException e2) {
            this.configError = BuildConfigError.PARSE_ERROR;
            e2.printStackTrace();
        }
    }

    public void save(File file) {
        try {
            FileTools.writeJSON(file.getAbsolutePath(), this.configJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        save(this.configFile);
    }

    public boolean isValid() {
        return this.isValid;
    }

    public void validate() {
        try {
            if (this.configJson.optJSONObject("defaultConfig") == null) {
                this.configJson.put("defaultConfig", new JSONObject());
            }
            if (this.configJson.optJSONArray("buildDirs") == null) {
                this.configJson.put("buildDirs", new JSONArray());
            }
            if (this.configJson.optJSONArray("compile") == null) {
                this.configJson.put("compile", new JSONArray());
            }
            if (this.configJson.optJSONArray("resources") == null) {
                this.configJson.put("resources", new JSONArray());
            }
            if (this.configJson.optJSONArray("nativeDirs") == null) {
                this.configJson.put("nativeDirs", new JSONArray());
            }
            if (this.configJson.optJSONArray("javaDirs") == null) {
                this.configJson.put("javaDirs", new JSONArray());
            }
        } catch (JSONException e) {
            this.isValid = false;
            e.printStackTrace();
        }
    }

    public boolean read() {
        if (this.isValid) {
            validate();
        }
        if (this.isValid) {
            this.defaultConfig = DefaultConfig.fromJson(this.configJson.optJSONObject("defaultConfig"));
            this.buildableDirs.clear();
            JSONArray buildableDirsJson = this.configJson.optJSONArray("buildDirs");
            for (int i = 0; i < buildableDirsJson.length(); i++) {
                this.buildableDirs.add(BuildableDir.fromJson(buildableDirsJson.optJSONObject(i)));
            }
            this.resourceDirs.clear();
            JSONArray resourceDirsJson = this.configJson.optJSONArray("resources");
            for (int i2 = 0; i2 < resourceDirsJson.length(); i2++) {
                this.resourceDirs.add(ResourceDir.fromJson(resourceDirsJson.optJSONObject(i2)));
            }
            this.sourcesToCompile.clear();
            JSONArray sourcesJson = this.configJson.optJSONArray("compile");
            for (int i3 = 0; i3 < sourcesJson.length(); i3++) {
                this.sourcesToCompile.add(Source.fromJson(sourcesJson.optJSONObject(i3), this.defaultConfig));
            }
            this.javaDirectories.clear();
            JSONArray javaJson = this.configJson.optJSONArray("javaDirs");
            for (int i4 = 0; i4 < javaJson.length(); i4++) {
                JSONObject directoryJson = javaJson.optJSONObject(i4);
                try {
                    DeclaredDirectory directory = DeclaredDirectory.fromJson(directoryJson, "path");
                    if (directory != null) {
                        this.javaDirectories.add(directory);
                    }
                } catch (Exception e) {
                    ICLog.e("InnerCore-BuildConfig", "invalid java directory object", e);
                }
            }
            this.nativeDirectories.clear();
            JSONArray nativeJson = this.configJson.optJSONArray("nativeDirs");
            for (int i5 = 0; i5 < nativeJson.length(); i5++) {
                JSONObject directoryJson2 = nativeJson.optJSONObject(i5);
                try {
                    DeclaredDirectory directory2 = DeclaredDirectory.fromJson(directoryJson2, "path");
                    if (directory2 != null) {
                        this.nativeDirectories.add(directory2);
                    }
                } catch (Exception e2) {
                    ICLog.e("InnerCore-BuildConfig", "invalid native directory object", e2);
                }
            }
            return true;
        }
        return false;
    }

    public BuildType getBuildType() {
        return this.defaultConfig.buildType;
    }

    public API getDefaultAPI() {
        return this.defaultConfig.apiInstance;
    }

    public String getName() {
        if (this.configFile != null) {
            return this.configFile.getParentFile().getName();
        }
        return "Unknown Mod";
    }

    public ArrayList<Source> getAllSourcesToCompile(boolean useApi) {
        File[] files;
        ArrayList<Source> sources = new ArrayList<>(this.sourcesToCompile);
        if (this.defaultConfig.libDir != null) {
            File libraryDir = new File(this.configFile.getParent(), this.defaultConfig.libDir);
            if (libraryDir.exists() && libraryDir.isDirectory() && (files = libraryDir.listFiles()) != null) {
                for (File file : files) {
                    Logger.debug("LIB-DIR", "found library file " + file + " local_path=" + this.defaultConfig.libDir + file.getName());
                    if (!file.isDirectory()) {
                        Source source = new Source(new JSONObject(), null);
                        if (useApi) {
                            source.setAPI(this.defaultConfig.apiInstance);
                        }
                        source.setPath(String.valueOf(this.defaultConfig.libDir) + file.getName());
                        source.setSourceName(file.getName());
                        source.setSourceType(SourceType.LIBRARY);
                        sources.add(source);
                    }
                }
            }
        }
        return sources;
    }

    public static class DefaultConfig {
        public API apiInstance;
        public BuildType buildType;
        public final ResourceGameVersion gameVersion;
        public JSONObject json;
        public int optimizationLevel;
        public String libDir = null;
        public String resourcePacksDir = null;
        public String behaviorPacksDir = null;
        public String setupScriptDir = null;

        private DefaultConfig(JSONObject json) {
            this.json = json;
            this.gameVersion = new ResourceGameVersion(json);
        }

        public void setAPI(API api) {
            this.apiInstance = api;
            try {
                this.json.put("api", api.getName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setOptimizationLevel(int level) {
            this.optimizationLevel = BuildConfig.validateOptimizationLevel(level);
            try {
                this.json.put("optimizationLevel", level);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setBuildType(BuildType type) {
            this.buildType = type;
            try {
                this.json.put("buildType", type.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setLibDir(String dir) {
            this.libDir = dir;
            try {
                this.json.put("libraryDir", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setMinecraftResourcePacksDir(String dir) {
            this.resourcePacksDir = dir;
            try {
                this.json.put("resourcePacksDir", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setMinecraftBehaviorPacksDir(String dir) {
            this.behaviorPacksDir = dir;
            try {
                this.json.put("behaviorPacksDir", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setSetupScriptDir(String dir) {
            this.setupScriptDir = dir;
            try {
                this.json.put("setupScript", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static DefaultConfig fromJson(JSONObject json) {
            DefaultConfig defaultConfig = new DefaultConfig(json);
            defaultConfig.apiInstance = BuildConfig.getAPIFromJSON(json);
            defaultConfig.optimizationLevel = BuildConfig.getOptimizationLevelFromJSON(json);
            defaultConfig.buildType = BuildConfig.getBuildTypeFromJSON(json);
            defaultConfig.libDir = json.optString("libraryDir", null);
            defaultConfig.resourcePacksDir = json.optString("resourcePacksDir", null);
            defaultConfig.behaviorPacksDir = json.optString("behaviorPacksDir", null);
            defaultConfig.setupScriptDir = json.optString("setupScript", null);
            return defaultConfig;
        }
    }

    public static class BuildableDir {
        public String dir;
        public JSONObject json;
        public String targetSource;

        private BuildableDir(JSONObject json) {
            this.json = json;
        }

        public void setDir(String dir) {
            this.dir = dir;
            try {
                this.json.put("dir", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setTargetSource(String targetSource) {
            this.targetSource = targetSource;
            try {
                this.json.put("targetSource", targetSource);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static BuildableDir fromJson(JSONObject json) {
            BuildableDir buildableDir = new BuildableDir(json);
            buildableDir.dir = json.optString("dir");
            buildableDir.targetSource = json.optString("targetSource");
            return buildableDir;
        }

        public boolean isRelatedSource(Source source) {
            if (source != null && this.targetSource != null) {
                return this.targetSource.equals(source.path);
            }
            return false;
        }
    }

    public static class ResourceDir {
        public final ResourceGameVersion gameVersion;
        public JSONObject json;
        public String path;
        public ResourceDirType resourceType;

        private ResourceDir(JSONObject json) {
            this.json = json;
            this.gameVersion = new ResourceGameVersion(json);
        }

        public void setPath(String path) {
            this.path = path;
            try {
                this.json.put("path", path);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setResourceType(ResourceDirType type) {
            this.resourceType = type;
            try {
                this.json.put("resourceType", type.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static ResourceDir fromJson(JSONObject json) {
            ResourceDir resourceDir = new ResourceDir(json);
            resourceDir.path = json.optString("path");
            resourceDir.resourceType = BuildConfig.getResourceDirTypeFromJSON(json);
            return resourceDir;
        }
    }

    public static class Source {
        public API apiInstance;
        public final ResourceGameVersion gameVersion;
        public JSONObject json;
        public int optimizationLevel;
        public String path;
        public String sourceName;
        public SourceType sourceType;

        private Source(JSONObject json) {
            this.json = json;
            this.gameVersion = new ResourceGameVersion(json);
        }

        Source(JSONObject jSONObject, Source source) {
            this(jSONObject);
        }

        public void setPath(String path) {
            this.path = path;
            try {
                this.json.put("path", path);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
            try {
                this.json.put("sourceName", sourceName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setSourceType(SourceType sourceType) {
            this.sourceType = sourceType;
            try {
                this.json.put("sourceType", sourceType.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setOptimizationLevel(int level) {
            this.optimizationLevel = BuildConfig.validateOptimizationLevel(level);
            try {
                this.json.put("optimizationLevel", level);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setAPI(API api) {
            this.apiInstance = api;
            try {
                this.json.put("api", api.getName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public CompilerConfig getCompilerConfig() {
            CompilerConfig config = new CompilerConfig(this.apiInstance);
            config.setName(this.sourceName);
            config.setOptimizationLevel(this.optimizationLevel);
            config.isLibrary = this.sourceType == SourceType.LIBRARY;
            return config;
        }

        public static Source fromJson(JSONObject json, DefaultConfig config) {
            Source source = new Source(json);
            source.path = json.optString("path");
            source.sourceType = BuildConfig.getSourceTypeFromJSON(json);
            if (json.has("sourceName")) {
                source.sourceName = json.optString("sourceName", "Unknown Source");
            } else {
                source.sourceName = source.path.substring(source.path.lastIndexOf("/") + 1);
            }
            if (json.has("api")) {
                source.apiInstance = BuildConfig.getAPIFromJSON(json);
            } else if (source.sourceType == SourceType.PRELOADER) {
                source.apiInstance = API.getInstanceByName("Preloader");
            } else {
                source.apiInstance = config.apiInstance;
            }
            if (json.has("optimizationLevel")) {
                source.optimizationLevel = BuildConfig.getOptimizationLevelFromJSON(json);
            } else {
                source.optimizationLevel = config.optimizationLevel;
            }
            return source;
        }
    }

    public static API getAPIFromJSON(JSONObject obj) {
        if (obj.has("api")) {
            try {
                return API.getInstanceByName(obj.getString("api"));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static int validateOptimizationLevel(int level) {
        return Math.min(9, Math.max(-1, level));
    }

    public static int getOptimizationLevelFromJSON(JSONObject obj) {
        if (obj.has("optimizationLevel")) {
            return validateOptimizationLevel(obj.optInt("optimizationLevel", -1));
        }
        return 9;
    }

    public static ResourceDirType getResourceDirTypeFromJSON(JSONObject obj) {
        ResourceDirType result = ResourceDirType.RESOURCE;
        try {
            if (obj.has("resourceType")) {
                result = ResourceDirType.fromString(obj.getString("resourceType"));
            }
            obj.put("resourceType", result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static SourceType getSourceTypeFromJSON(JSONObject obj) {
        SourceType result = SourceType.MOD;
        try {
            if (obj.has("sourceType")) {
                result = SourceType.fromString(obj.getString("sourceType"));
            }
            obj.put("sourceType", result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static BuildType getBuildTypeFromJSON(JSONObject obj) {
        BuildType result = BuildType.DEVELOP;
        try {
            if (obj.has("buildType")) {
                result = BuildType.fromString(obj.getString("buildType"));
            }
            obj.put("buildType", result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public BuildableDir findRelatedBuildableDir(Source source) {
        Iterator<BuildableDir> it = this.buildableDirs.iterator();
        while (it.hasNext()) {
            BuildableDir dir = it.next();
            if (dir.isRelatedSource(source)) {
                return dir;
            }
        }
        return null;
    }
}
