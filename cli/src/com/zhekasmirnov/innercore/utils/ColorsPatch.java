package com.zhekasmirnov.innercore.utils;

import org.mozilla.javascript.MembersPatch;

public class ColorsPatch {
    public static void init() {
        MembersPatch.addOverride("android.graphics.Color.rgb", "com.zhekasmirnov.innercore.utils.ColorsPatch.rgb");
        MembersPatch.addOverride("android.graphics.Color.argb", "com.zhekasmirnov.innercore.utils.ColorsPatch.argb");
    }

    public static int rgb(float red, float green, float blue) {
        throw new UnsupportedOperationException();
    }

    public static int argb(float alpha, float red, float green, float blue) {
        throw new UnsupportedOperationException();
    }
}
