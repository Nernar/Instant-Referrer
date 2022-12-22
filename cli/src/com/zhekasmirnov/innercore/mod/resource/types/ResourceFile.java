package com.zhekasmirnov.innercore.mod.resource.types;

import com.zhekasmirnov.innercore.mod.resource.pack.IResourcePack;
import com.zhekasmirnov.innercore.mod.resource.types.enums.AnimationType;
import com.zhekasmirnov.innercore.mod.resource.types.enums.FileType;
import com.zhekasmirnov.innercore.mod.resource.types.enums.ParseError;
import com.zhekasmirnov.innercore.mod.resource.types.enums.TextureType;
import java.io.File;

public class ResourceFile extends File {
    private AnimationType animationType;
    @SuppressWarnings("unused")
    private String packDir;
    protected ParseError parseError;
    private IResourcePack resourcePack;
    private TextureType textureType;
    private FileType type;

    public ResourceFile(String path) {
        super(path);
        this.parseError = ParseError.NONE;
        String name = getName();
        if (name.contains(".anim.")) {
            this.type = FileType.ANIMATION;
            if (path.endsWith(".png")) {
                this.animationType = AnimationType.TEXTURE;
            } else if (path.endsWith(".json")) {
                this.animationType = AnimationType.DESCRIPTOR;
            } else {
                this.type = FileType.INVALID;
                this.parseError = ParseError.ANIMATION_INVALID_NAME;
            }
        } else if (!name.endsWith(".png") && !name.endsWith(".tga")) {
            if (name.endsWith(".json")) {
                if (name.equals("pack_manifest.json")) {
                    this.type = FileType.MANIFEST;
                } else {
                    this.type = FileType.JSON;
                }
            } else if (name.endsWith(".js")) {
                this.type = FileType.EXECUTABLE;
            } else {
                this.type = FileType.RAW;
            }
        } else {
            this.type = FileType.TEXTURE;
            if (path.contains("items-opaque/")) {
                this.textureType = TextureType.ITEM;
            } else if (path.contains("terrain-atlas/")) {
                this.textureType = TextureType.BLOCK;
            } else if (path.contains("particle-atlas/")) {
                this.textureType = TextureType.PARTICLE;
            } else {
                this.textureType = TextureType.DEFAULT;
            }
        }
    }

    public ResourceFile(File f) {
        this(f.getAbsolutePath());
    }

    public ResourceFile(IResourcePack pack, File f) {
        this(f);
        setResourcePack(pack);
    }

    public IResourcePack getResourcePack() {
        return this.resourcePack;
    }

    public void setResourcePack(IResourcePack pack) {
        this.resourcePack = pack;
    }

    public String getLocalPath() {
        String path = getAbsolutePath();
        if (this.resourcePack == null) {
            return path;
        }
        return path.substring(this.resourcePack.getAbsolutePath().length());
    }

    public String getLocalDir() {
        String locPath = getLocalPath();
        return locPath.substring(0, locPath.lastIndexOf(47) + 1);
    }

    public FileType getType() {
        return this.type;
    }

    public TextureType getTextureType() {
        return this.textureType;
    }

    public AnimationType getAnimationType() {
        return this.animationType;
    }

    public ParseError getParseError() {
        return this.parseError;
    }
}
