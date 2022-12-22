package com.zhekasmirnov.innercore.mod.resource.types.enums;

public enum AnimationType {
    TEXTURE,
    DESCRIPTOR;

    public static AnimationType[] valuesCustom() {
        AnimationType[] valuesCustom = values();
        int length = valuesCustom.length;
        AnimationType[] animationTypeArr = new AnimationType[length];
        System.arraycopy(valuesCustom, 0, animationTypeArr, 0, length);
        return animationTypeArr;
    }
}
