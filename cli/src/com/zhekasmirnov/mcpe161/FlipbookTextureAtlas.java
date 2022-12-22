package com.zhekasmirnov.mcpe161;

import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.Resource;
import com.zhekasmirnov.horizon.modloader.resource.processor.ResourceProcessor;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResource;
import com.zhekasmirnov.horizon.modloader.resource.runtime.RuntimeResourceHandler;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.log.ICLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FlipbookTextureAtlas implements ResourceProcessor, RuntimeResourceHandler {
    public final String asset;
    private JSONArray flipbookTextures;
    public final String name;

    public FlipbookTextureAtlas(String asset, String name) {
        this.asset = asset;
        this.name = name;
    }

    @Override
    public void initialize(ResourceManager manager) {
        try {
            this.flipbookTextures = new JSONArray(FileUtils.readStringFromAsset(null, "resource_packs/vanilla/" + this.asset));
        } catch (IOException e) {
            throw new RuntimeException("failed to read json for flipbook atlas: " + this.asset + " " + e, e);
        } catch (JSONException e2) {
            throw new RuntimeException("failed to read json for flipbook atlas: " + this.asset + " " + e2, e2);
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
            FileUtils.writeJSON(resource.getFile(), this.flipbookTextures);
        } catch (IOException e) {
            throw new RuntimeException("unexpected exception inside TextureAtlas.handle()", e);
        }
    }

    private void insertPng(Resource resource) {
        int delay;
        String path = resource.getAtlasPath();
        List<String> nameParts = new ArrayList<>();
        Collections.addAll(nameParts, resource.getName().split("\\."));
        int replicate = 0;
        if (nameParts.size() > 2 && "liquid".equals(nameParts.get(1))) {
            replicate = 2;
            nameParts.remove(1);
        }
        if (nameParts.size() > 2 && nameParts.get(nameParts.size() - 2).equals("anim")) {
            delay = 1;
        } else if (nameParts.size() > 3 && nameParts.get(nameParts.size() - 3).equals("anim")) {
            try {
                delay = Integer.parseInt(nameParts.get(nameParts.size() - 2));
                if (delay < 1) {
                    Logger.error("BlockTextureAnimation", "invalid animation delay: " + path);
                    return;
                }
            } catch (Exception e) {
                Logger.error("BlockTextureAnimation", "invalid animation delay: " + path);
                return;
            }
        } else {
            Logger.error("BlockTextureAnimation", "invalid animation name: " + path);
            return;
        }
        String tile = nameParts.get(0);
        try {
            JSONObject json = new JSONObject();
            json.put("flipbook_texture", path);
            json.put("atlas_tile", tile);
            json.put("ticks_per_frame", delay);
            if (replicate > 1) {
                json.put("replicate", replicate);
            }
            this.flipbookTextures.put(json);
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
    }

    private void insertJson(Resource resource) {
        int delay;
        String path = resource.getAtlasPath();
        try {
            JSONObject data = FileUtils.readJSON(resource.file);
            if (data.has("name")) {
                String textureName = data.getString("name");
                if (data.has("tile")) {
                    String tile = data.getString("tile");
                    if (data.has("delay")) {
                        delay = data.optInt("delay");
                        if (delay < 1) {
                            Logger.error("BlockTextureAnimation", "animation json has invalid delay: " + path);
                        }
                    } else {
                        delay = 1;
                    }
                    int delay2 = delay;
                    try {
                        JSONObject json = new JSONObject();
                        json.put("flipbook_texture", textureName);
                        json.put("atlas_tile", tile);
                        json.put("ticks_per_frame", delay2);
                        this.flipbookTextures.put(json);
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                Logger.error("BlockTextureAnimation", "animation json missing tile: " + path);
                return;
            }
            Logger.error("BlockTextureAnimation", "animation json missing name: " + path);
        } catch (Exception e2) {
            Logger.error("BlockTextureAnimation", "animation json has invalid json: " + path);
            e2.printStackTrace();
        }
    }

    @Override
    public void process(Resource resource, Collection<Resource> resources) {
        resources.add(resource);
        String name = resource.getName();
        if (name.matches("[^\\.]+(\\.liquid)?\\.anim(\\.[0-9]+)?\\.png")) {
            ICLog.d("DEBUG", "INSERT PNG");
            insertPng(resource);
        } else if (name.endsWith(".json") && name.contains(".anim.")) {
            insertJson(resource);
        }
    }
}
