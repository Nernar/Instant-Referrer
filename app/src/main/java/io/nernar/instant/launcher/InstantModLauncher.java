package io.nernar.instant.launcher;

import com.zhekasmirnov.horizon.util.LocaleUtils;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.log.ModLoaderEventHandler;
import com.zhekasmirnov.innercore.api.runtime.AsyncModLauncher;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.api.runtime.other.PrintStacking;
import com.zhekasmirnov.innercore.mod.executable.library.LibraryRegistry;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.ui.ModLoadingOverlay;
import com.zhekasmirnov.innercore.utils.UIUtils;
import com.zhekasmirnov.mcpe161.InnerCore;
import java.lang.reflect.InvocationTargetException;

public class InstantModLauncher extends AsyncModLauncher {
	
	public void launchInstantModsInThread() {
		final ModLoadingOverlay overlay = new ModLoadingOverlay(UIUtils.getContext());
		overlay.await(500);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(10);
				long start = System.currentTimeMillis();
				launchInstantModsInCurrentThread();
				overlay.close();
				ICLog.i("LOADING", "instant mods launched in " + (System.currentTimeMillis() - start) + "ms");
			}
		}).start();
	}
	
	public void launchInstantModsInCurrentThread() {
		try {
			Class ModPackContext = Class.forName("com.zhekasmirnov.innercore.modpack.ModPackContext");
			Object instance = ModPackContext.getMethod("getInstance").invoke(null);
			instance.getClass().getMethod("assurePackSelected").invoke(instance);
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			// Ignore in legacy Inner Core version
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		LoadingUI.setTextAndProgressBar("Preparing...", 0.65f);
		PrintStacking.prepare();
		ICLog.setupEventHandlerForCurrentThread(new ModLoaderEventHandler());
		try {
			NameTranslation.refresh(false);
		} catch (RuntimeException any) {
			NameTranslation.setLanguage(LocaleUtils.getLanguage(null));
		}
		LoadingStage.setStage(6);
		try {
			LibraryRegistry.class.getMethod("loadAllBuiltInLibraries").invoke(null);
		} catch (NoSuchMethodException e) {
			// Ignore in legacy Inner Core version
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		LibraryRegistry.prepareAllLibraries();
		LoadingUI.setTextAndProgressBar("Running Mods...", 0.5f);
		try {
			InstantModLoader.runInstantModsViaNewModLoader();
		} catch (UnsupportedOperationException e) {
			InstantModLoader.instance.startMods();
		}
		LoadingUI.setTextAndProgressBar("Post Initialization...", 1.0f);
		invokeInstantPostLoadedCallbacks();
		InnerCore.getInstance().onFinalLoadComplete();
		ICLog.flush();
	}
	
	private static void invokeInstantPostLoadedCallbacks() {
		Callback.invokeAPICallback("CorePreconfigured", InnerCoreConfig.config);
		Callback.invokeAPICallback("InstantLoaded", new Object[0]);
	}
}
