package com.zhekasmirnov.mcpe161;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import com.nernar.instant.Launch;
import com.nernar.instant.launcher.InstantInnerCore;
import com.nernar.instant.prebuilt.InstantTranslation;
import com.nernar.instant.referrer.AlertDialogReferrer;
import com.nernar.instant.referrer.HorizonReferrer;
import com.nernar.instant.referrer.InstantReferrer;
import com.nernar.instant.visual.InstantActivityFactory;
import com.nernar.instant.visual.InstantLiteActivityFactory;
import com.zheka.horizon.R;
import com.zhekasmirnov.horizon.HorizonApplication;
import com.zhekasmirnov.horizon.launcher.ads.AdContainer;
import com.zhekasmirnov.horizon.launcher.ads.AdDistributionModel;
import com.zhekasmirnov.horizon.launcher.ads.AdDomain;
import com.zhekasmirnov.horizon.launcher.ads.AdsManager;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.modloader.ExecutionDirectory;
import com.zhekasmirnov.horizon.modloader.LaunchSequence;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.ResourceDirectory;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.resource.ResourceStorage;
import com.zhekasmirnov.innercore.mod.resource.types.enums.TextureType;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.mineprogramming.horizon.innercore.AboutActivityFactory;
import org.mineprogramming.horizon.innercore.ModsManagerActivityFactory;
import org.mineprogramming.horizon.innercore.inflater.JsonInflater;
import org.mineprogramming.horizon.innercore.util.DencityConverter;

public class EnvironmentSetup {
    private static TextureAtlas blockTextureAtlas;
    private static InnerCore innerCore;
    private static TextureAtlas itemTextureAtlas;
	private static boolean isPreloadedInstant;
	
	private static Runnable reportResourceSetupError(final String message, final Throwable cause, final boolean showLog) {
		return new Runnable() {
			
			@Override
			public void run() {
				if (showLog) DialogHelper.reportNonFatalError(message, cause);
				ICLog.e("InstantReferrer", message, cause);
				ICLog.flush();
			}
		};
	}
	
	private static Runnable reportResourceSetupError(Throwable cause, boolean showLog) {
		return reportResourceSetupError("Something goes wrong during startup and loading", cause, showLog);
	}
	
	private static Runnable reportResourceSetupError(String message, Throwable cause) {
		return reportResourceSetupError(message, cause, false);
	}
	
	private static Runnable reportResourceSetupError(Throwable cause) {
		return reportResourceSetupError(cause, false);
	}
	
    public static void abortLaunchIfRequired(Pack pack) {
        final Activity context = HorizonApplication.getTopRunningActivity();
        if (context == null) {
            pack.abortLaunch();
        }
        final boolean aborted = !InnerCore.checkLicence(context);
		if (aborted && !InstantReferrer.hadMinecraftActually()) {
			pack.abortLaunch();
		}
        if (context != null) {
            context.runOnUiThread(new Runnable() {
				
				public void run() {
					if (aborted) {
						if (InstantReferrer.hadLicenseWarning()) {
							new AlertDialog.Builder(context, context.getResources().getIdentifier("AppTheme.Dialog", "style", "com.zheka.horizon")).setMessage(JsonInflater.getString(context, "license_warning")).setTitle(JsonInflater.getString(context, "licence_title")).setPositiveButton(JsonInflater.getString(context, "ok"), null).show();
						}
						if (!InstantReferrer.hadMinecraftActually()) {
							return;
						}
					}
					SharedPreferences prefs = context.getSharedPreferences("InnerCore", 0);
					if (!prefs.getBoolean("first_launch_warn", false)) {
						prefs.edit().putBoolean("first_launch_warn", true).commit();
						new AlertDialog.Builder(context, context.getResources().getIdentifier("AppTheme.Dialog", "style", "com.zheka.horizon")).setMessage(JsonInflater.getString(context, "crashy_warning")).setPositiveButton(JsonInflater.getString(context, "ok"), (DialogInterface.OnClickListener) null).show();
					}
				}
			});
        }
    }
	
    public static void prepareForInjection(Pack pack) {
		if (innerCore == null) {
			innerCore = new InnerCore(HorizonApplication.getTopRunningActivity(), pack);
		} else {
			innerCore.setMinecraftActivity(HorizonApplication.getTopRunningActivity());
		}
		if (innerCore instanceof InstantInnerCore) {
			if (!InstantInnerCore.isCompletedInstant()) {
				((InstantInnerCore) innerCore).preload();
			}
		}
		innerCore.load();
    }
	
    public static void prepareForLaunch(Pack pack) {
        File noMediaFile = new File(pack.directory, ".nomedia");
        if (!noMediaFile.exists()) {
            try {
                noMediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File logsDir = new File(Environment.getExternalStorageDirectory(), "games/horizon/logs");
        if (!logsDir.isDirectory()) {
            logsDir.delete();
            logsDir.mkdirs();
        }
        if (logsDir.listFiles() != null) {
            File[] listFiles = logsDir.listFiles();
            for (File file : listFiles) {
                if (file.getName().startsWith("crash.txt")) {
                    try {
                        int hash = FileUtils.readFileText(file).hashCode();
                        file.renameTo(new File(file.getParent(), "archived-crash-" + hash + ".txt"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        file.delete();
                    }
                }
            }
        }
		try {
			String DIR_HORIZON = (String) FileTools.class.getField("DIR_HORIZON").get(null);
			NativeAPI.setWorldsPathOverride(new File(pack.directory, "worlds").getAbsolutePath());
			Method setResourcePacksPathOverride = NativeAPI.class.getMethod("setResourcePacksPathOverride", String.class);
			setResourcePacksPathOverride.invoke(null, new File(DIR_HORIZON, "resource_packs").getAbsolutePath());
        	Method setBehaviorPacksPathOverride = NativeAPI.class.getMethod("setBehaviorPacksPathOverride", String.class);
			setBehaviorPacksPathOverride.invoke(null, new File(DIR_HORIZON, "behavior_packs").getAbsolutePath());
        } catch (NoSuchFieldException | NoSuchMethodException e) {
			ICLog.i("Legacy-Warning", "Legacy version found, it might be unstable");
		} catch (IllegalAccessException | InvocationTargetException e) {
			AlertDialogReferrer.awaitReport(InstantTranslation.translate("resource_override_invocation"), e, reportResourceSetupError(e));
		}
		File installationPackage = new File(pack.directory, ".installation_package");
        if (installationPackage.isFile()) {
            installationPackage.delete();
        }
    }
	
    public static Activity getCurrentActivity() {
		if (innerCore == null) {
			return HorizonApplication.getTopActivity();
		}
        return innerCore.getCurrentActivity();
    }
	
    public static void setupResourceManager(ResourceManager manager) {
        blockTextureAtlas = new TextureAtlas(TextureType.BLOCK, "textures/terrain_texture.json", "block-atlas-descriptor", "terrain-atlas");
        manager.addResourceProcessor(blockTextureAtlas);
        manager.addRuntimeResourceHandler(blockTextureAtlas);
        itemTextureAtlas = new TextureAtlas(TextureType.ITEM, "textures/item_texture.json", "item-atlas-descriptor", "items-opaque");
        manager.addResourceProcessor(itemTextureAtlas);
        manager.addRuntimeResourceHandler(itemTextureAtlas);
        try {
            FlipbookTextureAtlas flipbookTextureAtlas = new FlipbookTextureAtlas("textures/flipbook_textures.json", "flipbook-texture-descriptor");
            manager.addResourceProcessor(flipbookTextureAtlas);
            manager.addRuntimeResourceHandler(flipbookTextureAtlas);
        } catch (Throwable err) {
            AlertDialogReferrer.awaitReport(InstantTranslation.translate("flipbook_descriptor_invocation"), err, reportResourceSetupError(err));
        }
        try {
            MaterialProcessor materialProcessor = new MaterialProcessor("materials/entity.material", "material-override-entity", "custom-materials", "custom-shaders");
            manager.addRuntimeResourceHandler(materialProcessor.newShaderUniformList("uniforms.json", "shader-uniforms-override"));
            manager.addResourceProcessor(materialProcessor);
            manager.addRuntimeResourceHandler(materialProcessor);
        } catch (Throwable err) {
            AlertDialogReferrer.awaitReport(InstantTranslation.translate("material_processor_invocation"), err, reportResourceSetupError(err));
        }
        try {
            ContentProcessor contentProcessor = new ContentProcessor();
            manager.addResourceProcessor(contentProcessor);
            manager.addRuntimeResourceHandler(contentProcessor);
            manager.addResourcePrefixes(new String[] { ResourceStorage.VANILLA_RESOURCE });
        } catch (Throwable err) {
            AlertDialogReferrer.awaitReport(InstantTranslation.translate("content_manager_invocation"), err, reportResourceSetupError("You should restart app", err));
        }
    }
	
    public static void addEnvironmentLibraries(ArrayList<File> libraries, File root) {
        libraries.add(new File(root, "libminecraftpe.so"));
    }
	
    public static void getAdditionalNativeDirectories(Pack pack, ArrayList<LibraryDirectory> nativeDirectories) {
        innerCore.addNativeDirectories(nativeDirectories);
    }
	
    public static void getAdditionalJavaDirectories(Pack pack, ArrayList<JavaDirectory> javaDirectories) {
        innerCore.addJavaDirectories(javaDirectories);
    }
	
    public static void getAdditionalResourceDirectories(Pack pack, ArrayList<ResourceDirectory> list) {
        innerCore.addResourceDirectories(list);
    }
	
    public static void addMenuActivities(Pack pack, ArrayList<Pack.MenuActivityFactory> activities) {
		Launch.tryToChangeDescription(InstantTranslation.translate("instant_build"));
		FileTools.initializeDirectories(pack.directory);
        DencityConverter.initializeDensity(pack.getModContext().context);
        activities.add(new ModsManagerActivityFactory());
        activities.add(new AboutActivityFactory());
		FileTools.initializeDirectories(pack.getWorkingDirectory());
		try {
			loadInstantInnerCore(pack, activities);
		} catch (Throwable e) {
			if (e instanceof UnsupportedOperationException) {
				if (activities.size() < 3) activities.add(new InstantLiteActivityFactory());
			}
			if (AlertDialogReferrer.awaitDecision(InstantTranslation.translate("instant_not_supported"), InstantTranslation.translate("instant_startup_interrupted") + " " + InstantTranslation.translate("continue_decision"),
					Integer.valueOf(R.string.proceed), Integer.valueOf(R.string.exit), reportResourceSetupError(e, true)) == AlertDialogReferrer.DecisionStatus.DECLINED) {
				HorizonApplication.terminate();
			}
		}
    }
	
	public static void loadInstantInnerCore(Pack pack, ArrayList<Pack.MenuActivityFactory> activities) {
		InstantInnerCore core = InstantReferrer.getInstantInnerCoreInstance();
		if (core == null) throw new UnsupportedOperationException();
		activities.add(new InstantActivityFactory());
		core.preload();
		innerCore = core;
		initializeInstantLibraries(pack);
	}
	
	public static void initializeInstantLibraries(Pack pack) {
		ExecutionDirectory executionDir = pack.contextHolder.getExecutionDir();
		executionDir.clear();
		ArrayList<LibraryDirectory> additionalNativeDirectories = new ArrayList<>();
		getAdditionalNativeDirectories(pack, additionalNativeDirectories);
		for (LibraryDirectory nativeDir : additionalNativeDirectories) {
			executionDir.addLibraryDirectory(nativeDir);
		}
		ArrayList<JavaDirectory> additionalJavaDirectories = new ArrayList<>();
		getAdditionalJavaDirectories(pack, additionalJavaDirectories);
		for (JavaDirectory javaDir : additionalJavaDirectories) {
			executionDir.addJavaDirectory(javaDir);
		}
	}
	
    static TextureAtlas getBlockTextureAtlas() {
        return blockTextureAtlas;
    }
	
    static TextureAtlas getItemTextureAtlas() {
        return itemTextureAtlas;
    }
	
    public static void initializePackRelatedAds(Pack pack, final AdsManager manager, AdDistributionModel model) {
		PrintStream printStream = System.out;
		printStream.println("init pack related ads " + model);
		if (InstantReferrer.inSupportDistribution()) {
			AdDistributionModel.DomainFactory innercoreDevDomain = new AdDistributionModel.DomainFactory() {

				public AdDomain newDomain() {
					AdDomain domain = new AdDomain(manager, "ca-app-pub-3152642364854897~5577139781");
					List<AdContainer> containers = new ArrayList<>();
					containers.add(AdContainer.newContainer(manager, "interstitial", "ca-app-pub-3152642364854897/5851444283"));
					containers.add(AdContainer.newContainer(manager, "interstitial", "ca-app-pub-3152642364854897/4538362613"));
					containers.add(AdContainer.newContainer(manager, "interstitial_video", "ca-app-pub-3152642364854897/7340991056"));
					containers.add(AdContainer.newContainer(manager, "native", "ca-app-pub-3152642364854897/5696365763"));
					containers.add(AdContainer.newContainer(manager, "native", "ca-app-pub-3152642364854897/3225280945"));
					containers.add(AdContainer.newContainer(manager, "native", "ca-app-pub-3152642364854897/9599117602"));
					containers.add(AdContainer.newContainer(manager, "native", "ca-app-pub-3152642364854897/6874264800"));
					containers.add(AdContainer.newContainer(manager, "banner", "ca-app-pub-3152642364854897/9788202751"));
					containers.add(AdContainer.newContainer(manager, "banner", "ca-app-pub-3152642364854897/2140264136"));
					containers.add(AdContainer.newContainer(manager, "banner", "ca-app-pub-3152642364854897/8283549399"));
					containers.add(AdContainer.newContainer(manager, "banner", "ca-app-pub-3152642364854897/9790689295"));
					domain.addContainers(containers);
					return domain;
				}
			};
			AdDistributionModel.DomainFactory innercoreSupportDevDomain = new AdDistributionModel.DomainFactory() {

				public AdDomain newDomain() {
					AdDomain domain = new AdDomain(manager, "ca-app-pub-6874502201951300~7445180251");
					List<AdContainer> containers = new ArrayList<>();
					containers.add(AdContainer.newContainer(manager, "interstitial", "ca-app-pub-6874502201951300/2109093097"));
					containers.add(AdContainer.newContainer(manager, "interstitial", "ca-app-pub-6874502201951300/3721557666"));
					containers.add(AdContainer.newContainer(manager, "native", "ca-app-pub-6874502201951300/6156149310"));
					containers.add(AdContainer.newContainer(manager, "native", "ca-app-pub-6874502201951300/5584948457"));
					containers.add(AdContainer.newContainer(manager, "banner", "ca-app-pub-6874502201951300/3613746455"));
					containers.add(AdContainer.newContainer(manager, "banner", "ca-app-pub-6874502201951300/6539292694"));
					domain.addContainers(containers);
					return domain;
				}
			};
			List<AdDistributionModel.Node> modAdNodes = new ArrayList<>();
			model.addDistributionNode("pack-dev", model.getWeight("innercore:pack-dev:pack-main-menu", 8.0d), "pack-main-menu");
			model.addDistributionNode("pack-dev", model.getWeight("innercore:pack-dev:pack-mod-browser", 2.0d), "pack-mod-browser");
			model.addDistributionNode("pack-main-menu", model.getWeight("innercore:pack-main-menu:innercore-dev", 60.0d), "pack-main-menu:innercore-dev");
			model.addDistributionNode("pack-main-menu", model.getWeight("innercore:pack-main-menu:innercore-support-dev", 30.0d), "pack-main-menu:innercore-support-dev");
			model.addDistributionNodeFromConfig("pack-main-menu:innercore-dev", model.getWeight("innercore:innercore-dev:self", 100.0d), "pack-main-menu:innercore-dev:self", "innercore-dev", innercoreDevDomain);
			modAdNodes.add(model.addDistributionNode("pack-main-menu:innercore-dev", model.getWeight("innercore:innercore-dev:mods", 0.0d), "pack-main-menu:innercore-dev:mods"));
			model.addDistributionNodeFromConfig("pack-main-menu:innercore-support-dev", model.getWeight("innercore:innercore-support-dev:self", 100.0d), "pack-main-menu:innercore-support-dev:self", "innercore-support-dev", innercoreSupportDevDomain);
			modAdNodes.add(model.addDistributionNode("pack-main-menu:innercore-support-dev", model.getWeight("innercore:innercore-support-dev:mods", 0.0d), "pack-main-menu:innercore-support-dev:mods"));
			model.addDistributionNode("pack-mod-browser", model.getWeight("innercore:pack-mod-browser:innercore-dev", 30.0d), "pack-mod-browser:innercore-dev");
			model.addDistributionNode("pack-mod-browser", model.getWeight("innercore:pack-mod-browser:innercore-support-dev", 70.0d), "pack-mod-browser:innercore-support-dev");
			model.addDistributionNodeFromConfig("pack-mod-browser:innercore-dev", model.getWeight("innercore:innercore-dev:self", 100.0d), "pack-mod-browser:innercore-dev:self", "innercore-dev", innercoreDevDomain);
			modAdNodes.add(model.addDistributionNode("pack-mod-browser:innercore-dev", model.getWeight("innercore:innercore-dev:mods", 0.0d), "pack-mod-browser:innercore-dev:mods"));
			model.addDistributionNodeFromConfig("pack-mod-browser:innercore-support-dev", model.getWeight("innercore:innercore-support-dev:self", 100.0d), "pack-mod-browser:innercore-support-dev:self", "innercore-support-dev", innercoreSupportDevDomain);
			modAdNodes.add(model.addDistributionNode("pack-mod-browser:innercore-support-dev", model.getWeight("innercore:innercore-support-dev:mods", 0.0d), "pack-mod-browser:innercore-support-dev:mods"));
		} else if (InstantReferrer.isAnySupportAcquired()) {
			HorizonReferrer.denyAnyDeveloperSupport(manager, model);
		}
		Launch.tryToChangeDescription(InstantTranslation.translate("instant_run"));
		initializeAndBuildInstant(pack);
	}
	
	public static void initializeAndBuildInstant(Pack pack) {
		EventLogger logger = pack.modContext.getEventLogger();
		ExecutionDirectory exec = pack.contextHolder.getExecutionDir();
		logger.section("build");
		LaunchSequence sequence = exec.build(pack.modContext.getActivityContext(), logger);
        sequence.buildSequence(logger);
		logger.section("initialize");
		sequence.loadAll(logger);
		try {
			Method nativeInitializeAllModules = pack.modContext.getClass().getDeclaredMethod("nativeInitializeAllModules");
			nativeInitializeAllModules.setAccessible(true);
			nativeInitializeAllModules.invoke(pack.modContext);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	public static void addCustomDrawables(Pack pack, String category, ArrayList<Drawable> drawables) {
		if (category == "menu-background") {
			HorizonReferrer.overrideLaunchedHorizonActivity(pack, drawables);
		}
	}
	
	public static void buildCustomMenuLayout(View layout, View button) {
		buildCustomMenuLayout((ViewGroup) layout, button);
	}
	
	public static void buildCustomMenuLayout(ViewGroup layout, View button) {
		HorizonReferrer.prepatchHorizonActivity(layout, button);
		layout.post(new Runnable() {
			
			@Override
			public void run() {
				preloadInstantIfNeeded();
			}
		});
	}
	
	public static void initiateInstantLaunch(Activity activity, InstantInnerCore core) {
		try {
			core.setMinecraftActivity(activity);
			core.launchInstant();
		} catch (Throwable e) {
			reportResourceSetupError(e).run();
			DialogHelper.reportFatalError(e.getLocalizedMessage(), e);
		}
	}
	
	public static void preloadInstantIfNeeded() {
		if (!isPreloadedInstant) {
			if (InstantInnerCore.isCompletedInstant()) {
				new Handler().post(INSTANT_LAUNCH);
			}
			isPreloadedInstant = true;
		}
	}
	
	public static Runnable INSTANT_LAUNCH = new Runnable() {
		
		@Override
		public void run() {
			InnerCore core = InnerCore.getInstance();
			if (core instanceof InstantInnerCore) {
				Activity activity = InstantReferrer.Horizon.get().getSingleton();
				initiateInstantLaunch(activity, (InstantInnerCore) core);
			}
		}
	};
}
