package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class Callback {
    private static HashMap<String, ArrayList<CallbackFunction>> callbacks = new HashMap<>();

    private static class CallbackFunction {
        public final Function function;
        public final int priority;

        public CallbackFunction(Function function, int priority) {
            this.function = function;
            this.priority = priority;
        }
    }

    public static void addCallback(String name, Function func, int priority) {
        if (!callbacks.containsKey(name)) {
            callbacks.put(name, new ArrayList<>());
        }
        CallbackFunction callback = new CallbackFunction(func, priority);
        ArrayList<CallbackFunction> callbacksByName = callbacks.get(name);
        for (int i = 0; i < callbacksByName.size(); i++) {
            if (callbacksByName.get(i).priority < priority) {
                callbacksByName.add(i, callback);
                return;
            }
        }
        callbacksByName.add(callback);
    }

    public static void invokeCallbackV(String name, Object[] params) {
        Logger.debug("Callback", name + Arrays.toString(params));
        Context ctx = Compiler.assureContextForCurrentThread();
        ArrayList<CallbackFunction> funcs = callbacks.get(name);
        if (funcs != null) {
            Iterator<CallbackFunction> it = funcs.iterator();
            while (it.hasNext()) {
                CallbackFunction callback = it.next();
                Scriptable parent = callback.function.getParentScope();
                callback.function.call(ctx, parent, parent, params);
            }
        }
    }

    public static List<Runnable> getCallbackAsRunnableList(String name, final Object[] params) {
        List<Runnable> result = new ArrayList<>();
        ArrayList<CallbackFunction> funcs = callbacks.get(name);
        if (funcs != null) {
            Iterator<CallbackFunction> it = funcs.iterator();
            while (it.hasNext()) {
                final CallbackFunction func0 = it.next();
                result.add(new Runnable() {
                    @Override
                    public void run() {
                        Scriptable parent = func0.function.getParentScope();
                        func0.function.call(Compiler.assureContextForCurrentThread(), parent, parent, params);
                    }
                });
            }
        }
        return result;
    }

    public static void invokeCallback(String name, Object... params) {
        invokeCallbackV(name, params);
    }

    public static void invokeAPICallbackUnsafe(String name, Object[] params) {
        invokeCallbackV(name, params);
    }

    public static Throwable invokeAPICallback(String name, Object... params) {
        try {
            invokeAPICallbackUnsafe(name, params);
            return null;
        } catch (Throwable e) {
            ICLog.e("INNERCORE-CALLBACK", "error occurred while calling callback " + name, e);
            DialogHelper.reportNonFatalError("Non-Fatal error occurred in callback " + name, e);
            return e;
        }
    }
}
