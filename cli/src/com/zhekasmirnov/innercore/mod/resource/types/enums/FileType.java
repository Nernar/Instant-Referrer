package com.zhekasmirnov.innercore.mod.resource.types.enums;

public enum FileType {
    RAW,
    JSON,
    EXECUTABLE,
    MANIFEST,
    TEXTURE,
    ANIMATION,
    INVALID;

    public static FileType[] valuesCustom() {
        FileType[] valuesCustom = values();
        int length = valuesCustom.length;
        FileType[] fileTypeArr = new FileType[length];
        System.arraycopy(valuesCustom, 0, fileTypeArr, 0, length);
        return fileTypeArr;
    }
}
