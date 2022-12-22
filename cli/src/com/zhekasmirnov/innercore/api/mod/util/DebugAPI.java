package com.zhekasmirnov.innercore.api.mod.util;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

public class DebugAPI extends ScriptableObject {
    @Override
    public String getClassName() {
        return "DebugAPI";
    }

    @SuppressWarnings("unused")
    private static Object constructDialog() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    private static Object constructDialog(String text) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    private static Object constructDialog(Object img) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static void dialog(String msg, String title) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static void dialog(String msg) {
        dialog(msg, "");
    }

    @JSStaticFunction
    public static void img(Object bmp, String prefix) {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static void img(Object bmp) {
        img(bmp, "");
    }
}
