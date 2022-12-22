package com.zhekasmirnov.innercore.api.log;

public enum LogPrefix {
    DEBUG,
    INFO,
    LOADER,
    MOD,
    ERROR,
    WARNING;
    
    @SuppressWarnings("unused")
    private String name;
    private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$api$log$LogPrefix;

    public static LogPrefix[] valuesCustom() {
        LogPrefix[] valuesCustom = values();
        int length = valuesCustom.length;
        LogPrefix[] logPrefixArr = new LogPrefix[length];
        System.arraycopy(valuesCustom, 0, logPrefixArr, 0, length);
        return logPrefixArr;
    }

    static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$api$log$LogPrefix() {
        int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$api$log$LogPrefix;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[valuesCustom().length];
        try {
            iArr2[DEBUG.ordinal()] = 1;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[ERROR.ordinal()] = 5;
        } catch (NoSuchFieldError unused2) {
        }
        try {
            iArr2[INFO.ordinal()] = 2;
        } catch (NoSuchFieldError unused3) {
        }
        try {
            iArr2[LOADER.ordinal()] = 3;
        } catch (NoSuchFieldError unused4) {
        }
        try {
            iArr2[MOD.ordinal()] = 4;
        } catch (NoSuchFieldError unused5) {
        }
        try {
            iArr2[WARNING.ordinal()] = 6;
        } catch (NoSuchFieldError unused6) {
        }
        $SWITCH_TABLE$com$zhekasmirnov$innercore$api$log$LogPrefix = iArr2;
        return iArr2;
    }

    public String toFontColor() {
        switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$api$log$LogPrefix()[ordinal()]) {
            case 2:
                return "#0000FF";
            case 3:
            default:
                return "#FFFFFF";
            case 4:
                return "#FFFFFF";
            case 5:
                return "#FF0000";
            case 6:
                return "#FFFF00";
        }
    }

    public static LogPrefix fromString(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode == 2251950) {
            if (str.equals("INFO")) {
            }
            c = 65535;
        } else if (hashCode == 64921139) {
            if (str.equals("DEBUG")) {
            }
            c = 65535;
        } else if (hashCode != 66247144) {
            if (hashCode == 1842428796 && str.equals("WARNING")) {
            }
            c = 65535;
        } else {
            if (str.equals("ERROR")) {
            }
            c = 65535;
        }
        switch (c) {
            case 0:
                return DEBUG;
            case 1:
                return ERROR;
            case 2:
                return INFO;
            case 3:
                return WARNING;
            default:
                return str.contains("MOD") ? MOD : LOADER;
        }
    }
}
