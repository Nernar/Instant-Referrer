package com.zhekasmirnov.mcpe161;

import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.Resource;
import com.zhekasmirnov.horizon.modloader.resource.processor.ResourceProcessor;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResource;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResourceHandler;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.mod.resource.types.enums.TextureType;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TextureAtlas implements ResourceProcessor, RuntimeResourceHandler {
    public final String asset;
    private JSONObject atlas;
    private final String directoryName;
    public final String name;
    private JSONObject textureData;
    private TextureType type;

    public TextureAtlas(TextureType type, String asset, String name, String directoryName) {
        this.type = type;
        this.asset = asset;
        this.name = name;
        this.directoryName = directoryName;
    }

    @Override
    public void initialize(ResourceManager manager) {
        try {
            this.atlas = FileUtils.readJSONFromAssets(null, "resource_packs/vanilla/" + this.asset);
            this.textureData = this.atlas.getJSONObject("texture_data");
        } catch (IOException | JSONException e) {
            Logger.debug("failed to read json for texture atlas: " + this.asset + " " + e);
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
            FileUtils.writeJSON(resource.getFile(), this.atlas);
        } catch (IOException e) {
            throw new RuntimeException("unexpected exception inside TextureAtlas.handle()", e);
        } catch (NullPointerException e) {}
    }

    private String parseAdditionalNameParameters(String name, Map<String, Object> params) {
        if (name.endsWith(".liquid")) {
            params.put("quad", 1);
            return name.substring(0, name.length() - 7);
        }
        return name;
    }

    private String insert(String name, int index, String path) throws JSONException {
        JSONArray textures;
        Map<String, Object> params = new HashMap<>();
        String name2 = parseAdditionalNameParameters(name, params);
        JSONObject set = this.textureData.optJSONObject(name2);
        if (set == null) {
            set = new JSONObject();
            set.put("textures", new JSONArray());
            this.textureData.put(name2, set);
        }
        Object _textures = set.opt("textures");
        if (_textures instanceof JSONArray) {
            textures = (JSONArray) _textures;
        } else if (_textures instanceof String) {
            textures = new JSONArray();
            textures.put(_textures);
        } else {
            textures = new JSONArray();
        }
        for (int i = 0; i < index; i++) {
            if (textures.optString(i) == "") {
                textures.put(i, path);
            }
        }
        textures.put(index, path);
        set.put("textures", textures);
        for (Map.Entry<String, Object> param : params.entrySet()) {
            try {
                set.put(param.getKey(), param.getValue());
            } catch (JSONException e) {
            }
        }
        return name2;
    }

    private String insert(Resource resource) {
        String name = resource.getNameWithoutExtension();
        String path = resource.getAtlasPath();
        int index = resource.getIndex();
        try {
            return insert(name, index, path);
        } catch (JSONException e) {
            throw new RuntimeException("unexpected exception inside TextureAtlas.insert(Resource)", e);
        }
    }

    @Override
    public void process(Resource resource, Collection<Resource> resources) {
        resources.add(resource);
        if (resource.getPath().contains(this.directoryName) && "png".equals(resource.getExtension())) {
            String name = insert(resource);
            ResourcePackManager.instance.resourceStorage.addResourceFile(this.type, resource, name);
        }
    }
}
