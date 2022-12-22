package com.zhekasmirnov.apparatus.adapter.innercore.serialization;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.ScriptableObject;

public class ScriptableData {
    private ScriptableObject scriptable = ScriptableObjectHelper.createEmpty();

    public void setScriptable(ScriptableObject scriptable) {
        this.scriptable = scriptable;
    }

    public ScriptableObject getScriptable() {
        return this.scriptable;
    }

    public void put(String key, Object value) {
    }
}
