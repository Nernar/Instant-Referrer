package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import com.zhekasmirnov.innercore.mod.build.enums.AnalyzedModType;
import com.zhekasmirnov.innercore.mod.build.enums.BuildType;
import com.zhekasmirnov.innercore.mod.build.enums.ResourceDirType;
import com.zhekasmirnov.innercore.mod.build.enums.SourceType;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;

public class ModBuilder {
    public static final String LOGGER_TAG = "INNERCORE-MOD-BUILD";
    private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$SourceType;
    private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$AnalyzedModType;

    static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$SourceType() {
        int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$SourceType;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[SourceType.valuesCustom().length];
        try {
            iArr2[SourceType.CUSTOM.ordinal()] = 4;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[SourceType.LAUNCHER.ordinal()] = 2;
        } catch (NoSuchFieldError unused2) {
        }
        try {
            iArr2[SourceType.LIBRARY.ordinal()] = 5;
        } catch (NoSuchFieldError unused3) {
        }
        try {
            iArr2[SourceType.MOD.ordinal()] = 3;
        } catch (NoSuchFieldError unused4) {
        }
        try {
            iArr2[SourceType.PRELOADER.ordinal()] = 1;
        } catch (NoSuchFieldError unused5) {
        }
        $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$SourceType = iArr2;
        return iArr2;
    }

    static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$AnalyzedModType() {
        int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$AnalyzedModType;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[AnalyzedModType.valuesCustom().length];
        try {
            iArr2[AnalyzedModType.CORE_ENGINE_MOD.ordinal()] = 2;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[AnalyzedModType.INNER_CORE_MOD.ordinal()] = 1;
        } catch (NoSuchFieldError unused2) {
        }
        try {
            iArr2[AnalyzedModType.MODPE_MOD_ARRAY.ordinal()] = 3;
        } catch (NoSuchFieldError unused3) {
        }
        try {
            iArr2[AnalyzedModType.RESOUCE_PACK.ordinal()] = 4;
        } catch (NoSuchFieldError unused4) {
        }
        try {
            iArr2[AnalyzedModType.UNKNOWN.ordinal()] = 5;
        } catch (NoSuchFieldError unused5) {
        }
        $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$AnalyzedModType = iArr2;
        return iArr2;
    }

    public static BuildConfig loadBuildConfigForDir(String dir) {
        BuildConfig config = new BuildConfig(new File(String.valueOf(dir) + "/build.config"));
        config.read();
        return config;
    }

    public static void addGuiDir(String dir, BuildConfig.ResourceDir resourceDir) {
        String path = String.valueOf(dir) + resourceDir.path;
        if (!FileTools.exists(path)) {
            ICLog.d("INNERCORE-MOD-BUILD", "failed to import resource or ui dir " + resourceDir.path + ": it does not exist");
        }
    }

    public static String checkRedirect(String dir) {
        File redirectFile = new File(String.valueOf(dir) + ".redirect");
        if (redirectFile.exists()) {
            try {
                return FileTools.readFileText(redirectFile.getAbsolutePath()).trim();
            } catch (IOException e) {
            }
        }
        return dir;
    }

    private static class LauncherScope extends ScriptableObject {
        Mod mod;

        public LauncherScope(Mod mod) {
            this.mod = mod;
        }

        @JSFunction
        public void Launch(ScriptableObject scope) {
            this.mod.RunMod(scope);
        }

        @JSFunction
        public void ConfigureMultiplayer(ScriptableObject props) {
            ScriptableObjectWrapper wrapper = new ScriptableObjectWrapper(props);
            this.mod.configureMultiplayer(wrapper.getString("name"), wrapper.getString("version"), wrapper.getBoolean("isClientOnly") || wrapper.getBoolean("isClientSide"));
        }

        @Override
        public String getClassName() {
            return "LauncherAPI";
        }
    }

    private static void setupLauncherScript(Executable launcherScript, Mod mod) {
        LauncherScope scope = new LauncherScope(mod);
        scope.defineFunctionProperties(new String[]{"Launch", "ConfigureMultiplayer"}, scope.getClass(), 2);
        launcherScript.addToScope(scope);
    }

    private static Executable compileOrLoadExecutable(Mod mod, CompiledSources compiledSources, BuildConfig.Source source) throws IOException {
        CompilerConfig compilerConfig = source.getCompilerConfig();
        compilerConfig.setModName(mod.getName());
        if (mod.getBuildType() == BuildType.RELEASE) {
            Executable execFromDex = compiledSources.getCompiledExecutableFor(source.path, compilerConfig);
            if (execFromDex != null) {
                return execFromDex;
            }
            ICLog.d("INNERCORE-MOD-BUILD", "no multidex executable created for " + source.path);
        }
        Reader sourceReader = new FileReader(new File(String.valueOf(mod.dir) + source.path));
        compilerConfig.setOptimizationLevel(-1);
        return Compiler.compileReader(sourceReader, compilerConfig);
    }

    public static Mod buildModForDir(String dir, ModPack modPack, String locationName) {
        String dir2;
        String dir3 = checkRedirect(dir);
        if (!FileTools.exists(dir3)) {
            ICLog.d("INNERCORE-MOD-BUILD", "failed to load mod, dir does not exist, maybe redirect file is pointing to the missing dir " + dir3);
            return null;
        }
        Mod builtMod = new Mod(dir3);
        builtMod.setModPackAndLocation(modPack, locationName);
        builtMod.buildConfig = loadBuildConfigForDir(dir3);
        if (!builtMod.buildConfig.isValid()) {
            return null;
        }
        builtMod.buildConfig.save();
        ModDebugInfo debugInfo = builtMod.getDebugInfo();
        if (builtMod.buildConfig.getBuildType() == BuildType.DEVELOP) {
            ArrayList<BuildConfig.BuildableDir> buildableDirs = builtMod.buildConfig.buildableDirs;
            for (int i = 0; i < buildableDirs.size(); i++) {
                BuildConfig.BuildableDir buildableDir = buildableDirs.get(i);
                BuildHelper.buildDir(dir3, buildableDir);
            }
        }
        ArrayList<BuildConfig.ResourceDir> resourceDirs = builtMod.buildConfig.resourceDirs;
        for (int i2 = 0; i2 < resourceDirs.size(); i2++) {
            BuildConfig.ResourceDir resourceDir = resourceDirs.get(i2);
            if (resourceDir.resourceType == ResourceDirType.GUI) {
                addGuiDir(dir3, resourceDir);
            }
        }
        Config modConfig = builtMod.getConfig();
        if (modConfig.getBool("enabled")) {
            String resourcePacksDir = builtMod.buildConfig.defaultConfig.resourcePacksDir;
            if (resourcePacksDir != null) {
                File resourcePacksDirFile = new File(dir3, resourcePacksDir);
                if (resourcePacksDirFile.isDirectory()) {
                    for (File directory : resourcePacksDirFile.listFiles()) {
                        if (directory.isDirectory() && new File(directory, "manifest.json").isFile()) {
                            ModLoader.addMinecraftResourcePack(directory);
                        }
                    }
                }
            }
            String behaviorPacksDir = builtMod.buildConfig.defaultConfig.behaviorPacksDir;
            if (behaviorPacksDir != null) {
                File behaviorPacksDirFile = new File(dir3, behaviorPacksDir);
                if (behaviorPacksDirFile.isDirectory()) {
                    File[] listFiles2 = behaviorPacksDirFile.listFiles();
                    int length = listFiles2.length;
                    int i3 = 0;
                    while (i3 < length) {
                        File directory2 = listFiles2[i3];
                        if (directory2.isDirectory()) {
                            dir2 = dir3;
                            if (new File(directory2, "manifest.json").isFile()) {
                                ModLoader.addMinecraftBehaviorPack(directory2);
                            }
                        } else {
                            dir2 = dir3;
                        }
                        i3++;
                        dir3 = dir2;
                    }
                }
            }
        }
        CompiledSources compiledSources = builtMod.createCompiledSources();
        ArrayList<BuildConfig.Source> sourcesToCompile = builtMod.buildConfig.getAllSourcesToCompile(true);
        int i4 = 0;
        while (true) {
            int i42 = i4;
            if (i42 >= sourcesToCompile.size()) {
                return builtMod;
            }
            BuildConfig.Source source = sourcesToCompile.get(i42);
            if (source.gameVersion.isCompatible()) {
                if (source.apiInstance == null) {
                    String msg = "could not find api for " + source.path + ", maybe it is missing in build.config or name is incorrect, compilation failed.";
                    ICLog.d("INNERCORE-MOD-BUILD", msg);
                    debugInfo.putStatus(source.path, new IllegalArgumentException(msg));
                } else {
                    try {
                        Executable compiledSource = compileOrLoadExecutable(builtMod, compiledSources, source);
                        switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$SourceType()[source.sourceType.ordinal()]) {
                            case 1:
                                builtMod.compiledPreloaderScripts.add(compiledSource);
                                break;
                            case 2:
                                builtMod.compiledLauncherScripts.add(compiledSource);
                                setupLauncherScript(compiledSource, builtMod);
                                break;
                            case 3:
                                builtMod.compiledModSources.add(compiledSource);
                                break;
                            case 4:
                                builtMod.compiledCustomSources.put(source.path, compiledSource);
                                break;
                            case 5:
                                builtMod.compiledLibs.add(compiledSource);
                                break;
                        }
                        builtMod.onImportExecutable(compiledSource);
                        debugInfo.putStatus(source.path, compiledSource);
                    } catch (Exception e) {
                        ICLog.e("INNERCORE-MOD-BUILD", "failed to compile source " + source.path + ":", e);
                        debugInfo.putStatus(source.path, e);
                    }
                }
            }
            i4 = i42 + 1;
        }
    }

    public static AnalyzedModType analyzeModDir(String dir) {
        String dir2 = checkRedirect(dir);
        if (FileTools.exists(String.valueOf(dir2) + "/build.config")) {
            return AnalyzedModType.INNER_CORE_MOD;
        }
        if (FileTools.exists(String.valueOf(dir2) + "/main.js") && FileTools.exists(String.valueOf(dir2) + "/launcher.js") && FileTools.exists(String.valueOf(dir2) + "/config.json")) {
            FileTools.exists(String.valueOf(dir2) + "/resources.json");
        }
        return AnalyzedModType.UNKNOWN;
    }

    public static boolean analyzeAndSetupModDir(String dir) {
        AnalyzedModType analysisResult = analyzeModDir(checkRedirect(dir));
        switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$AnalyzedModType()[analysisResult.ordinal()]) {
            case 2:
            case 4:
            default:
                return true;
            case 3:
                return false;
            case 5:
                return false;
        }
    }
}
