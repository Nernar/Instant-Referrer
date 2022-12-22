package com.zhekasmirnov.innercore.mod.resource.types.enums;

public enum TextureType {
    DEFAULT,
    ITEM,
    BLOCK,
    PARTICLE,
    GUI;

    public static TextureType[] valuesCustom() {
        TextureType[] valuesCustom = values();
        int length = valuesCustom.length;
        TextureType[] textureTypeArr = new TextureType[length];
        System.arraycopy(valuesCustom, 0, textureTypeArr, 0, length);
        return textureTypeArr;
    }
}
