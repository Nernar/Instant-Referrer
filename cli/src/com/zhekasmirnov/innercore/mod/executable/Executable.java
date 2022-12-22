package com.zhekasmirnov.innercore.mod.executable;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.library.LibraryDependency;
import com.zhekasmirnov.innercore.mod.executable.library.LibraryRegistry;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.util.HashMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Executable implements Runnable {
    private static final HashMap<String, Scriptable> javaWrapCache = new HashMap<>();
    public API apiInstance;
    public CompilerConfig compilerConfig;
    private boolean isApiAdded;
    public boolean isLoadedFromDex;
    protected boolean isRunning;
    protected Throwable lastRunException;
    public String name;
    public Context parentContext;
    private Mod parentMod;
    public Script script;
    public ScriptableObject scriptScope;

    public void setParentMod(Mod parentMod) {
        this.parentMod = parentMod;
    }

    public Mod getParentMod() {
        return this.parentMod;
    }

    public Executable(Context context, Script script, ScriptableObject scriptScope, CompilerConfig config, API apiInstance) {
        this.parentMod = null;
        this.isLoadedFromDex = false;
        this.isRunning = false;
        this.lastRunException = null;
        this.isApiAdded = false;
        this.parentContext = context;
        this.script = script;
        this.scriptScope = scriptScope;
        this.apiInstance = apiInstance;
        this.compilerConfig = config;
        this.name = config.getName();
    }

    public Executable(Context context, ScriptableObject scriptScope, CompilerConfig config, API apiInstance) {
        this.parentMod = null;
        this.isLoadedFromDex = false;
        this.isRunning = false;
        this.lastRunException = null;
        this.isApiAdded = false;
        this.parentContext = context;
        this.script = null;
        this.scriptScope = scriptScope;
        this.apiInstance = apiInstance;
        this.compilerConfig = config;
        this.name = config.getName();
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public ScriptableObject getScope() {
        return this.scriptScope;
    }

    public void addToScope(ScriptableObject obj) {
        if (obj == null) {
            return;
        }
        Object[] keys = obj.getAllIds();
        for (Object key : keys) {
            if (key instanceof String) {
                this.scriptScope.put((String) key, this.scriptScope, obj.get(key));
            }
        }
    }

    public void injectValueIntoScope(String name, Object obj) {
        this.scriptScope.put(name, this.scriptScope, obj);
    }

    public Object evaluateStringInScope(String str) {
        return this.parentContext.evaluateString(this.scriptScope, str, this.name, 0, null);
    }

    public Object callFunction(String name, Object[] args) {
        Object _func = ScriptableObjectHelper.getProperty(this.scriptScope, name, null);
        if (_func == null || !(_func instanceof Function)) {
            return null;
        }
        Function func = (Function) _func;
        return func.call(this.parentContext, this.scriptScope, this.scriptScope, args);
    }

    public Function getFunction(String name) {
        Object _func = ScriptableObjectHelper.getProperty(this.scriptScope, name, null);
        if (_func == null || !(_func instanceof Function)) {
            return null;
        }
        return (Function) _func;
    }

    public Throwable getLastRunException() {
        return this.lastRunException;
    }

    public void injectStaticAPIs() {
        IDRegistry.injectAPI(this.scriptScope);
        Function importLib = new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                String libName = (String) params[0];
                String valueName = params.length > 1 ? (String) params[1] : "*";
                LibraryDependency dependency = new LibraryDependency(libName);
                dependency.setParentMod(Executable.this.getParentMod());
                LibraryRegistry.importLibrary(Executable.this.scriptScope, dependency, valueName);
                return null;
            }
        };
        this.scriptScope.put("importLib", this.scriptScope, importLib);
        this.scriptScope.put("IMPORT", this.scriptScope, importLib);
        this.scriptScope.put("IMPORT_NATIVE", this.scriptScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                throw new UnsupportedOperationException();
            }
        });
        this.scriptScope.put("WRAP_NATIVE", this.scriptScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                throw new UnsupportedOperationException();
            }
        });
        this.scriptScope.put("WRAP_JAVA", this.scriptScope, new ScriptableFunctionImpl() {
            @Override
            @SuppressWarnings("deprecation")
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                String name = (String) params[0];
                if (!name.contains("com.zhekasmirnov.horizon.launcher.ads")) {
                    Scriptable result = (Scriptable) Executable.javaWrapCache.get(name);
                    if (result != null) {
                        return result;
                    }
                    Scriptable nativeJavaPackage;
                    try {
                        nativeJavaPackage = new NativeJavaClass(parent, Class.forName(name), false);
                    } catch (ClassNotFoundException e) {
                        nativeJavaPackage = new NativeJavaPackage(name);
                    }
                    Executable.javaWrapCache.put(name, nativeJavaPackage);
                    return nativeJavaPackage;
                }
                throw new IllegalArgumentException("Unauthorized");
            }
        });
        this.scriptScope.put("__packdir__", this.scriptScope, FileTools.DIR_PACK);
        this.scriptScope.put("__modpack__", this.scriptScope, Context.javaToJS(ModPackContext.getInstance().assureJsAdapter(), this.scriptScope));
    }

    @Override
    public void run() {
        runForResult();
    }

    protected Object runScript() {
        Context context = Compiler.assureContextForCurrentThread();
        return this.script.exec(context, this.scriptScope);
    }

    protected void injectAPI() {
        if (!this.isApiAdded) {
            this.isApiAdded = true;
            injectStaticAPIs();
            if (this.apiInstance != null) {
                this.apiInstance.prepareExecutable(this);
            }
        }
    }

    public Object runForResult() {
        if (this.isRunning) {
            throw new RuntimeException("Could not run executable '" + this.name + "', it is already running");
        }
        this.isRunning = true;
        try {
            injectAPI();
            return runScript();
        } catch (Throwable e2) {
            this.lastRunException = e2;
            ICLog.e("INNERCORE-EXEC", "failed to inject API to executable '" + this.name + "', some errors occurred:", e2);
            return null;
        }
    }

    public void reset() {
        this.isRunning = false;
    }
}
