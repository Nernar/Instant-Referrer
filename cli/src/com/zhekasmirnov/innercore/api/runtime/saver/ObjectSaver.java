package com.zhekasmirnov.innercore.api.runtime.saver;

import org.mozilla.javascript.ScriptableObject;

public abstract class ObjectSaver {
    private int saverId = 0;

    public abstract Object read(ScriptableObject scriptableObject);

    public abstract ScriptableObject save(Object obj);

    public int getSaverId() {
        return this.saverId;
    }

    public void setSaverId(int saverId) {
        this.saverId = saverId;
    }
}
