package com.zhekasmirnov.innercore.api.mod.coreengine;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

public class CEHandler {
    public static final Object CALL_FAILED = new Object();
    public static final Object GET_FAILED = new Object();
    public final Executable coreEngine;
    private ScriptableObject scope = null;
    private boolean isLoaded = false;

    public CEHandler(Executable coreEngine) {
        this.coreEngine = coreEngine;
    }

    public void load() {
        if (!this.isLoaded) {
            this.isLoaded = true;
            this.scope = this.coreEngine.getScope();
            this.coreEngine.run();
        }
    }

    public Object callMethod(String name, Object... params) {
        if (!this.isLoaded || this.scope == null) {
            return CALL_FAILED;
        }
        Object _func = this.scope.get(name);
        if (_func instanceof Function) {
            return ((Function) _func).call(Compiler.assureContextForCurrentThread(), this.scope, this.scope, params);
        }
        return CALL_FAILED;
    }

    public void injectCoreAPI(ScriptableObject scope) {
        Object result = callMethod("injectCoreAPI", scope);
        if (result == CALL_FAILED) {
            ICLog.e("CORE-ENGINE", "failed to inject CoreAPI: method call failed", new RuntimeException());
        }
    }

    public Object getValue(String name) {
        if (!this.isLoaded || this.scope == null) {
            return CALL_FAILED;
        }
        return this.scope.get(name);
    }

    public Object requireGlobal(String name) {
        if (!this.isLoaded || this.scope == null) {
            return CALL_FAILED;
        }
        return this.coreEngine.evaluateStringInScope(name);
    }
}
