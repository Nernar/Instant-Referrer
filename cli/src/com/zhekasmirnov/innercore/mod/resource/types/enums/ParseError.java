package com.zhekasmirnov.innercore.mod.resource.types.enums;

public enum ParseError {
    NONE,
    ANIMATION_INVALID_FILE,
    ANIMATION_INVALID_NAME,
    ANIMATION_INVALID_JSON,
    ANIMATION_NAME_MISSING,
    ANIMATION_TILE_MISSING,
    ANIMATION_INVALID_DELAY;
    
    private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$ParseError;

    public static ParseError[] valuesCustom() {
        ParseError[] valuesCustom = values();
        int length = valuesCustom.length;
        ParseError[] parseErrorArr = new ParseError[length];
        System.arraycopy(valuesCustom, 0, parseErrorArr, 0, length);
        return parseErrorArr;
    }

    static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$ParseError() {
        int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$ParseError;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[valuesCustom().length];
        try {
            iArr2[ANIMATION_INVALID_DELAY.ordinal()] = 7;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[ANIMATION_INVALID_FILE.ordinal()] = 2;
        } catch (NoSuchFieldError unused2) {
        }
        try {
            iArr2[ANIMATION_INVALID_JSON.ordinal()] = 4;
        } catch (NoSuchFieldError unused3) {
        }
        try {
            iArr2[ANIMATION_INVALID_NAME.ordinal()] = 3;
        } catch (NoSuchFieldError unused4) {
        }
        try {
            iArr2[ANIMATION_NAME_MISSING.ordinal()] = 5;
        } catch (NoSuchFieldError unused5) {
        }
        try {
            iArr2[ANIMATION_TILE_MISSING.ordinal()] = 6;
        } catch (NoSuchFieldError unused6) {
        }
        try {
            iArr2[NONE.ordinal()] = 1;
        } catch (NoSuchFieldError unused7) {
        }
        $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$ParseError = iArr2;
        return iArr2;
    }

    @Override
    public String toString() {
        switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$mod$resource$types$enums$ParseError()[ordinal()]) {
            case 1:
                return "No Error";
            case 2:
                return "Animation file has invalid extension (.png or .json needed)";
            case 3:
                return "Animation file has invalid name (use <tile>.anim.png or <tile>.anim.<delay>.png)";
            case 4:
                return "Animation json file could not be parsed";
            case 5:
                return "Animation json missing animation texture name";
            case 6:
                return "Animation json missing tile texture name";
            case 7:
                return "Animation delay is not a number or less than 1";
            default:
                return "Unknown Error";
        }
    }
}
