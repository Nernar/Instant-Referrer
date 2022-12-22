package com.zhekasmirnov.mcpe161;

import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.ResourceOverride;
import com.zhekasmirnov.horizon.modloader.resource.directory.Resource;
import com.zhekasmirnov.horizon.modloader.resource.processor.ResourceProcessor;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResource;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResourceHandler;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContentProcessor implements ResourceProcessor, RuntimeResourceHandler {
    private JSONArray content;
    private JSONObject json;
    private final HashSet<String> paths = new HashSet<>();

    @Override
    public void initialize(ResourceManager manager) {
        try {
            this.paths.clear();
            this.json = FileUtils.readJSONFromAssets(manager.getAssets(), "resource_packs/vanilla/contents.json");
            this.content = this.json.getJSONArray("content");
        } catch (IOException e) {
            throw new RuntimeException("failed to read contents.json: " + e, e);
        } catch (JSONException e2) {
            throw new RuntimeException("failed to read contents.json: " + e2, e2);
        }
    }

    private void addPath(String path) throws JSONException {
        if (!this.paths.contains(path)) {
            this.paths.add(path);
            this.content.put(new JSONObject().put("path", path));
        }
    }

    @Override
    public void process(Resource resource, Collection<Resource> resources) {
        resources.add(resource);
        String resPath = resource.getPath();
        if (!resPath.endsWith(".png")) {
            resPath.endsWith(".tga");
        }
        try {
            List<ResourceOverride> overrides = new ArrayList<>();
            resource.addOverrides(overrides);
            for (ResourceOverride override : overrides) {
                addPath(override.path);
            }
        } catch (JSONException e) {
            throw new RuntimeException("unexpected error in ContentProcessor.process(): " + e, e);
        }
    }

    @Override
    public String getResourceName() {
        return "contents.json";
    }

    @Override
    public String getResourcePath() {
        return "contents.json";
    }

    @Override
    public void handle(RuntimeResource resource) {
        try {
            resource.getFile().getParentFile().mkdirs();
            FileUtils.writeJSON(resource.getFile(), this.json);
        } catch (IOException e) {
            throw new RuntimeException("unexpected exception inside ContentProcessor.handle()", e);
        }
    }
}
