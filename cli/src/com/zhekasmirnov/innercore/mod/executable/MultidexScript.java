package com.zhekasmirnov.innercore.mod.executable;

import java.util.ArrayList;
import java.util.Iterator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class MultidexScript implements Script {
    private ArrayList<Script> scripts = new ArrayList<>();

    public void addScript(Script script) {
        this.scripts.add(script);
    }

    public int getScriptCount() {
        return this.scripts.size();
    }

    @Override
    public Object exec(Context context, Scriptable scriptable) {
        Object result = null;
        Context ctx = Compiler.assureContextForCurrentThread();
        Iterator<Script> it = this.scripts.iterator();
        while (it.hasNext()) {
            Script script = it.next();
            Object _result = script.exec(ctx, scriptable);
            if (_result != null) {
                result = _result;
            }
        }
        return result;
    }
}
