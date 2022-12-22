package com.zhekasmirnov.mcpe161;

import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;
import com.zhekasmirnov.apparatus.adapter.innercore.PackInfo;
import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.ResourceDirectory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JsonIterator;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.log.ModLoaderEventHandler;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.modpack.DirectorySetRequestHandler;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.ColorsPatch;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.ReflectionPatch;
import com.zhekasmirnov.innercore.utils.UIUtils;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InnerCore {
    public static final String LOGGER_TAG = "INNERCORE-LAUNHER";
    public static final boolean isLicenceVersion = true;
    public List<File> allResourceDirectories = new ArrayList<>();
    private WeakReference<Object> currentActivity;
    private final Pack pack;
    private static InnerCore instance = null;
    private static final List<File> javaDirectoriesFromProxy = new ArrayList<>();
    private static final List<File> nativeDirectoriesFromProxy = new ArrayList<>();
    private static final List<File> resourceDirectoriesFromProxy = new ArrayList<>();
    private static final EnvironmentSetupProxy environmentSetupProxy = new EnvironmentSetupProxy() {
        @Override
        public void addResourceDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                InnerCore.resourceDirectoriesFromProxy.add(directory);
            }
        }

        @Override
        public void addGuiAssetsDirectory(ApparatusMod mod, File directory) {
        }

        @Override
        public void addNativeDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                InnerCore.nativeDirectoriesFromProxy.add(directory);
            }
        }

        @Override
        public void addJavaDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                InnerCore.javaDirectoriesFromProxy.add(directory);
            }
        }

        @Override
        public void addResourcePackDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                ModLoader.addMinecraftResourcePack(directory);
            }
        }

        @Override
        public void addBehaviorPackDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                ModLoader.addMinecraftBehaviorPack(directory);
            }
        }
    };

    public static InnerCore getInstance() {
        return instance;
    }

    public Pack getPack() {
        return this.pack;
    }

    public static List<File> getJavaDirectoriesFromProxy() {
        return javaDirectoriesFromProxy;
    }

    public InnerCore(Object context, Pack pack) {
        FileTools.initializeDirectories(pack.directory);
        instance = this;
        this.pack = pack;
        this.currentActivity = new WeakReference<>(context);
        Logger.info("initializing innercore");
    }

    public static boolean checkLicence(Object activity) {
        return isMCPEInstalled(activity);
    }

    public void load() {
        ReflectionPatch.init();
        ColorsPatch.init();
        API.loadAllAPIs();
        initiateLoading();
    }

    public void setMinecraftActivity(Object activity) {
        this.currentActivity = new WeakReference<>(activity);
    }

    public Object getCurrentActivity() {
        return this.currentActivity.get();
    }

    private static boolean isMCPEInstalled(Object activity) {
        return true;
    }

    private void initiateLoading() {
        Logger.debug("INNERCORE", String.format("Inner Core %s Started", PackInfo.getPackVersionName()));
        LoadingStage.setStage(1);
        preloadInnerCore();
    }

    private void addAllResourcePacks() {
        String name;
        File defaultResourcePacksDir = new File(this.pack.getWorkingDirectory(), "resourcepacks");
        if (!defaultResourcePacksDir.isDirectory()) {
            defaultResourcePacksDir.delete();
        }
        if (!defaultResourcePacksDir.isDirectory()) {
            defaultResourcePacksDir.mkdirs();
        }
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        DirectorySetRequestHandler texturePacks = modPack.getRequestHandler(ModPackDirectory.DirectoryType.TEXTURE_PACKS);
        List<String> allNames = new ArrayList<>();
        List<String> names = new ArrayList<>();
        JSONObject json = new JSONObject();
        try {
            json = FileUtils.readJSON(texturePacks.get("", "resourcepacks.json"));
        } catch (IOException e) {
        } catch (JSONException e2) {
        }
        JSONArray packsJson = json.optJSONArray("packs");
        if (packsJson != null) {
            Iterator<Object> it = new JsonIterator<>(packsJson).iterator();
            while (it.hasNext()) {
                JSONObject resourcePackJson = (JSONObject) it.next();
                if (resourcePackJson != null && (name = resourcePackJson.optString("name", null)) != null && name.length() > 0 && !allNames.contains(name)) {
                    if (resourcePackJson.optBoolean("enabled", true)) {
                        names.add(name);
                    }
                    allNames.add(name);
                }
            }
        }
        for (String name2 : texturePacks.getAllLocations()) {
            if (!allNames.contains(name2)) {
                names.add(name2);
                allNames.add(name2);
            }
        }
        for (String name3 : names) {
            File resourceDir = texturePacks.get(name3);
            if (resourceDir.isDirectory()) {
                this.allResourceDirectories.add(resourceDir);
            }
        }
    }

    private void addAllModResources() {
        File defaultTextures = new File(this.pack.getWorkingDirectory(), "assets/textures/");
        this.allResourceDirectories.add(defaultTextures);
        this.allResourceDirectories.addAll(resourceDirectoriesFromProxy);
    }

    private void preloadInnerCore() {
        ModPackContext.getInstance().assurePackSelected();
        ICLog.setupEventHandlerForCurrentThread(new ModLoaderEventHandler());
        LoadingUI.setTextAndProgressBar("Initializing Resources...", 0.0f);
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
        }
        LoadingStage.setStage(2);
        UIUtils.initialize(this.currentActivity.get());
        ResourcePackManager.instance.initializeResources();
        LoadingUI.setTextAndProgressBar("Loading Mods...", 0.15f);
        ModLoader.initialize();
        ModLoader.loadModsAndSetupEnvViaNewModLoader();
        addAllResourcePacks();
        addAllModResources();
        ModLoader.prepareResourcesViaNewModLoader();
        ModLoader.addGlobalMinecraftPacks();
        ModLoader.instance.loadResourceAndBehaviorPacks();
        LoadingUI.setTextAndProgressBar("Generating Cache...", 0.4f);
        LoadingUI.setTextAndProgressBar("Starting Minecraft...", 0.5f);
    }

    public void addResourceDirectories(ArrayList<ResourceDirectory> list) {
        ResourceManager manager = getResourceManager();
        Logger.debug("addResourceDirectories", String.valueOf(list.size()) + " " + list.toString());
        for (File dir : this.allResourceDirectories) {
            try {
                list.add(new ResourceDirectory(manager, dir));
            } catch (IllegalStateException e) {
                Logger.warning("Resource directory " + dir + " not found, skipping!");
            }
        }
    }

    public void addNativeDirectories(ArrayList<LibraryDirectory> list) {
        for (File directory : nativeDirectoriesFromProxy) {
            list.add(new LibraryDirectory(directory));
        }
    }

    public void addJavaDirectories(ArrayList<JavaDirectory> list) {
        for (File directory : javaDirectoriesFromProxy) {
            list.add(new JavaDirectory(null, directory));
        }
    }

    public void onFinalLoadComplete() {
        LoadingStage.setStage(7);
        ICLog.showIfErrorsAreFound();
        LoadingStage.outputTimeMap();
    }

    public String getWorkingDirectory() {
        return this.pack.getWorkingDirectory().getAbsolutePath();
    }

    public ResourceManager getResourceManager() {
        return this.pack.getModContext().getResourceManager();
    }

    public TextureAtlas getBlockTextureAtlas() {
        return EnvironmentSetup.getBlockTextureAtlas();
    }

    public TextureAtlas getItemTextureAtlas() {
        return EnvironmentSetup.getItemTextureAtlas();
    }

    public static EnvironmentSetupProxy getEnvironmentSetupProxy() {
        return environmentSetupProxy;
    }
}
