package com.zhekasmirnov.innercore.mod.build.enums;

public enum SourceType {
    PRELOADER("preloader"),
    LAUNCHER("launcher"),
    MOD("mod"),
    CUSTOM("custom"),
    LIBRARY("library"),
	INSTANT("instant");
    
    private final String name;
	
    private SourceType(String str) {
        this.name = str;
    }
	
    public static SourceType fromString(String str) {
        char c = 65535;
        switch (str.hashCode()) {
			case -1113514890:
                if (str.equals("preloader")) {
                    c = 0;
                }
                break;
            case -1407250528:
                if (str.equals("launcher")) {
                    c = 1;
                }
                break;
			case 166208699:
                if (str.equals("library")) {
                    c = 2;
                }
                break;
            case -1349088399:
                if (str.equals("custom")) {
                    c = 3;
                }
                break;
			case 1957570017:
				if (str.equals("instant")) {
					c = 4;
				}
				break;
        }
        switch (c) {
            case 0:
                return PRELOADER;
            case 1:
                return LAUNCHER;
            case 2:
                return LIBRARY;
            case 3:
                return CUSTOM;
			case 4:
				return INSTANT;
            default:
                return MOD;
        }
    }
	
    public String toString() {
        return this.name;
    }
}
