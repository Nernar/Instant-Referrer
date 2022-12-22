package com.zhekasmirnov.mcpe161;

import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.Resource;
import com.zhekasmirnov.horizon.modloader.resource.processor.ResourceProcessor;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResource;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResourceHandler;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JsonIterator;
import com.zhekasmirnov.innercore.api.log.ICLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class MaterialProcessor implements ResourceProcessor, RuntimeResourceHandler {
    public final String asset;
    public final String materialDirectoryName;
    private JSONObject materialsJson;
    public final String name;
    private JSONObject rootJson;
    public final String shaderDirectoryName;
    private HashMap<String, List<ShaderPathInJson>> shaderPaths = new HashMap<>();
    private HashMap<String, String> availableShaderPaths = new HashMap<>();
    private ShaderUniformsListResource shaderUniformList = null;

    class ShaderPathInJson {
        public final JSONObject json;
        public final String originalPath;
        public final String shaderPathKey;

        ShaderPathInJson(JSONObject json, String shaderPathKey, String originalPath) {
            this.json = json;
            this.shaderPathKey = shaderPathKey;
            this.originalPath = originalPath;
        }
    }

    public MaterialProcessor(String asset, String name, String materialDirectoryName, String shaderDirectoryName) {
        this.asset = asset;
        this.name = name;
        this.materialDirectoryName = materialDirectoryName;
        this.shaderDirectoryName = shaderDirectoryName;
    }

    public ShaderUniformsListResource newShaderUniformList(String asset, String name) {
        this.shaderUniformList = new ShaderUniformsListResource(asset, name);
        return this.shaderUniformList;
    }

    @Override
    public void initialize(ResourceManager manager) {
        try {
            try {
                this.shaderPaths.clear();
                this.rootJson = new JSONObject(FileUtils.readStringFromAsset(null, "resource_packs/vanilla/" + this.asset));
                this.materialsJson = this.rootJson.optJSONObject("materials");
                if (this.materialsJson == null) {
                    this.materialsJson = new JSONObject();
                    try {
                        this.rootJson.put("materials", this.materialsJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (this.shaderUniformList != null) {
                    this.shaderUniformList.initializeJson(manager);
                }
            } catch (JSONException e2) {
                throw new RuntimeException("failed to read json for flipbook atlas: " + this.asset + " " + e2, e2);
            }
        } catch (IOException e3) {
            throw new RuntimeException("failed to read json for flipbook atlas: " + this.asset + " " + e3, e3);
        }
    }

    @Override
    public String getResourceName() {
        return this.name;
    }

    @Override
    public String getResourcePath() {
        return this.asset;
    }

    @Override
    public void handle(RuntimeResource resource) {
        try {
            FileUtils.writeJSON(resource.getFile(), this.rootJson);
        } catch (IOException e) {
            throw new RuntimeException("unexpected exception inside MaterialProcessor.handle()", e);
        }
    }

    private void addShaderPathOverrideIfRequired(JSONObject json, String key) {
        String path = json.optString(key, null);
        if (path != null) {
            boolean isOverridable = false;
            if (path.startsWith("./")) {
                path = path.substring(2);
                isOverridable = true;
            } else {
                String shaderDirectoryPrefix = String.valueOf(this.shaderDirectoryName) + "/";
                if (path.contains(shaderDirectoryPrefix)) {
                    path = path.substring(path.indexOf(shaderDirectoryPrefix) + shaderDirectoryPrefix.length());
                    isOverridable = true;
                }
            }
            if (isOverridable) {
                String path2 = path.toLowerCase().trim();
                if (this.availableShaderPaths.containsKey(path2)) {
                    try {
                        json.put(key, this.availableShaderPaths.get(path2));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (!this.shaderPaths.containsKey(path2)) {
                    this.shaderPaths.put(path2, new ArrayList<>());
                }
                this.shaderPaths.get(path2).add(new ShaderPathInJson(json, key, path2));
            }
        }
    }

    private void mergeMaterials(JSONObject materials) {
        Iterator<Object> it = new JsonIterator<>(materials).iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            JSONObject material = materials.optJSONObject(key);
            try {
                this.materialsJson.put(key, material);
                addShaderPathOverrideIfRequired(material, "vertexShader");
                addShaderPathOverrideIfRequired(material, "fragmentShader");
                addShaderPathOverrideIfRequired(material, "vrGeometryShader");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void mergeMaterials(Resource resource) {
        try {
            JSONObject json = FileUtils.readJSON(resource.file);
            mergeMaterials(json);
        } catch (IOException | JSONException e) {
            ICLog.e("ERROR", "failed to read json from: " + resource.file.getAbsolutePath(), e);
        }
    }

    private Resource overrideShaderPath(Resource resource) {
        String path = resource.getPath();
        String path2 = path.substring(path.indexOf(this.shaderDirectoryName) + this.shaderDirectoryName.length() + 1);
        String pathOverride = "shaders/glsl/" + path2;
        if (this.shaderPaths.containsKey(path2)) {
            for (ShaderPathInJson pathInJson : this.shaderPaths.get(path2)) {
                try {
                    pathInJson.json.put(pathInJson.shaderPathKey, pathOverride);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        this.availableShaderPaths.put(path2, pathOverride);
        return resource.getLink(pathOverride);
    }

    private void addShaderUniforms(JSONObject json) {
        if (this.shaderUniformList != null) {
            Iterator<Object> it = new JsonIterator<>(json).iterator();
            while (it.hasNext()) {
                String setName = (String) it.next();
                JSONObject setJson = json.optJSONObject(setName);
                if (setJson != null) {
                    Iterator<Object> it2 = new JsonIterator<>(setJson).iterator();
                    while (it2.hasNext()) {
                        String uniformName = (String) it2.next();
                        String uniformType = setJson.optString(uniformName);
                        this.shaderUniformList.addUniform(setName, uniformName, uniformType);
                    }
                }
            }
        }
    }

    private void addShaderUniforms(Resource resource) {
        try {
            JSONObject json = FileUtils.readJSON(resource.file);
            addShaderUniforms(json);
        } catch (IOException | JSONException e) {
            ICLog.e("ERROR", "failed to read json from: " + resource.file.getAbsolutePath(), e);
        }
    }

    @Override
    public void process(Resource resource, Collection<Resource> resources) {
        resources.add(resource);
        String path = resource.getPath();
        if (!path.contains(this.shaderDirectoryName)) {
            if (path.contains(this.materialDirectoryName)) {
                mergeMaterials(resource);
            }
        } else if (path.endsWith(".uniforms")) {
            addShaderUniforms(resource);
        } else {
            Resource linked = overrideShaderPath(resource);
            if (linked != null) {
                resources.add(linked);
            }
        }
    }
}
