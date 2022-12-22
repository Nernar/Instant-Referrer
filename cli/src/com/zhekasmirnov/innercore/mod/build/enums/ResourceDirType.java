package com.zhekasmirnov.innercore.mod.build.enums;

public enum ResourceDirType {
    RESOURCE("resource"),
    GUI("gui");
    
    private final String name;

    public static ResourceDirType[] valuesCustom() {
        ResourceDirType[] valuesCustom = values();
        int length = valuesCustom.length;
        ResourceDirType[] resourceDirTypeArr = new ResourceDirType[length];
        System.arraycopy(valuesCustom, 0, resourceDirTypeArr, 0, length);
        return resourceDirTypeArr;
    }

    ResourceDirType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static ResourceDirType fromString(String str) {
        if (((str.hashCode() == 102715 && str.equals("gui")) ? (char) 0 : (char) 65535) == 0) {
            return GUI;
        }
        return RESOURCE;
    }
}
