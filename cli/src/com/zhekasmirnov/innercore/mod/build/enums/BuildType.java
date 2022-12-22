package com.zhekasmirnov.innercore.mod.build.enums;

public enum BuildType {
    RELEASE("release"),
    DEVELOP("develop");
    
    private final String name;

    public static BuildType[] valuesCustom() {
        BuildType[] valuesCustom = values();
        int length = valuesCustom.length;
        BuildType[] buildTypeArr = new BuildType[length];
        System.arraycopy(valuesCustom, 0, buildTypeArr, 0, length);
        return buildTypeArr;
    }

    BuildType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static BuildType fromString(String str) {
        if (((str.hashCode() == 1090594823 && str.equals("release")) ? (char) 0 : (char) 65535) == 0) {
            return RELEASE;
        }
        return DEVELOP;
    }
}
