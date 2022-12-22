package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.apparatus.Apparatus;
import com.zhekasmirnov.apparatus.minecraft.version.VanillaIdConversionMap;
import com.zhekasmirnov.apparatus.mod.ContentIdSource;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.log.ModLoaderEventHandler;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.api.runtime.other.PrintStacking;
import com.zhekasmirnov.innercore.api.unlimited.BlockRegistry;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.library.LibraryRegistry;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.mcpe161.InnerCore;
import java.io.InputStreamReader;
import java.io.Reader;

public class AsyncModLauncher {
    public void launchModsInThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(10);
                long start = System.currentTimeMillis();
                AsyncModLauncher.this.launchModsInCurrentThread();
                ICLog.i("LOADING", "mods launched in " + (System.currentTimeMillis() - start) + "ms");
            }
        }).start();
    }

    public void launchModsInCurrentThread() {
        Apparatus.loadClasses();
        ModPackContext.getInstance().assurePackSelected();
        LoadingUI.setTextAndProgressBar("Preparing...", 0.65f);
        PrintStacking.prepare();
        NameTranslation.refresh(false);
        loadAllMenuScripts();
        ICLog.setupEventHandlerForCurrentThread(new ModLoaderEventHandler());
        LoadingStage.setStage(6);
        VanillaIdConversionMap.getSingleton().reloadFromAssets();
        BlockRegistry.onInit();
        LibraryRegistry.loadAllBuiltInLibraries();
        LibraryRegistry.prepareAllLibraries();
        // LoadingUI.setTextAndProgressBar("Running Core Engine...", 0.4f);
        // CoreEngineAPI.getOrLoadCoreEngine();
        LoadingUI.setTextAndProgressBar("Running Mods...", 0.5f);
        ModLoader.runModsViaNewModLoader();
        LoadingUI.setTextAndProgressBar("Defining Blocks...", 1.0f);
        BlockRegistry.onModsLoaded();
        LoadingUI.setTextAndProgressBar("Post Initialization...", 1.0f);
        invokePostLoadedCallbacks();
        ContentIdSource.getGlobal().save();
        InnerCore.getInstance().onFinalLoadComplete();
        ICLog.flush();
    }

    private static void invokePostLoadedCallbacks() {
        Callback.invokeAPICallback("CoreConfigured", InnerCoreConfig.config);
        Callback.invokeAPICallback("PreLoaded", new Object[0]);
        Callback.invokeAPICallback("APILoaded", new Object[0]);
        Callback.invokeAPICallback("ModsLoaded", new Object[0]);
        Callback.invokeAPICallback("PostLoaded", new Object[0]);
    }

    private static void loadAllMenuScripts() {
        loadMenuScript("innercore/scripts/workbench", "screen_workbench");
    }

    private static void loadMenuScript(String asset, String name) {
        CompilerConfig cfg = new CompilerConfig(API.getInstanceByName("PrefsWinAPI"));
        cfg.setName(name);
        cfg.setOptimizationLevel(-1);
        try {
            Reader input = new InputStreamReader(FileTools.getAssetInputStream(String.valueOf(asset) + ".js"));
            Compiler.compileReader(input, cfg).run();
        } catch (Exception e) {
            ICLog.e("ERROR", "failed to load script " + name, e);
        }
    }
}
