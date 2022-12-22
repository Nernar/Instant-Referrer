package com.zhekasmirnov.innercore.mod.executable.library;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Library extends Executable {
    private ArrayList<LibraryDependency> dependencies;
    private HashSet<String> exportNames;
    private ArrayList<LibraryExport> exports;
    private boolean isLoadingInProgress;
    @SuppressWarnings("unused")
    private boolean isOldFormatted;
    private boolean isShared;
    private String libName;
    private LibraryState state;
    private int versionCode;

    public Library(Context context, Script script, ScriptableObject scriptScope, CompilerConfig config, API apiInstance) {
        super(context, script, scriptScope, config, apiInstance);
        this.libName = null;
        this.versionCode = 0;
        this.isShared = false;
        this.dependencies = new ArrayList<>();
        this.state = LibraryState.NONE;
        this.isOldFormatted = false;
        this.isLoadingInProgress = false;
        this.exports = new ArrayList<>();
        this.exportNames = new HashSet<>();
    }

    public Library(Context context, ScriptableObject scriptScope, CompilerConfig config, API apiInstance) {
        super(context, scriptScope, config, apiInstance);
        this.libName = null;
        this.versionCode = 0;
        this.isShared = false;
        this.dependencies = new ArrayList<>();
        this.state = LibraryState.NONE;
        this.isOldFormatted = false;
        this.isLoadingInProgress = false;
        this.exports = new ArrayList<>();
        this.exportNames = new HashSet<>();
    }

    public String getLibName() {
        return this.libName;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public ArrayList<LibraryDependency> getDependencies() {
        return this.dependencies;
    }

    public boolean isShared() {
        return this.isShared;
    }

    private void setState(LibraryState state) {
        this.state = state;
    }

    public boolean isInvalid() {
        return this.state == LibraryState.INVALID;
    }

    public boolean isInitialized() {
        return this.state != LibraryState.NONE;
    }

    public boolean isPrepared() {
        return (!isInitialized() || isInvalid() || this.state == LibraryState.INITIALIZED) ? false : true;
    }

    public boolean isLoaded() {
        return this.state == LibraryState.LOADED;
    }

    private void onFatalException(Throwable exception) {
        setState(LibraryState.INVALID);
        this.lastRunException = exception;
        ICLog.e("INNERCORE-EXEC", "failed to run executable '" + this.name + "', some errors occurred:", exception);
    }

    private static class RunInterruptionException extends RuntimeException {
        private RunInterruptionException() {
        }

        RunInterruptionException(RunInterruptionException runInterruptionException) {
            this();
        }
    }

    private static class InvalidHeaderCall extends RuntimeException {
        public InvalidHeaderCall(String msg) {
            super(msg);
        }
    }

    public void headerCall(String name, int version, String apiName, boolean shared, ArrayList<LibraryDependency> dependencies) {
        if (name == null) {
            throw new InvalidHeaderCall("Error in library initialization - name is not given");
        }
        API api = API.getInstanceByName(apiName);
        if (api == null) {
            throw new InvalidHeaderCall("Error in library initialization - invalid API name: " + apiName);
        }
        if (!isPrepared()) {
            this.libName = name;
            this.versionCode = version;
            this.isShared = shared;
            this.apiInstance = api;
            this.dependencies = dependencies;
            prepare();
            throw new RunInterruptionException(null);
        }
    }

    public void initialize() {
        setState(LibraryState.INITIALIZED);
        this.scriptScope.put("LIBRARY", this.scriptScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] args) {
                ScriptableObjectWrapper params = new ScriptableObjectWrapper((Scriptable) args[0]);
                ArrayList<LibraryDependency> dependencies = new ArrayList<>();
                ScriptableObjectWrapper dependenciesArr = params.getScriptableWrapper("dependencies");
                if (dependenciesArr != null) {
                    Object[] strings = dependenciesArr.asArray();
                    for (Object str : strings) {
                        if (str instanceof String) {
                            LibraryDependency dependency = new LibraryDependency((String) str);
                            dependency.setParentMod(Library.this.getParentMod());
                            dependencies.add(dependency);
                        }
                    }
                }
                Library.this.headerCall(params.getString("name"), params.getInt("version"), params.getString("api"), params.getBoolean("shared"), dependencies);
                return null;
            }
        });
        try {
            runScript();
        } catch (InvalidHeaderCall err) {
            onFatalException(err);
        } catch (RunInterruptionException e) {
            this.isOldFormatted = false;
        } catch (Throwable th) {
            this.isOldFormatted = true;
            this.isShared = true;
            this.libName = this.compilerConfig.getName();
            this.versionCode = 0;
            if (this.libName.endsWith(".js")) {
                this.libName = this.libName.substring(0, this.libName.length() - 3);
            }
            prepare();
        }
    }

    public void prepare() {
        if (isInvalid() || !isInitialized()) {
            return;
        }
        try {
            injectStaticAPIs();
            if (this.apiInstance != null) {
                this.apiInstance.injectIntoScope(this.scriptScope);
                this.apiInstance.prepareExecutable(this);
            }
            new LibraryAnnotation("$EXPORT", new Class[]{CharSequence.class}).injectMethod(this.scriptScope);
            new LibraryAnnotation("$BACKCOMP", new Class[]{Number.class}).injectMethod(this.scriptScope);
            ScriptableFunctionImpl EXPORT = new ScriptableFunctionImpl() {
                @Override
                public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                    String name = (String) objects[0];
                    Object value = objects[1];
                    int targetVersion = -1;
                    if (name.contains(":")) {
                        String[] parts = name.split(":");
                        name = parts[0];
                        try {
                            targetVersion = Integer.valueOf(parts[1]).intValue();
                        } catch (NumberFormatException e) {
                            ICLog.i("ERROR", "invalid formatted library export name " + name + " target version will be ignored");
                        }
                    }
                    LibraryExport export = new LibraryExport(name, value);
                    export.setTargetVersion(targetVersion);
                    Library.this.addExport(export);
                    return null;
                }
            };
            this.scriptScope.put("registerAPIUnit", this.scriptScope, EXPORT);
            this.scriptScope.put("EXPORT", this.scriptScope, EXPORT);
            setState(LibraryState.PREPARED);
        } catch (Throwable err) {
            onFatalException(err);
        }
    }

    public boolean isLoadingInProgress() {
        return this.isLoadingInProgress;
    }

    public void load() {
        if (!isPrepared()) {
            onFatalException(new IllegalStateException("Trying to load library without calling prepare()"));
            return;
        }
        this.isLoadingInProgress = true;
        Iterator<LibraryDependency> it = this.dependencies.iterator();
        while (it.hasNext()) {
            LibraryDependency dependency = it.next();
            if (LibraryRegistry.resolveDependencyAndLoadLib(dependency) == null) {
                ICLog.i("ERROR", "failed to resolve dependency " + dependency + " for library " + this.libName + ", it may load incorrectly.");
            }
        }
        try {
            runScript();
            ArrayList<LibraryAnnotation.AnnotationSet> allAnnotations = LibraryAnnotation.getAllAnnotations(this.scriptScope);
            Iterator<LibraryAnnotation.AnnotationSet> it2 = allAnnotations.iterator();
            while (it2.hasNext()) {
                LibraryAnnotation.AnnotationSet annotationSet = it2.next();
                resolveAnnotations(annotationSet);
            }
            ICLog.d("LIBRARY", "library loaded " + this.libName + ":" + this.versionCode);
            setState(LibraryState.LOADED);
            this.isLoadingInProgress = false;
        } catch (Throwable err) {
            onFatalException(err);
            this.isLoadingInProgress = false;
        }
    }

    private void resolveAnnotations(LibraryAnnotation.AnnotationSet set) {
        LibraryAnnotation.AnnotationInstance exportAnnotation = set.find("$EXPORT");
        LibraryAnnotation.AnnotationInstance backCompAnnotation = set.find("$BACKCOMP");
        if (exportAnnotation != null) {
            LibraryExport export = new LibraryExport(((CharSequence) exportAnnotation.getParameter(0, CharSequence.class)).toString(), set.getTarget());
            if (backCompAnnotation != null) {
                export.setTargetVersion(((Number) backCompAnnotation.getParameter(0, Number.class)).intValue());
            }
            addExport(export);
        }
    }

    public HashSet<String> getExportNames() {
        return this.exportNames;
    }

    public void addExport(LibraryExport export) {
        if (export.name == null || export.name.equals("*")) {
            throw new IllegalArgumentException("invalid library export name: " + export.name);
        }
        this.exports.add(export);
        this.exportNames.add(export.name);
    }

    public LibraryExport getExportForDependency(LibraryDependency dependency, String exportName) {
        LibraryExport result = null;
        Iterator<LibraryExport> it = this.exports.iterator();
        while (it.hasNext()) {
            LibraryExport export = it.next();
            if (exportName.equals(export.name)) {
                if (result == null) {
                    result = export;
                }
                if (dependency.hasTargetVersion() && export.hasTargetVersion() && export.getTargetVersion() >= dependency.minVersion && (!result.hasTargetVersion() || result.getTargetVersion() > export.getTargetVersion())) {
                    result = export;
                }
            }
        }
        return result;
    }

    @Override
    public Object runForResult() {
        throw new UnsupportedOperationException("runForResult is not supported for library executables");
    }
}
