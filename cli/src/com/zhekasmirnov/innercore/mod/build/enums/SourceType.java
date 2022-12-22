package com.zhekasmirnov.innercore.mod.build.enums;

public enum SourceType {
    PRELOADER("preloader"),
    LAUNCHER("launcher"),
    MOD("mod"),
    CUSTOM("custom"),
    LIBRARY("library");
    
    private final String name;

    public static SourceType[] valuesCustom() {
        SourceType[] valuesCustom = values();
        int length = valuesCustom.length;
        SourceType[] sourceTypeArr = new SourceType[length];
        System.arraycopy(valuesCustom, 0, sourceTypeArr, 0, length);
        return sourceTypeArr;
    }

    SourceType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static SourceType fromString(String str) {
        int hashCode = str.hashCode();
        if (hashCode == -1407250528) {
            if (str.equals("launcher")) {
                return LAUNCHER;
            }
        } else if (hashCode == -1349088399) {
            if (str.equals("custom")) {
                return CUSTOM;
            }
        } else if (hashCode != -1113514890) {
            if (hashCode == 166208699 && str.equals("library")) {
                return LIBRARY;
            }
        } else {
            if (str.equals("preloader")) {
                return PRELOADER;
            }
        }
        return MOD;
    }
}
