package com.zhekasmirnov.innercore.mod.build;

import com.mojang.minecraftpe.MainActivity;
import com.nernar.instant.launcher.InstantModLoader;
import com.nernar.instant.launcher.InstantableMod;
import com.nernar.instant.referrer.InstantReferrer;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import com.zhekasmirnov.innercore.api.mod.ui.TextureSource;
import com.zhekasmirnov.innercore.mod.build.enums.AnalyzedModType;
import com.zhekasmirnov.innercore.mod.build.enums.BuildType;
import com.zhekasmirnov.innercore.mod.build.enums.ResourceDirType;
import com.zhekasmirnov.innercore.mod.build.enums.SourceType;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.mcpe161.EnvironmentSetup;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;

public class ModBuilder {
    public static final String LOGGER_TAG = "INNERCORE-MOD-BUILD";
	
    public static BuildConfig loadBuildConfigForDir(String dir) {
        BuildConfig config = new BuildConfig(new File(dir + "/build.config"));
        config.read();
		for (BuildConfig.Source source : config.sourcesToCompile) {
			boolean force = source.json != null ? source.json.has("api") : false;
			if (!force && source.sourceType.toString() == SourceType.INSTANT.toString()) {
                source.apiInstance = API.getInstanceByName("Instant");
			}
		}
        return config;
    }
	
    public static void addGuiDir(String dir, BuildConfig.ResourceDir resourceDir) {
        String path = dir + resourceDir.path;
        if (!FileTools.exists(path)) {
            ICLog.d(LOGGER_TAG, "failed to import resource or ui dir " + resourceDir.path + ": it does not exist");
            return;
        }
        TextureSource.instance.loadDirectory(new File(path));
    }
	
    public static String checkRedirect(String dir) {
        File redirectFile = new File(dir + ".redirect");
        if (redirectFile.exists()) {
            try {
                return FileTools.readFileText(redirectFile.getAbsolutePath()).trim();
            } catch (IOException e) {}
        }
        return dir;
    }
	
    private static class LauncherScope extends ScriptableObject {
		Mod mod;
		
        public LauncherScope(Mod mod2) {
            this.mod = mod2;
        }
		
        @JSFunction
        public void Launch(ScriptableObject scope) {
            this.mod.RunMod(scope);
        }
		
        @JSFunction
        public void ConfigureMultiplayer(ScriptableObject props) {
            ScriptableObjectWrapper wrapper = new ScriptableObjectWrapper((Scriptable) props);
            this.mod.configureMultiplayer(wrapper.getString("name"), wrapper.getString("version"), wrapper.getBoolean("isClientOnly") || wrapper.getBoolean("isClientSide"));
        }
		
        public String getClassName() {
            return "LauncherAPI";
        }
    }
	
    private static void setupLauncherScript(Executable launcherScript, Mod mod) {
        LauncherScope scope = new LauncherScope(mod);
        scope.defineFunctionProperties(new String[] { "Launch", "ConfigureMultiplayer" }, scope.getClass(), 2);
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
            ICLog.d(LOGGER_TAG, "no multidex executable created for " + source.path);
        }
        Reader sourceReader = new FileReader(new File(mod.dir + source.path));
        compilerConfig.setOptimizationLevel(-1);
        return Compiler.compileReader(sourceReader, compilerConfig);
    }
	
    public static Mod buildModForDir(String dir, ModPack modPack, String locationName) {
        dir = checkRedirect(dir);
        if (!FileTools.exists(dir)) {
            ICLog.d(LOGGER_TAG, "failed to load mod, dir does not exist, maybe redirect file is pointing to the missing dir " + dir);
            return null;
        }
        InstantableMod builtMod = new InstantableMod(dir);
		try {
			Method setModPackAndLocation = builtMod.getClass().getMethod("setModPackAndLocation", ModPack.class, String.class);
			setModPackAndLocation.invoke(builtMod, modPack, locationName);
		} catch (NoSuchMethodException e) {
			// Ignore in legacy Inner Core version
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
        builtMod.buildConfig = loadBuildConfigForDir(dir);
        if (!builtMod.buildConfig.isValid()) {
            return null;
        }
        builtMod.buildConfig.save();
        ModDebugInfo debugInfo = builtMod.getDebugInfo();
        if (builtMod.buildConfig.getBuildType() == BuildType.DEVELOP) {
            ArrayList<BuildConfig.BuildableDir> buildableDirs = builtMod.buildConfig.buildableDirs;
            for (int i = 0; i < buildableDirs.size(); i++) {
                BuildHelper.buildDir(dir, buildableDirs.get(i));
            }
        }
        ArrayList<BuildConfig.ResourceDir> resourceDirs = builtMod.buildConfig.resourceDirs;
        for (int i = 0; i < resourceDirs.size(); i++) {
            BuildConfig.ResourceDir resourceDir = resourceDirs.get(i);
            if (resourceDir.resourceType == ResourceDirType.GUI) {
                addGuiDir(dir, resourceDir);
            }
        }
		Config config = null;
		try {
			Method getConfig = builtMod.getClass().getDeclaredMethod("getConfig");
			getConfig.setAccessible(true);
			config = (Config) getConfig.invoke(builtMod);
		} catch (NoSuchMethodException e) {
			File modConfigFile = new File(dir, "config.json");
			Config modConfig = new Config(modConfigFile);
			if (modConfigFile.isFile()) config = modConfig;
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
        if (config == null || config.getBool("enabled")) {
            String resourcePacksDir = builtMod.buildConfig.defaultConfig.resourcePacksDir;
            if (resourcePacksDir != null) {
                File resourcePacksDirFile = new File(dir, resourcePacksDir);
                if (resourcePacksDirFile.isDirectory()) {
                    File[] listFiles = resourcePacksDirFile.listFiles();
                    for (File directory : listFiles) {
                        if (directory.isDirectory() && new File(directory, "manifest.json").isFile()) {
                            InstantModLoader.addMinecraftResourcePack(directory);
                        }
                    }
                }
            }
            String behaviorPacksDir = builtMod.buildConfig.defaultConfig.behaviorPacksDir;
            if (behaviorPacksDir != null) {
                File behaviorPacksDirFile = new File(dir, behaviorPacksDir);
                if (behaviorPacksDirFile.isDirectory()) {
                    File[] listFiles = behaviorPacksDirFile.listFiles();
					String behavior;
                    int i = 0;
                    while (i < listFiles.length) {
                        File directory = listFiles[i];
                        if (directory.isDirectory()) {
                            behavior = dir;
                            if (new File(directory, "manifest.json").isFile()) {
                                InstantModLoader.addMinecraftBehaviorPack(directory);
                            }
                        } else {
                            behavior = dir;
                        }
                        i++;
                        dir = behavior;
                    }
                }
            }
        }
        CompiledSources compiledSources = builtMod.createCompiledSources();
        ArrayList<BuildConfig.Source> sourcesToCompile = null;
		try {
			Method getAllSourcesToCompile = builtMod.buildConfig.getClass().getMethod("getAllSourcesToCompile", Boolean.TYPE);
			sourcesToCompile = (ArrayList<BuildConfig.Source>) getAllSourcesToCompile.invoke(builtMod.buildConfig, true);
		} catch (NoSuchMethodException e) {
			sourcesToCompile = builtMod.buildConfig.getAllSourcesToCompile();
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
        for (int i = 0; i < sourcesToCompile.size(); i++) {
            BuildConfig.Source source = sourcesToCompile.get(i);
			try {
				Field gameVersionField = source.getClass().getField("gameVersion");
				Object gameVersion = gameVersionField.get(source);
				Method isCompatible = gameVersion.getClass().getMethod("isCompatible");
				Boolean compatible = (Boolean) isCompatible.invoke(gameVersion);
				if (!compatible.booleanValue()) continue;
			} catch (NoSuchFieldException e) {
				// Ignore in legacy Inner Core version
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
            if (source.apiInstance == null) {
				String msg = "could not find api for " + source.path + ", maybe it is missing in build.config or name is incorrect, compilation failed.";
				ICLog.d(LOGGER_TAG, msg);
				debugInfo.putStatus(source.path, new IllegalArgumentException(msg));
			} else {
				try {
					Executable compiledSource = compileOrLoadExecutable(builtMod, compiledSources, source);
					switch (source.sourceType.toString()) {
						case "preloader":
							builtMod.compiledPreloaderScripts.add(compiledSource);
							break;
						case "launcher":
							builtMod.compiledLauncherScripts.add(compiledSource);
							setupLauncherScript(compiledSource, builtMod);
							break;
						case "library":
							builtMod.compiledLibs.add(compiledSource);
							break;
						case "mod":
							builtMod.compiledModSources.add(compiledSource);
							break;
						case "custom":
							builtMod.compiledCustomSources.put(source.path, compiledSource);
							break;
						case "instant":
							builtMod.compiledInstantSources.add(compiledSource);
							break;
					}
					builtMod.onImportExecutable(compiledSource);
					debugInfo.putStatus(source.path, compiledSource);
				} catch (Exception e) {
					ICLog.e(LOGGER_TAG, "failed to compile source " + source.path + ":", e);
					debugInfo.putStatus(source.path, e);
                }
            }
        }
        return builtMod;
    }
	
	@Deprecated
	public static Mod buildModForDir(String dir) {
		return buildModForDir(dir, null, null);
	}
	
    public static AnalyzedModType analyzeModDir(String dir) {
        dir = checkRedirect(dir);
        if (FileTools.exists(dir + "/build.config")) {
			if (FileTools.exists(dir + "/mod.info")) {
				try {
					JSONObject json = FileTools.readJSON(dir + "/mod.info");
					if (json.optBoolean("instantLaunch", false)) {
						return AnalyzedModType.INSTANT_INNER_CORE_MOD;
					}
				} catch (IOException | JSONException e) {}
			}
            return AnalyzedModType.INNER_CORE_MOD;
        }
        if (FileTools.exists(dir + "/main.js")) {
            if (FileTools.exists(dir + "/launcher.js")) {
                if (FileTools.exists(dir + "/config.json")) {
                    FileTools.exists(dir + "/resources.json");
                }
            }
        }
        return AnalyzedModType.UNKNOWN;
    }
	
    public static boolean analyzeAndSetupModDir(String dir) {
		switch (analyzeModDir(checkRedirect(dir))) {
			case INNER_CORE_MOD:
            case CORE_ENGINE_MOD:
				return EnvironmentSetup.getCurrentActivity() instanceof MainActivity;
			case INSTANT_INNER_CORE_MOD:
				if (EnvironmentSetup.getCurrentActivity() instanceof MainActivity) {
					return false;
				}
				return InstantReferrer.inInstantDistribution();
            default:
                return false;
        }
    }
}
