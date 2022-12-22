package com.zhekasmirnov.innercore.api.unlimited;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

public class BlockVariant {
    public final int data;
    public final boolean inCreative;
    public boolean isTechnical;
    public final String name;
    public int renderType = 0;
    public BlockShape shape = new BlockShape();
    public final int[] textureIds;
    public final String[] textures;
    public final int uid;

    public BlockVariant(int uid, int data, String name, String[] textures, int[] textureIds, boolean inCreative) {
        this.name = name;
        this.textures = textures;
        this.textureIds = textureIds;
        this.inCreative = inCreative;
        this.uid = uid;
        this.data = data;
        validate();
    }

    public BlockVariant(int uid, int data, ScriptableObject obj) {
        this.uid = uid;
        this.data = data;
        String name = ScriptableObjectHelper.getStringProperty(obj, "name", null);
        name = name != null ? NameTranslation.fixUnicodeIfRequired("block_" + uid + "_" + data, name) : name;
        this.name = name;
        this.inCreative = ScriptableObjectHelper.getBooleanProperty(obj, "inCreative", false);
        this.isTechnical = ScriptableObjectHelper.getBooleanProperty(obj, "isTech", !this.inCreative);
        this.textures = new String[6];
        this.textureIds = new int[6];
        try {
            NativeArray _texs = ScriptableObjectHelper.getNativeArrayProperty(obj, "texture", ScriptableObjectHelper.getNativeArrayProperty(obj, "textures", null));
            if (_texs != null) {
                Object[] texs = _texs.toArray();
                int i = 0;
                while (i < 6) {
                    Object _tex = texs[i > texs.length - 1 ? texs.length - 1 : i];
                    if (_tex != null && (_tex instanceof NativeArray)) {
                        Object[] tex = ((NativeArray) _tex).toArray();
                        if ((tex[0] instanceof CharSequence) && (tex[1] instanceof Number)) {
                            this.textures[i] = tex[0].toString();
                            this.textureIds[i] = ((Number) tex[1]).intValue();
                            if (!ResourcePackManager.isValidBlockTexture(this.textures[i], this.textureIds[i])) {
                                Logger.debug("WARNING", "invalid block texture: " + this.textures[i] + " " + this.textureIds[i]);
                                this.textures[i] = "missing_block";
                                this.textureIds[i] = 0;
                            }
                        }
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        validate();
        NameTranslation.sendNameToGenerateCache(uid, data, name);
    }

    private void validate() {
        for (int i = 0; i < 6; i++) {
            if (this.textures[i] == null) {
                this.textures[i] = "missing_block";
                this.textureIds[i] = 0;
            }
        }
        for (int i2 = 0; i2 < 6; i2++) {
            if (!ResourcePackManager.isValidBlockTexture(this.textures[i2], this.textureIds[i2])) {
                Logger.debug("INNERCORE-BLOCKS", "invalid block texture will be replaced with default: " + this.textures[i2] + " " + this.textureIds[i2]);
                this.textures[i2] = "missing_block";
                this.textureIds[i2] = 0;
            }
        }
    }

    public Object getGuiBlockModel() {
        throw new UnsupportedOperationException();
    }

    public String getSpriteTexturePath() {
        return ResourcePackManager.getBlockTextureName(this.textures[0], this.textureIds[0]);
    }
}
