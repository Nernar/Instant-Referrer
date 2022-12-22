package com.zhekasmirnov.innercore.mod.build.enums;

public enum BuildConfigError {
    NONE,
    FILE_ERROR,
    PARSE_ERROR;
    
    private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$BuildConfigError;

    public static BuildConfigError[] valuesCustom() {
        BuildConfigError[] valuesCustom = values();
        int length = valuesCustom.length;
        BuildConfigError[] buildConfigErrorArr = new BuildConfigError[length];
        System.arraycopy(valuesCustom, 0, buildConfigErrorArr, 0, length);
        return buildConfigErrorArr;
    }

    static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$BuildConfigError() {
        int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$BuildConfigError;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[valuesCustom().length];
        try {
            iArr2[FILE_ERROR.ordinal()] = 2;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[NONE.ordinal()] = 1;
        } catch (NoSuchFieldError unused2) {
        }
        try {
            iArr2[PARSE_ERROR.ordinal()] = 3;
        } catch (NoSuchFieldError unused3) {
        }
        $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$BuildConfigError = iArr2;
        return iArr2;
    }

    @Override
    public String toString() {
        switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$BuildConfigError()[ordinal()]) {
            case 1:
                return "No Error";
            case 2:
                return "File could not be loaded";
            case 3:
                return "JSON Parse Error";
            default:
                return "Unknown Error";
        }
    }
}
