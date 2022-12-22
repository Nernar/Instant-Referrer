package com.zhekasmirnov.innercore.mod.resource;

import com.zhekasmirnov.horizon.modloader.resource.directory.Resource;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.resource.pack.IResourcePack;
import com.zhekasmirnov.innercore.mod.resource.types.ResourceFile;
import com.zhekasmirnov.innercore.mod.resource.types.TextureAnimationFile;
import com.zhekasmirnov.innercore.mod.resource.types.TextureAtlasDescription;
import com.zhekasmirnov.innercore.mod.resource.types.enums.TextureType;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResourceStorage implements IResourcePack {
    public static final String VANILLA_RESOURCE = "resource_packs/vanilla/";
    private static ArrayList<String> textureLoadQueue = new ArrayList<>();
    JSONArray animationList;
    TextureAtlasDescription blockTextureDescriptor;
    TextureAtlasDescription itemTextureDescriptor;
    private HashMap<String, String> resourceLinks = new HashMap<>();
    JSONArray textureList;
    private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$TextureType;

    public static native void nativeAddTextureToLoad(String str);

    static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$TextureType() {
        int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$TextureType;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[TextureType.valuesCustom().length];
        try {
            iArr2[TextureType.BLOCK.ordinal()] = 3;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[TextureType.DEFAULT.ordinal()] = 1;
        } catch (NoSuchFieldError unused2) {
        }
        try {
            iArr2[TextureType.GUI.ordinal()] = 5;
        } catch (NoSuchFieldError unused3) {
        }
        try {
            iArr2[TextureType.ITEM.ordinal()] = 2;
        } catch (NoSuchFieldError unused4) {
        }
        try {
            iArr2[TextureType.PARTICLE.ordinal()] = 4;
        } catch (NoSuchFieldError unused5) {
        }
        $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$TextureType = iArr2;
        return iArr2;
    }

    @Override
    public String getAbsolutePath() {
        return "/";
    }

    @Override
    public String getPackName() {
        return "Inner Core Resource Storage";
    }

    public void build() throws IOException, JSONException {
        this.itemTextureDescriptor = new TextureAtlasDescription("textures/item_texture.json");
        this.blockTextureDescriptor = new TextureAtlasDescription("textures/terrain_texture.json");
        this.animationList = FileTools.getAssetAsJSONArray("resource_packs/vanilla/textures/flipbook_textures.json");
        this.textureList = FileTools.getAssetAsJSONArray("resource_packs/vanilla/textures/textures_list.json");
    }

    public String getId() {
        return "innercore-resource-main";
    }

    public String getLinkedFilePath(String path) {
        String res = this.resourceLinks.get(path);
        return res != null ? res : path;
    }

    public void addResourceFile(TextureType type, Resource resource, String name) {
        try {
            switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$TextureType()[type.ordinal()]) {
                case 2:
                    this.itemTextureDescriptor.addTexturePath(name, resource.getIndex(), resource.getPath());
                    break;
                case 3:
                    this.blockTextureDescriptor.addTexturePath(name, resource.getIndex(), resource.getPath());
                    break;
            }
        } catch (JSONException e) {
            ICLog.e("INNERCORE-RESOURCES", "Cannot add texture path", e);
        }
    }

    private void addAsAnimation(ResourceFile file) {
        TextureAnimationFile animationFile = new TextureAnimationFile(file);
        if (animationFile.isValid()) {
            try {
                JSONObject animationJson = animationFile.constructAnimation();
                this.animationList.put(animationJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadAllTextures() {
        Iterator<String> it = textureLoadQueue.iterator();
        while (it.hasNext()) {
            String path = it.next();
            nativeAddTextureToLoad(path);
        }
    }

    public static void addTextureToLoad(String path) {
        textureLoadQueue.add(path);
        if (textureLoadQueue.size() < 2) {
            textureLoadQueue.add(path);
        }
    }
}
