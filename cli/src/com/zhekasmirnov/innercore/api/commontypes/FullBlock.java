package com.zhekasmirnov.innercore.api.commontypes;

import org.mozilla.javascript.ScriptableObject;

public class FullBlock extends ScriptableObject {
    public int data;
    public int id;

    @Override
    public String getClassName() {
        return "FullBlock";
    }

    public FullBlock(int id, int data) {
        this.id = id;
        put("id", this, Integer.valueOf(this.id));
        this.data = data;
        put("data", this, Integer.valueOf(this.data));
    }

    public FullBlock(int idData) {
        this.id = 65535 & idData;
        if ((idData >> 24) == 1) {
            this.id = -this.id;
        }
        put("id", this, Integer.valueOf(this.id));
        this.data = (idData >> 16) & 255;
        put("data", this, Integer.valueOf(this.data));
    }

    public FullBlock(Object blockSource, int x, int y, int z) {
        this.data = 0;
        this.id = 0;
        put("id", this, Integer.valueOf(this.id));
        put("data", this, Integer.valueOf(this.data));
    }

    public FullBlock(long actor, int x, int y, int z) {
        this((Object) null, x, y, z);
    }
}
