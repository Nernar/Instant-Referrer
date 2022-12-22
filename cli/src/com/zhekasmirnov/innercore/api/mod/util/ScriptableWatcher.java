package com.zhekasmirnov.innercore.api.mod.util;

import java.util.ArrayList;
import java.util.Iterator;
import org.mozilla.javascript.ScriptableObject;

public class ScriptableWatcher {
    private int checkPosition;
    public ScriptableObject object;
    private boolean isDirty = false;
    private ArrayList<Object> cached = new ArrayList<>();

    public ScriptableWatcher(ScriptableObject object) {
        this.object = object;
        refresh();
        validate();
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public void validate() {
        this.isDirty = false;
    }

    public void invalidate() {
        this.isDirty = true;
    }

    public void setTarget(ScriptableObject object) {
        this.object = object;
    }

    public void refresh() {
        this.checkPosition = 0;
        updateCached(this.object);
    }

    private void updateCached(ScriptableObject obj) {
        if (this.checkPosition > 128) {
            return;
        }
        if (obj == null) {
            updateSymbol("null");
            return;
        }
        Object[] keys = obj.getAllIds();
        for (Object key : keys) {
            Object val = obj.get(key);
            updateSymbol(key);
            if (val instanceof ScriptableObject) {
                updateSymbol("{");
                updateCached((ScriptableObject) val);
                updateSymbol("}");
            } else {
                updateSymbol(val);
            }
        }
    }

    private void updateSymbol(Object value) {
        if (value == null) {
            value = "null";
        }
        if (this.cached.size() <= this.checkPosition) {
            this.cached.add(value);
            this.isDirty = true;
        } else if (!this.cached.get(this.checkPosition).equals(value)) {
            this.cached.set(this.checkPosition, value);
            this.isDirty = true;
        }
        this.checkPosition++;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        Iterator<Object> it = this.cached.iterator();
        while (it.hasNext()) {
            Object val = it.next();
            result.append(val.toString());
            result.append(" ");
        }
        return "{" + ((Object) result) + "}";
    }
}
