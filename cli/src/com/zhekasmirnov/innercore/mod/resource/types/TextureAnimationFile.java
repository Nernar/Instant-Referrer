package com.zhekasmirnov.innercore.mod.resource.types;

import com.zhekasmirnov.innercore.mod.resource.types.enums.AnimationType;
import com.zhekasmirnov.innercore.mod.resource.types.enums.FileType;
import com.zhekasmirnov.innercore.mod.resource.types.enums.ParseError;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class TextureAnimationFile extends ResourceFile {
    private int delay;
    private boolean isValid;
    private int replicate;
    private String textureName;
    private String tileToAnimate;
    private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$AnimationType;

    static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$AnimationType() {
        int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$AnimationType;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[AnimationType.valuesCustom().length];
        try {
            iArr2[AnimationType.DESCRIPTOR.ordinal()] = 2;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[AnimationType.TEXTURE.ordinal()] = 1;
        } catch (NoSuchFieldError unused2) {
        }
        $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$AnimationType = iArr2;
        return iArr2;
    }

    public TextureAnimationFile(String path) {
        super(path);
        this.isValid = false;
        this.replicate = 0;
        read();
    }

    public TextureAnimationFile(ResourceFile file) {
        super(file.getResourcePack(), file);
        this.isValid = false;
        this.replicate = 0;
        read();
    }

    public boolean isValid() {
        return this.isValid;
    }

    private void read() {
        if (getType() != FileType.ANIMATION) {
            this.parseError = ParseError.ANIMATION_INVALID_FILE;
            return;
        }
        switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$AnimationType()[getAnimationType().ordinal()]) {
            case 1:
                List<String> nameParts = new ArrayList<>();
                Collections.addAll(nameParts, getName().split("\\."));
                this.replicate = 0;
                if (nameParts.size() > 2 && "liquid".equals(nameParts.get(1))) {
                    this.replicate = 2;
                    nameParts.remove(1);
                }
                if (nameParts.size() > 2 && nameParts.get(nameParts.size() - 2).equals("anim")) {
                    this.delay = 1;
                } else if (nameParts.size() > 3 && nameParts.get(nameParts.size() - 3).equals("anim")) {
                    try {
                        this.delay = Integer.parseInt(nameParts.get(nameParts.size() - 2));
                        if (this.delay < 1) {
                            this.parseError = ParseError.ANIMATION_INVALID_DELAY;
                            return;
                        }
                    } catch (Exception e) {
                        this.parseError = ParseError.ANIMATION_INVALID_DELAY;
                        return;
                    }
                } else {
                    this.parseError = ParseError.ANIMATION_INVALID_NAME;
                    return;
                }
                this.tileToAnimate = nameParts.get(0);
                this.textureName = getLocalPath();
                break;
            case 2:
                try {
                    JSONObject data = FileTools.readJSON(getAbsolutePath());
                    if (data.has("name")) {
                        this.textureName = data.getString("name");
                        if (data.has("tile")) {
                            this.tileToAnimate = data.getString("tile");
                            if (data.has("delay")) {
                                this.delay = data.optInt("delay");
                                if (this.delay < 1) {
                                    this.parseError = ParseError.ANIMATION_INVALID_DELAY;
                                }
                            } else {
                                this.delay = 1;
                            }
                            this.replicate = data.optInt("replicate");
                            break;
                        } else {
                            this.parseError = ParseError.ANIMATION_TILE_MISSING;
                            return;
                        }
                    } else {
                        this.parseError = ParseError.ANIMATION_NAME_MISSING;
                        return;
                    }
                } catch (Exception e2) {
                    this.parseError = ParseError.ANIMATION_INVALID_JSON;
                    e2.printStackTrace();
                    break;
                }
            default:
                return;
        }
        this.isValid = true;
    }

    public JSONObject constructAnimation() throws JSONException {
        if (!this.isValid) {
            return null;
        }
        JSONObject data = new JSONObject();
        data.put("atlas_tile", new StringBuilder(String.valueOf(this.tileToAnimate)).toString());
        data.put("flipbook_texture", new StringBuilder(String.valueOf(this.textureName)).toString());
        data.put("ticks_per_frame", this.delay);
        if (this.replicate > 1) {
            data.put("replicate", this.replicate);
        }
        return data;
    }
}
