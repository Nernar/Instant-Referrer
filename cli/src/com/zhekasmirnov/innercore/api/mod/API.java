package com.zhekasmirnov.innercore.api.mod;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.annotations.APIIgnore;
import com.zhekasmirnov.innercore.api.annotations.APIStaticModule;
import com.zhekasmirnov.innercore.api.constants.ConstantRegistry;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.PreferencesWindowAPI;
import com.zhekasmirnov.innercore.api.mod.coreengine.CoreEngineAPI;
import com.zhekasmirnov.innercore.api.mod.preloader.PreloaderAPI;
import com.zhekasmirnov.innercore.api.mod.util.DebugAPI;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSStaticFunction;

public abstract class API extends ScriptableObject {
    public static final String LOGGER_TAG = "INNERCORE-API";
    private static ScriptableObject currentScopeToInject = null;
    private static ArrayList<API> APIInstanceList = new ArrayList<>();
    protected boolean isLoaded = false;
    protected ArrayList<Executable> executables = new ArrayList<>();

    public abstract int getLevel();

    public abstract String getName();

    public abstract void onCallback(String str, Object[] objArr);

    public abstract void onLoaded();

    public abstract void onModLoaded(Mod mod);

    public abstract void setupCallbacks(Executable executable);

    public void prepareExecutable(Executable executable) {
        if (!this.isLoaded) {
            ICLog.d("INNERCORE-API", "loading API: " + getName());
            this.isLoaded = true;
            onLoaded();
        }
        ICLog.d("INNERCORE-API", "adding executable: api=" + getName() + " exec=" + executable.name);
        this.executables.add(executable);
        setupCallbacks(executable);
    }

    public void invokeExecutableCallback(String name, Object[] args) {
        Compiler.assureContextForCurrentThread();
        Iterator<Executable> it = this.executables.iterator();
        while (it.hasNext()) {
            Executable executable = it.next();
            executable.callFunction(name, args);
        }
    }

    protected void addExecutableCallback(Executable exec, String callbackName, String funcName) {
        Function func = exec.getFunction(funcName);
        if (func != null) {
            Callback.addCallback(callbackName, func, 0);
        }
    }

    @JSStaticFunction
    public String getCurrentAPIName() {
        return getName();
    }

    @JSStaticFunction
    public int getCurrentAPILevel() {
        return getLevel();
    }

    @Override
    public String getClassName() {
        return getName();
    }

    protected void injectIntoScope(ScriptableObject scope, List<String> filter) {
        currentScopeToInject = scope;
        injectIntoScope(getClass(), scope, filter);
        ConstantRegistry.injectConstants(scope);
    }

    public void injectIntoScope(ScriptableObject scope) {
        injectIntoScope(scope, null);
    }

    private static HashMap<Class<?>, ArrayList<String>> getAllClassMethods(Class<?> clazz, List<String> filter) {
        HashMap<Class<?>, ArrayList<String>> methodMap = new HashMap<>();
        while (clazz != null && clazz.getName().contains("com.zhekasmirnov.innercore")) {
            Method[] methods = clazz.getMethods();
            ArrayList<String> names = new ArrayList<>();
            if (!methodMap.containsKey(clazz)) {
                methodMap.put(clazz, names);
            }
            for (Method method : methods) {
                if ((filter == null || filter.contains(method.getName())) && (method.getAnnotation(JSFunction.class) != null || method.getAnnotation(JSStaticFunction.class) != null)) {
                    Class<?> methodClass = method.getDeclaringClass();
                    if (methodClass == clazz) {
                        names.add(method.getName());
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methodMap;
    }

    private static Collection<Class<?>> getSubclasses(Class<?> clazz) {
        HashSet<Class<?>> allClasses = new HashSet<>();
        while (clazz != null && clazz.getName().contains("com.zhekasmirnov.innercore")) {
            Class<?>[] classes = clazz.getClasses();
            for (Class<?> cla$$ : classes) {
                allClasses.add(cla$$);
            }
            clazz = clazz.getSuperclass();
        }
        return allClasses;
    }

    protected static void injectIntoScope(Class<?> apiClass, ScriptableObject scope, List<String> filter) {
        apiClass.getMethods();
        HashMap<Class<?>, ArrayList<String>> methodMap = getAllClassMethods(apiClass, filter);
        for (Class<?> methodClass : methodMap.keySet()) {
            ArrayList<String> names = methodMap.get(methodClass);
            String[] nameArr = new String[names.size()];
            names.toArray(nameArr);
            scope.defineFunctionProperties(nameArr, methodClass, 2);
        }
        Collection<Class<?>> classes = getSubclasses(apiClass);
        for (final Class<?> module : classes) {
            if (filter == null || filter.contains(module.getSimpleName())) {
                if (module.getAnnotation(APIStaticModule.class) != null) {
                    ScriptableObject childScope = new ScriptableObject() {
                        @Override
                        public String getClassName() {
                            return module.getSimpleName();
                        }
                    };
                    scope.defineProperty(module.getSimpleName(), childScope, 2);
                    injectIntoScope(module, childScope, null);
                } else if (module.getAnnotation(APIIgnore.class) == null) {
                    try {
                        scope.defineProperty(module.getSimpleName(), new NativeJavaClass(currentScopeToInject, module, false), 2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @JSStaticFunction
    public void createDump(Object _aClass) {
        Class<?> aClass = null;
        if (_aClass instanceof Class) {
            aClass = (Class<?>) _aClass;
        }
        if (aClass == null) {
            aClass = getClass();
        }
        String str = createDumpString(aClass, "", "");
        DebugAPI.dialog(str);
    }

    private String dumpMethod(Method method) {
        String str = String.valueOf(method.getName()) + "(";
        Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                str = String.valueOf(str) + ", ";
            }
            str = String.valueOf(str) + params[i].getSimpleName();
        }
        return String.valueOf(str) + ")";
    }

    private String createDumpString(Class<?> aClass, String prefix, String str) {
        Method[] methods = aClass.getMethods();
        String str2 = str;
        for (Method method : methods) {
            if (method.getAnnotation(JSStaticFunction.class) != null) {
                str2 = String.valueOf(str2) + prefix + dumpMethod(method) + "\n";
                Class<?> returnType = method.getReturnType();
                if (returnType != null && !returnType.isPrimitive()) {
                    Method[] returnMethods = returnType.getMethods();
                    String str3 = str2;
                    for (Method returnMethod : returnMethods) {
                        if ((returnMethod.getModifiers() & 8) == 0 && !returnMethod.getName().equals("getClassName") && returnMethod.getDeclaringClass().getPackage().toString().contains("zhekasmirnov")) {
                            str3 = String.valueOf(str3) + "\t" + dumpMethod(returnMethod) + "\n";
                        }
                    }
                    str2 = str3;
                }
            }
        }
        String str4 = String.valueOf(str2) + "\n";
        Class<?>[] classes = aClass.getClasses();
        for (Class<?> module : classes) {
            if (module.getAnnotation(APIStaticModule.class) != null) {
                str4 = createDumpString(module, String.valueOf(prefix) + module.getSimpleName() + ".", str4);
            }
        }
        return str4;
    }

    public static void loadAllAPIs() {
        registerInstance(new AdaptedScriptAPI());
        registerInstance(new PreferencesWindowAPI());
        registerInstance(new CoreEngineAPI());
        registerInstance(new PreloaderAPI());
    }

    protected static void registerInstance(API instance) {
        if (!APIInstanceList.contains(instance)) {
            Logger.debug("INNERCORE-API", "Register Mod API: " + instance.getName());
            APIInstanceList.add(instance);
        }
    }

    public static API getInstanceByName(String name) {
        for (int i = 0; i < APIInstanceList.size(); i++) {
            API apiInstance = APIInstanceList.get(i);
            if (apiInstance.getName().equals(name)) {
                return apiInstance;
            }
        }
        return null;
    }

    public static void invokeCallback(String name, Object... args) {
        Iterator<API> it = APIInstanceList.iterator();
        while (it.hasNext()) {
            API apiInstance = it.next();
            apiInstance.onCallback(name, args);
        }
    }

    public static ArrayList<Function> collectAllCallbacks(String name) {
        ArrayList<Function> callbacks = new ArrayList<>();
        Iterator<API> it = APIInstanceList.iterator();
        while (it.hasNext()) {
            API apiInstance = it.next();
            Iterator<Executable> it2 = apiInstance.executables.iterator();
            while (it2.hasNext()) {
                Executable exec = it2.next();
                Function callback = exec.getFunction(name);
                if (callback != null) {
                    callbacks.add(callback);
                }
            }
        }
        return callbacks;
    }

    public API newInstance() {
        try {
            API instance = (API) getClass().getConstructor(new Class[0]).newInstance(new Object[0]);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void debugLookUpClass(Class<?> clazz) {
        ICLog.d("LOOKUP", "starting at " + clazz);
        debugLookUpClass(clazz, "");
    }

    private static void debugLookUpClass(Class<?> clazz, String prefix) {
        if (clazz == null) {
            return;
        }
        String _prefix = String.valueOf(prefix) + "  ";
        HashMap<Class<?>, ArrayList<String>> methodMap = getAllClassMethods(clazz, null);
        for (Class<?> cla$$ : methodMap.keySet()) {
            ICLog.d("LOOKUP", String.valueOf(prefix) + "methods in class " + cla$$.getSimpleName() + ": ");
            Iterator<String> it = methodMap.get(cla$$).iterator();
            while (it.hasNext()) {
                String name = it.next();
                ICLog.d("LOOKUP", String.valueOf(_prefix) + name + "(...)");
            }
        }
        Collection<Class<?>> classes = getSubclasses(clazz);
        for (Class<?> cla$$2 : classes) {
            if (cla$$2 != null && cla$$2.getAnnotation(APIIgnore.class) == null) {
                if (cla$$2.getAnnotation(APIStaticModule.class) != null) {
                    ICLog.d("LOOKUP", String.valueOf(prefix) + "looking up module " + cla$$2.getSimpleName() + ":");
                } else {
                    ICLog.d("LOOKUP", String.valueOf(prefix) + "looking up constructor class " + cla$$2.getSimpleName() + ":");
                }
                debugLookUpClass(cla$$2, _prefix);
            }
        }
    }
}
