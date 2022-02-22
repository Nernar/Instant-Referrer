package com.nernar.instant.referrer;

import android.app.Activity;
import com.nernar.instant.Launch;
import com.nernar.instant.launcher.InstantInnerCore;
import com.nernar.instant.prebuilt.InstantConfigSource;
import com.zhekasmirnov.horizon.HorizonApplication;
import com.zhekasmirnov.horizon.activity.main.HorizonActivity;
import com.zhekasmirnov.horizon.activity.main.PackSelectorActivity;
import com.zhekasmirnov.horizon.activity.main.adapter.MenuHolder;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.launcher.pack.PackHolder;
import com.zhekasmirnov.horizon.modloader.repo.ModList;
import com.zhekasmirnov.horizon.runtime.task.TaskManager;
import com.zhekasmirnov.horizon.runtime.task.TaskSequence;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

public class InstantReferrer {
	public static final boolean HAD_LAUNCH_BUTTON = true;
	
	public static <T extends Activity> T findSingletonActivity(Class<?> type) {
		List<Activity> stack = HorizonApplication.getActivityStack();
		synchronized (stack) {
			for (Activity activity : stack) {
				if (activity != null && activity.getClass().equals(type)) {
					return (T) activity;
				}
			}
		}
		return null;
	}
	
	public static <T extends Activity> T findRunningActivity(Class<?> type) {
		HashSet<Activity> stack = HorizonApplication.getActivitiesOnTop();
		synchronized (stack) {
			for (Activity activity : stack) {
				if (activity != null && activity.getClass().equals(type)) {
					return (T) activity;
				}
			}
		}
		return null;
	}
	
	public static class Horizon {
		private final HorizonActivity singleton;
		
		protected Horizon(HorizonActivity activity) {
			this.singleton = activity;
		}
		
		public static Horizon get(HorizonActivity activity) {
			return activity != null ? new Horizon(activity) : null;
		}
		
		public static Horizon get() {
			return get((HorizonActivity) findSingletonActivity(HorizonActivity.class));
		}
		
		public HorizonActivity getSingleton() {
			return singleton;
		}
		
		public PackHolder getPackHolder() {
			try {
				Field deepIntoReflection = singleton.getClass().getDeclaredField("packHolder");
				deepIntoReflection.setAccessible(true);
				return (PackHolder) deepIntoReflection.get(singleton);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return null;
			}
		}
		
		public MenuHolder getMenuHolder() {
			try {
				Field deepIntoReflection = singleton.getClass().getDeclaredField("menuHolder");
				deepIntoReflection.setAccessible(true);
				return (MenuHolder) deepIntoReflection.get(singleton);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return null;
			}
		}
		
		public boolean isLaunching() {
			try {
				Field deepIntoReflection = singleton.getClass().getDeclaredField("isLaunching");
				deepIntoReflection.setAccessible(true);
				return (boolean) deepIntoReflection.get(singleton);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return false;
			}
		}
		
		public boolean launchPack() {
			try {
				Method deepIntoReflection = singleton.getClass().getDeclaredMethod("launchPack");
				deepIntoReflection.setAccessible(true);
				deepIntoReflection.invoke(singleton);
				return true;
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				return false;
			}
		}
	}
	
	public static class PackSelector {
		private final PackSelectorActivity singleton;
		
		protected PackSelector(PackSelectorActivity activity) {
			this.singleton = activity;
		}
		
		public static PackSelector get(PackSelectorActivity activity) {
			return activity != null ? new PackSelector(activity) : null;
		}
		
		public static PackSelector get() {
			return get((PackSelectorActivity) findSingletonActivity(PackSelectorActivity.class));
		}
		
		public PackSelectorActivity getSingleton() {
			return singleton;
		}
		
		public PackHolder getPendingPackHolder() {
			try {
				Field deepIntoReflection = singleton.getClass().getDeclaredField("currentSelectingPackHolder");
				deepIntoReflection.setAccessible(true);
				return (PackHolder) deepIntoReflection.get(singleton);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return null;
			}
		}
	}
	
	public static class Repository {
		private final ModList singleton;
		
		protected Repository(ModList modList) {
			this.singleton = modList;
		}
		
		public static Repository get(ModList modList) {
			return modList != null ? new Repository(modList) : null;
		}
		
		public static Repository get(Pack pack) {
			return get(pack != null ? pack.getModList() : null);
		}
		
		public TaskSequence getRebuildSequence() {
			try {
				Field deepIntoReflection = singleton.getClass().getDeclaredField("REBUILD_SEQUENCE");
				deepIntoReflection.setAccessible(true);
				return (TaskSequence) deepIntoReflection.get(singleton);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return null;
			}
		}
		
		public TaskSequence getLaunchSequence() {
			try {
				Field deepIntoReflection = singleton.getClass().getDeclaredField("LAUNCH_SEQUENCE");
				deepIntoReflection.setAccessible(true);
				return (TaskSequence) deepIntoReflection.get(singleton);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return null;
			}
		}
		
		public TaskSequence getRefreshSequence() {
			try {
				Field deepIntoReflection = singleton.getClass().getDeclaredField("REFRESH_SEQUENCE");
				deepIntoReflection.setAccessible(true);
				return (TaskSequence) deepIntoReflection.get(singleton);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return null;
			}
		}
		
		public Runnable getInterruptRunnable() {
			try {
				Field deepIntoReflection = singleton.getClass().getDeclaredField("interrupt");
				deepIntoReflection.setAccessible(true);
				return (Runnable) deepIntoReflection.get(singleton);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return null;
			}
		}
		
		public boolean interruptTaskSequenceSafety(TaskManager manager, TaskSequence sequence) {
			try {
				manager.interruptTaskSequence(sequence);
				return true;
			} catch (NullPointerException e) {
				return false;
			}
		}
		
		public void abortLaunch(TaskManager manager) {
			TaskSequence rebuild = getRebuildSequence();
			interruptTaskSequenceSafety(manager, rebuild);
			TaskSequence launch = getLaunchSequence();
			interruptTaskSequenceSafety(manager, launch);
			TaskSequence refresh = getRefreshSequence();
			interruptTaskSequenceSafety(manager, refresh);
			Runnable interrupt = getInterruptRunnable();
			if (interrupt != null) interrupt.run();
		}
		
		public void abortLaunch(Pack pack) {
			abortLaunch(pack.getContextHolder().getTaskManager());
		}
	}
	
	public static String getVersionName() {
		return "v1.1";
	}
	
	public static boolean hadInformativeProgress() {
		return InstantConfig.get("environment.informative_progress", InstantConfigSource.Environment.INFORMATIVE_PROGRESS);
	}
	
	public static boolean hadImmersiveMode() {
		return InstantConfig.get("environment.immersive_mode", InstantConfigSource.Environment.IMMERSIVE_MODE);
	}
	
	public static boolean hadAutoLaunchEnabled() {
		if (!HAD_LAUNCH_BUTTON) return !HAD_LAUNCH_BUTTON;
		return InstantConfig.get("environment.auto_launch", InstantConfigSource.Environment.AUTO_LAUNCH);
	}
	
	public static boolean hadAutoLaunchOverride() {
		if (!HAD_LAUNCH_BUTTON) return !HAD_LAUNCH_BUTTON;
		return InstantConfig.get("environment.auto_launch_override", InstantConfigSource.Environment.AUTO_LAUNCH_OVERRIDE);
	}
	
	public static void setAutoLaunchEnabled(boolean enabled) {
		InstantConfig.setAndSave("environment.auto_launch", enabled);
	}
	
	public static boolean hadAbortAbility() {
		return InstantConfig.get("environment.abort_ability", InstantConfigSource.Environment.ABORT_ABILITY);
	}
	
	public static boolean hadShuffledBackground() {
		return InstantConfig.get("background.shuffle_art", InstantConfigSource.Background.SHUFFLE_ART);
	}
	
	public static int getBackgroundDuration() {
		return InstantConfig.get("background.frame_duratuon", InstantConfigSource.Background.FRAME_DURATUON) * 100;
	}
	
	public static boolean hadBackgroundInterpolation() {
		return InstantConfig.get("background.smooth_movement", InstantConfigSource.Background.SMOOTH_MOVEMENT);
	}
	
	public static boolean hadFullscreenBackground() {
		return InstantConfig.get("background.force_fullscreen", InstantConfigSource.Background.FORCE_FULLSCREEN);
	}
	
	public static double getBackgroundBrightness() {
		return InstantConfig.get("background.brightness", InstantConfigSource.Background.BRIGHTNESS);
	}
	
	public static boolean hadMenuChangedGravity() {
		return InstantConfig.get("recycler.measure_to_bottom", InstantConfigSource.Recycler.MEASURE_TO_BOTTOM);
	}
	
	public static double getMenuWidthModifier() {
		return InstantConfig.get("recycler.width_modifier", InstantConfigSource.Recycler.WIDTH_MODIFIER);
	}
	
	public static boolean hadMenuCardPadding() {
		return InstantConfig.get("recycler.card_padding", InstantConfigSource.Recycler.CARD_PADDING);
	}
	
	public static int getMenuCardRadiusModifier() {
		return InstantConfig.get("recycler.card_radius_modifier", InstantConfigSource.Recycler.CARD_RADIUS_MODIFIER);
	}
	
	public static boolean hadMinecraftActually() {
        return InstantConfig.get("distribution.had_minecraft", InstantConfigSource.Distribution.HAD_MINECRAFT);
	}
	
	public static boolean hadLicenseWarning() {
		return !InstantConfig.get("distribution.dismiss_warning", InstantConfigSource.Distribution.DISMISS_WARNING);
	}
	
	public static boolean inSupportDistribution() {
		return InstantConfig.get("advertisement.support_modification", InstantConfigSource.Advertisement.SUPPORT_MODIFICATION);
	}
	
	public static boolean isAnySupportAcquired() {
		return InstantConfig.get("advertisement.block_everything", InstantConfigSource.Advertisement.BLOCK_EVERYTHING);
	}
	
	public static InstantInnerCore getInstantInnerCoreInstance() {
		InstantInnerCore instance = InstantInnerCore.getInstance();
		if (instance == null) {
			PackSelector selector = PackSelector.get();
			Pack pendingSelectedPack = Launch.pendingInstantPack;
			if (pendingSelectedPack != null) Launch.pendingInstantPack = null;
			else {
				PackHolder holder = selector.getPendingPackHolder();
				if (holder == null) throw new UnsupportedOperationException();
				pendingSelectedPack = holder.getPack();
			}
			new InstantInnerCore(selector.getSingleton(), pendingSelectedPack);
		}
		return InstantInnerCore.getInstance();
	}
	
	public static boolean inInstantDistribution() {
		return true;
	}
}
