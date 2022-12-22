package com.zhekasmirnov.innercore.api.mod.coreengine;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import org.mozilla.javascript.ScriptableObject;

public class CoreEngineAPI extends API {
    public static final String LOGGER_TAG = "CORE-ENGINE";
    private static CEHandler ceHandlerSingleton;

    public static synchronized CEHandler getOrLoadCoreEngine() {
        return ceHandlerSingleton;
    }

    @Override
    public String getName() {
        return "CoreEngine";
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void onLoaded() {
        ICLog.i(getName(), "IT'S ACTUALLY STUB AND SHOULDN'T BE USED IN SOURCES!");
    }

    @Override
    public void onModLoaded(Mod mod) {
    }

    @Override
    public void onCallback(String name, Object[] args) {
    }

    @Override
    public void setupCallbacks(Executable executable) {
    }

    @SuppressWarnings("unused")
    private void transferValue(Executable executable, String name) {
    }

    @Override
    public void prepareExecutable(Executable executable) {
    }

    @Override
    public void injectIntoScope(ScriptableObject scope) {
    }
}
