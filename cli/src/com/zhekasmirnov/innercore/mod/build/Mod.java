package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.apparatus.minecraft.enums.EnumsJsInjector;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.mod.build.enums.BuildType;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.mod.executable.library.Library;
import com.zhekasmirnov.innercore.mod.executable.library.LibraryRegistry;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Mod {
    private static int guiIconCounter = 0;
    public BuildConfig buildConfig;
    public Config config;
    public String dir;
    private ModPack modPack;
    private String modPackLocationName;
    private ModDebugInfo debugInfo = new ModDebugInfo();
    public ArrayList<Executable> compiledLibs = new ArrayList<>();
    public ArrayList<Executable> compiledModSources = new ArrayList<>();
    public ArrayList<Executable> compiledLauncherScripts = new ArrayList<>();
    public ArrayList<Executable> compiledPreloaderScripts = new ArrayList<>();
    public HashMap<String, Executable> compiledCustomSources = new HashMap<>();
    private boolean isConfiguredForMultiplayer = false;
    private boolean isClientOnly = false;
    private String multiplayerName = null;
    private String multiplayerVersion = null;
    public boolean isEnabled = false;
    private String guiIconName = "missing_mod_icon";
    private JSONObject modInfoJson = new JSONObject();
    private boolean isPreloaderRunning = false;
    private boolean isLauncherRunning = false;
    public boolean isModRunning = false;

    public void setModPackAndLocation(ModPack modPack, String modPackLocationName) {
        this.modPack = modPack;
        this.modPackLocationName = modPackLocationName;
    }

    public ModPack getModPack() {
        return this.modPack;
    }

    public String getModPackLocationName() {
        return this.modPackLocationName;
    }

    private void importConfigIfNeeded() {
        if (this.config == null) {
            this.config = new Config(this.modPack.getRequestHandler(ModPackDirectory.DirectoryType.CONFIG).get(this.modPackLocationName, "config.json"));
            try {
                this.config.checkAndRestore("{\"enabled\":true}");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.isEnabled = ((Boolean) this.config.get("enabled")).booleanValue();
    }

    public Config getConfig() {
        importConfigIfNeeded();
        return this.config;
    }

    public Mod(String dir) {
        this.dir = dir;
    }

    public CompiledSources createCompiledSources() {
        return new CompiledSources(new File(this.dir, ".dex"));
    }

    public ModDebugInfo getDebugInfo() {
        return this.debugInfo;
    }

    public void onImportExecutable(Executable exec) {
        importConfigIfNeeded();
        ScriptableObject additionalScope = ScriptableObjectHelper.createEmpty();
        additionalScope.put("__version__", additionalScope, Integer.valueOf(MinecraftVersions.getCurrent().getCode()));
        additionalScope.put("__mod__", additionalScope, this);
        additionalScope.put("__name__", additionalScope, getName());
        additionalScope.put("__dir__", additionalScope, this.dir);
        additionalScope.put("__config__", additionalScope, Context.javaToJS(this.config, exec.getScope()));
        additionalScope.put("__debug_typecheck__", additionalScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                ICLog.d("DEBUG", "checked object type: obj=" + objects[0] + " class=" + objects[0].getClass());
                return new StringBuilder().append(objects[0].getClass()).toString();
            }
        });
        new EnumsJsInjector(additionalScope, true).injectAllEnumScopes("E");
        additionalScope.put("runCustomSource", additionalScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                String path = (String) params[0];
                ScriptableObject additionalScope2 = null;
                if (params.length > 1 && (params[1] instanceof ScriptableObject)) {
                    additionalScope2 = (ScriptableObject) params[1];
                }
                Mod.this.runCustomSource(path, additionalScope2);
                return null;
            }
        });
        exec.addToScope(additionalScope);
        exec.setParentMod(this);
        if (exec instanceof Library) {
            LibraryRegistry.addLibrary((Library) exec);
        }
    }

    public void onImport() {
        API apiInstance;
        this.isEnabled = true;
        importConfigIfNeeded();
        if (this.isEnabled && (apiInstance = this.buildConfig.getDefaultAPI()) != null) {
            apiInstance.onModLoaded(this);
        }
        if (FileTools.exists(String.valueOf(this.dir) + "mod_icon.png")) {
            StringBuilder sb = new StringBuilder();
            sb.append("_modIcon");
            int i = guiIconCounter;
            guiIconCounter = i + 1;
            sb.append(i);
            this.guiIconName = sb.toString();
        }
        if (FileTools.exists(String.valueOf(this.dir) + "mod.info")) {
            try {
                this.modInfoJson = FileTools.readJSON(String.valueOf(this.dir) + "mod.info");
            } catch (IOException | JSONException e) {
            }
        }
    }

    public BuildType getBuildType() {
        return this.buildConfig.getBuildType();
    }

    public void setBuildType(BuildType buildType) {
        this.buildConfig.defaultConfig.setBuildType(buildType);
        this.buildConfig.save();
    }

    public void setBuildType(String strType) {
        setBuildType(BuildType.fromString(strType));
    }

    public String getGuiIcon() {
        return this.guiIconName;
    }

    public String getName() {
        Object infoName = getInfoProperty("name");
        return infoName != null ? infoName.toString() : this.buildConfig.getName();
    }

    public String getVersion() {
        Object version = getInfoProperty("version");
        return version != null ? version.toString() : this.buildConfig.getName();
    }

    public boolean isClientOnly() {
        return this.isClientOnly;
    }

    public boolean isConfiguredForMultiplayer() {
        return this.isConfiguredForMultiplayer;
    }

    public String getMultiplayerName() {
        return this.multiplayerName != null ? this.multiplayerName : getName();
    }

    public String getMultiplayerVersion() {
        return this.multiplayerVersion != null ? this.multiplayerVersion : getVersion();
    }

    public String getFormattedAPIName() {
        API apiInstance = this.buildConfig.defaultConfig.apiInstance;
        if (apiInstance != null) {
            return apiInstance.getCurrentAPIName();
        }
        return "???";
    }

    public Object getInfoProperty(String name) {
        if (this.modInfoJson != null) {
            return this.modInfoJson.opt(name);
        }
        return null;
    }

    public ArrayList<Executable> getAllExecutables() {
        ArrayList<Executable> all = new ArrayList<>();
        all.addAll(this.compiledModSources);
        all.addAll(this.compiledLibs);
        all.addAll(this.compiledLauncherScripts);
        all.addAll(this.compiledPreloaderScripts);
        return all;
    }

    public void RunPreloaderScripts() {
        if (!this.isEnabled) {
            return;
        }
        if (this.isPreloaderRunning) {
            throw new RuntimeException("mod " + this + " is already running preloader scripts.");
        }
        this.isPreloaderRunning = true;
        for (int i = 0; i < this.compiledPreloaderScripts.size(); i++) {
            Executable preloaderScript = this.compiledPreloaderScripts.get(i);
            preloaderScript.run();
        }
    }

    public void RunLauncherScripts() {
        if (!this.isEnabled) {
            return;
        }
        if (this.isLauncherRunning) {
            throw new RuntimeException("mod " + this + " is already running launcher scripts.");
        }
        this.isLauncherRunning = true;
        for (int i = 0; i < this.compiledLauncherScripts.size(); i++) {
            Executable launcherScript = this.compiledLauncherScripts.get(i);
            launcherScript.run();
        }
    }

    public void RunMod(ScriptableObject additionalScope) {
        if (!this.isEnabled) {
            return;
        }
        if (this.isModRunning) {
            throw new RuntimeException("mod " + this + " is already running.");
        }
        this.isModRunning = true;
        for (int i = 0; i < this.compiledModSources.size(); i++) {
            Executable modSource = this.compiledModSources.get(i);
            modSource.addToScope(additionalScope);
            modSource.run();
        }
    }

    public void configureMultiplayer(String name, String version, boolean isClientOnly) {
        this.multiplayerName = name;
        if (this.multiplayerName == null || this.multiplayerName.equals("auto")) {
            this.multiplayerName = getName();
        }
        this.multiplayerVersion = version;
        if (this.multiplayerVersion == null || this.multiplayerVersion.equals("auto")) {
            this.multiplayerVersion = getVersion();
        }
        this.isConfiguredForMultiplayer = true;
        this.isClientOnly = isClientOnly;
    }

    public void runCustomSource(String name, ScriptableObject additionalScope) {
        if (this.compiledCustomSources.containsKey(name)) {
            Executable exec = this.compiledCustomSources.get(name);
            if (additionalScope != null) {
                exec.addToScope(additionalScope);
            }
            exec.reset();
            exec.run();
        }
    }
}
