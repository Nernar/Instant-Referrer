package io.nernar.rhino;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

public class ThrowableResolver {
    private static Method assureContextForCurrentThread;

    static {
        try {
            try {
                assureContextForCurrentThread = Class.forName("com.zhekasmirnov.innercore.mod.executable.Compiler").getMethod("assureContextForCurrentThread", new Class[0]);
            } catch (NoClassDefFoundError e) {
                try {
                    assureContextForCurrentThread = Class.forName("zhekasmirnov.launcher.mod.executable.Compiler").getMethod("assureContextForCurrentThread", new Class[0]);
                } catch (ClassNotFoundException | NoSuchMethodException e2) {
                    throw new UnsupportedOperationException(e2);
                }
            } catch (NoSuchMethodException e3) {
                throw new UnsupportedOperationException(e3);
            }
        } catch (ClassNotFoundException e4) {
            throw new NoClassDefFoundError(e4.getMessage());
        }
    }

    private static Context assureContextForCurrentThread() {
        try {
            return (Context) assureContextForCurrentThread.invoke(null, new Object[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static Object invoke(Scriptable scriptable, Function function, Function function2) {
        try {
            return function.call(assureContextForCurrentThread(), scriptable != null ? scriptable.getParentScope() : null, scriptable, new Object[0]);
        } catch (Throwable th) {
            return function2.call(assureContextForCurrentThread(), scriptable != null ? scriptable.getParentScope() : null, scriptable, new Object[]{th});
        }
    }

    public static Object invoke(Function function, Function function2) {
        return invoke(function.getParentScope(), function, function2);
    }

    public static Object invokeRuntime(Scriptable scriptable, Function function, Function function2) {
        try {
            return function.call(assureContextForCurrentThread(), scriptable != null ? scriptable.getParentScope() : null, scriptable, new Object[0]);
        } catch (RuntimeException e) {
            return function2.call(assureContextForCurrentThread(), scriptable != null ? scriptable.getParentScope() : null, scriptable, new Object[]{e});
        }
    }

    public static Object invokeRuntime(Function function, Function function2) {
        return invoke(function.getParentScope(), function, function2);
    }

    public static Object invokeRhino(Scriptable scriptable, Function function, Function function2) {
        try {
            return function.call(assureContextForCurrentThread(), scriptable != null ? scriptable.getParentScope() : null, scriptable, new Object[0]);
        } catch (RhinoException e) {
            return function2.call(assureContextForCurrentThread(), scriptable != null ? scriptable.getParentScope() : null, scriptable, new Object[]{e});
        }
    }

    public static Object invokeRhino(Function function, Function function2) {
        return invoke(function.getParentScope(), function, function2);
    }
}
