package io.nernar.instant.launcher;

import android.app.Activity;
import io.nernar.instant.referrer.InstantReferrer;
import com.zhekasmirnov.apparatus.adapter.innercore.PackInfo;
import com.zhekasmirnov.horizon.launcher.ads.AdsManager;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.log.ModLoaderEventHandler;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.PreferencesWindowAPI;
import com.zhekasmirnov.innercore.api.mod.preloader.PreloaderAPI;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.ColorsPatch;
import com.zhekasmirnov.innercore.utils.ReflectionPatch;
import com.zhekasmirnov.innercore.utils.UIUtils;
import com.zhekasmirnov.mcpe161.InnerCore;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InstantInnerCore extends InnerCore {
	protected boolean completedInstant = false;
	
	public InstantInnerCore(Activity context, Pack pack) {
		super(context, pack);
	}
	
	public void preload() {
		ReflectionPatch.init();
		ColorsPatch.init();
		try {
			Method registerInstance = API.class.getDeclaredMethod("registerInstance", API.class);
			registerInstance.setAccessible(true);
			registerInstance.invoke(null, new AdaptedScriptAPI());
			registerInstance.invoke(null, new PreferencesWindowAPI());
			registerInstance.invoke(null, new PreloaderAPI());
			initiatePreloading();
		} catch (NoSuchMethodException e) {
			throw new UnsupportedOperationException(e);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initiatePreloading() {
		Logger.debug("INNERCORE", String.format("Inner Core %s Preloading", PackInfo.getPackVersionName()));
		LoadingStage.setStage(1);
		preloadInnerCore();
	}
	
	private void preloadInnerCore() {
		try {
			Class ModPackContext = Class.forName("com.zhekasmirnov.innercore.modpack.ModPackContext");
			Object instance = ModPackContext.getMethod("getInstance").invoke(null);
			instance.getClass().getMethod("assurePackSelected").invoke(instance);
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			// Ignore in legacy Inner Core version
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		ICLog.setupEventHandlerForCurrentThread(new ModLoaderEventHandler());
		LoadingUI.setTextAndProgressBar("Initializing Resources...", 0.0f);
		LoadingStage.setStage(2);
		UIUtils.initialize(getCurrentActivity());
		LoadingUI.setTextAndProgressBar("Loading Mods...", 0.15f);
		InstantModLoader.initialize();
		try {
			InstantModLoader.class.getMethod("loadModsAndSetupEnvViaNewModLoader").invoke(null);
		} catch (NoSuchMethodException e) {
			InstantModLoader.instance.loadMods();
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		LoadingUI.setTextAndProgressBar("Referring Instant...", 0.5f);
		Logger.debug("INSTANT-API", "Instant Referrer " + InstantReferrer.getVersionName());
		completedInstant = true;
	}
	
	public void launchInstant() {
		UIUtils.initialize(UIUtils.getContext());
		if (getCurrentActivity().getPackageName().equals("com.zheka.horizon")) {
			AdsManager.getInstance().closeAllRequests();
			AdsManager.getInstance().closeInterstitialAds();
		}
		InstantModLauncher modLauncher = new InstantModLauncher();
		if (InnerCoreConfig.getBool("disable_loading_screen")) {
			modLauncher.launchInstantModsInCurrentThread();
		} else {
			modLauncher.launchInstantModsInThread();
		}
	}
	
	public static InstantInnerCore getInstance() {
		InnerCore core = InnerCore.getInstance();
		if (core instanceof InstantInnerCore) {
			return (InstantInnerCore) core;
		}
		return null;
	}
	
	public static boolean isCompletedInstant() {
		InstantInnerCore instance = getInstance();
		return instance != null ? instance.completedInstant : false;
	}
}
