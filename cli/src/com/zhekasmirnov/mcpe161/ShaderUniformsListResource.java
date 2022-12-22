package com.zhekasmirnov.mcpe161;

import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResource;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResourceHandler;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.IOException;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShaderUniformsListResource implements RuntimeResourceHandler {
    public final String asset;
    public final String name;
    private HashMap<String, HashMap<String, String>> uniformSets = new HashMap<>();
    private JSONObject uniformsListJson = null;

    public ShaderUniformsListResource(String asset, String name) {
        this.asset = asset;
        this.name = name;
    }

    @Override
    public String getResourceName() {
        return this.name;
    }

    @Override
    public String getResourcePath() {
        return this.asset;
    }

    public void initializeJson(ResourceManager manager) {
        try {
            this.uniformsListJson = new JSONObject(FileUtils.readStringFromAsset(manager.getAssets(), this.asset));
        } catch (IOException err) {
            err.printStackTrace();
        } catch (JSONException err2) {
            err2.printStackTrace();
        }
    }

    @Override
    public void handle(RuntimeResource resource) {
        if (this.uniformsListJson == null) {
            return;
        }
        for (String uniformSetName : this.uniformSets.keySet()) {
            JSONArray uniformSetJson = this.uniformsListJson.optJSONArray(uniformSetName);
            if (uniformSetJson == null) {
                try {
                    uniformSetJson = new JSONArray();
                    this.uniformsListJson.put(uniformSetName, uniformSetJson);
                } catch (JSONException e) {
                }
            }
            HashMap<String, String> uniformSet = this.uniformSets.get(uniformSetName);
            for (String uniformName : uniformSet.keySet()) {
                try {
                    JSONObject uniform = new JSONObject();
                    uniform.put("Name", uniformName);
                    uniform.put("Type", uniformSet.get(uniformName));
                    uniformSetJson.put(uniform);
                } catch (JSONException e2) {
                }
            }
        }
        try {
            FileUtils.writeJSON(resource.getFile(), this.uniformsListJson);
        } catch (IOException e3) {
            throw new RuntimeException("unexpected exception inside MaterialProcessor.handle()", e3);
        }
    }

    public boolean addUniform(String uniformSetName, String name, String type) {
        HashMap<String, String> uniformSet = this.uniformSets.get(uniformSetName);
        if (uniformSet == null) {
            uniformSet = new HashMap<>();
            this.uniformSets.put(uniformSetName, uniformSet);
        }
        if (uniformSet.containsKey(name)) {
            uniformSet.put(name, type);
            return false;
        }
        uniformSet.put(name, type);
        return true;
    }
}
