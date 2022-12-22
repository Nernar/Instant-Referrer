package com.zhekasmirnov.innercore.api.commontypes;

import android.util.Pair;
import org.mozilla.javascript.ScriptableObject;

public class ScriptableParams extends ScriptableObject {
    @Override
    public String getClassName() {
        return "Parameters";
    }

    @SuppressWarnings("unchecked")
    public ScriptableParams(Pair<String, Object>... pairArr) {
        for (Pair<String, Object> param : pairArr) {
            put(param.first, this, param.second);
        }
    }
}
