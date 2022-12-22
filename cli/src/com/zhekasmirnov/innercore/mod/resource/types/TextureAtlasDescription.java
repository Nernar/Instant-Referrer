package com.zhekasmirnov.innercore.mod.resource.types;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TextureAtlasDescription {
    public JSONObject jsonObject;
    public JSONObject textureData;

    public TextureAtlasDescription(String resourcesPath) {
        this.textureData = new JSONObject();
        this.jsonObject = new JSONObject();
        try {
            this.jsonObject.put("texture_data", this.textureData);
        } catch (JSONException e) {
        }
        for (String resourcePack : MinecraftVersions.getCurrent().getVanillaResourcePacksDirs()) {
            try {
                JSONObject packTextureData = FileTools.getAssetAsJSON(String.valueOf(resourcePack) + resourcesPath).optJSONObject("texture_data");
                if (packTextureData != null) {
                    Iterator<String> it = packTextureData.keys();
                    while (it.hasNext()) {
                        String key = it.next();
                        this.textureData.put(key, packTextureData.opt(key));
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public TextureAtlasDescription(JSONObject content) {
        try {
            this.jsonObject = content;
            this.textureData = this.jsonObject.getJSONObject("texture_data");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addTexturePath(String name, int index, String path) throws JSONException {
        JSONObject textureUnit;
        if (this.textureData.has(name)) {
            textureUnit = this.textureData.getJSONObject(name);
        } else {
            textureUnit = new JSONObject();
            textureUnit.put("textures", new JSONArray());
        }
        JSONArray textureArray = textureUnit.optJSONArray("textures");
        if (textureArray == null) {
            textureArray = new JSONArray();
            textureArray.put(0, textureUnit.getString("textures"));
        }
        textureArray.put(index, path);
        textureUnit.put("textures", textureArray);
        this.textureData.put(name, textureUnit);
    }

    public int getTextureCount(String name) throws JSONException {
        if (this.textureData.has(name)) {
            JSONObject textureUnit = this.textureData.getJSONObject(name);
            JSONArray textureArray = textureUnit.optJSONArray("textures");
            return textureArray.length();
        }
        return 0;
    }

    public void addTextureFile(File texture, String path) throws JSONException {
        try {
            try {
                String filename = texture.getName();
                String filename2 = filename.substring(0, filename.lastIndexOf(46));
                addTexturePath(filename2.substring(0, filename2.lastIndexOf(95)), Integer.valueOf(filename2.substring(filename2.lastIndexOf(95) + 1)).intValue(), path);
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                String filename3 = texture.getName();
                String name = filename3.substring(0, filename3.lastIndexOf(46));
                int index = getTextureCount(name);
                if (index > 0) {
                    ICLog.i("ERROR", "found texture with no index that conflicts with already added texture, add aborted");
                } else {
                    addTexturePath(name, index, path);
                }
            }
        } catch (Exception e2) {
            ICLog.i("ERROR", "invalid texture file name: " + texture.getName() + ", failed with error " + e2);
        }
    }

    public String getTextureName(String name, int id) {
        JSONObject textureUnit;
        if (this.textureData.has(name) && (textureUnit = this.textureData.optJSONObject(name)) != null) {
            JSONArray textureArray = textureUnit.optJSONArray("textures");
            if (textureArray != null) {
                return textureArray.optString(id, null);
            }
            String texture = textureUnit.optString("textures");
            if (texture != null) {
                return texture;
            }
            return null;
        }
        return null;
    }
}
