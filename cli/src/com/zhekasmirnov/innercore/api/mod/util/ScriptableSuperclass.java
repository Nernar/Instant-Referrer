package com.zhekasmirnov.innercore.api.mod.util;

import org.mozilla.javascript.ScriptableObject;

public class ScriptableSuperclass extends ScriptableObject {
    @Override
    public String getClassName() {
        return getClass().getSimpleName();
    }
}
