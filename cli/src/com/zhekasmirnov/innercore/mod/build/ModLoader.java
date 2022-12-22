package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.apparatus.modloader.ApparatusModLoader;
import com.zhekasmirnov.apparatus.modloader.LegacyInnerCoreMod;
import com.zhekasmirnov.apparatus.modloader.ModLoaderReporter;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.modpack.DirectorySetRequestHandler;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.mcpe161.InnerCore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModLoader {
    public static final String LOGGER_TAG = "INNERCORE-MOD";
    private static final ModLoaderReporter defaultLogReporter = C$$Lambda$ModLoader$lplr3acSrIo8RFRCRoW0bW4p9Lg.INSTANCE;
    public static ModLoader instance;
    public ArrayList<Mod> modsList = new ArrayList<>();
    private List<File> resourcePackDirs = new ArrayList<>();
    private List<File> behaviorPackDirs = new ArrayList<>();
    private List<MinecraftPack> minecraftPacks = new ArrayList<>();
    private final HashMap<String, File> runtimePackDirs = new HashMap<>();
    private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$ModLoader$MinecraftPackType;

    static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$ModLoader$MinecraftPackType() {
        int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$ModLoader$MinecraftPackType;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[MinecraftPackType.valuesCustom().length];
        try {
            iArr2[MinecraftPackType.BEHAVIOR.ordinal()] = 2;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[MinecraftPackType.RESOURCE.ordinal()] = 1;
        } catch (NoSuchFieldError unused2) {
        }
        $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$ModLoader$MinecraftPackType = iArr2;
        return iArr2;
    }

    public static void initialize() {
        instance = new ModLoader();
    }

    public void loadMods() {
        this.modsList.clear();
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        DirectorySetRequestHandler requestHandler = modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);
        List<String> allModLocations = requestHandler.getAllLocations();
        Logger.debug("INNERCORE-MOD", "found " + allModLocations.size() + " potential mod dirs.");
        int modIndex = 1;
        for (String locationName : allModLocations) {
            Logger.debug("INNERCORE-MOD", "investigating mod location: " + locationName);
            File file = requestHandler.get(locationName);
            if (file.isDirectory()) {
                int modIndex2 = modIndex + 1;
                LoadingUI.setTextAndProgressBar("Loading Mods: " + modIndex + "/" + allModLocations.size(), ((modIndex * 0.25f) / allModLocations.size()) + 0.15f);
                String modDir = file.getAbsolutePath() + "/";
                if (ModBuilder.analyzeAndSetupModDir(modDir)) {
                    Logger.debug("INNERCORE-MOD", "building and importing mod: " + file.getName());
                    Mod mod = ModBuilder.buildModForDir(modDir, modPack, locationName);
                    if (mod != null) {
                        this.modsList.add(mod);
                        mod.onImport();
                    } else {
                        Logger.debug("INNERCORE-MOD", "failed to build mod: build.config file could not be parsed.");
                    }
                }
                modIndex = modIndex2;
            }
        }
    }

    public void runPreloaderScripts() {
        LoadingStage.setStage(3);
        Logger.debug("INNERCORE-MOD", "imported " + this.modsList.size() + " mods.");
        for (int i = 0; i < this.modsList.size(); i++) {
            Mod mod = this.modsList.get(i);
            LoadingUI.setText("Initializing Mods: " + (i + 1) + "/" + this.modsList.size() + ": " + mod.getName());
            mod.RunPreloaderScripts();
        }
    }

    public void startMods() {
        for (int i = 0; i < this.modsList.size(); i++) {
            LoadingUI.setTextAndProgressBar("Running Mods: " + (i + 1) + "/" + this.modsList.size() + " ", ((i * 0.3f) / this.modsList.size()) + 0.7f);
            this.modsList.get(i).RunLauncherScripts();
        }
    }

    public static void addMinecraftResourcePack(File directory) {
        if (!instance.resourcePackDirs.contains(directory)) {
            instance.resourcePackDirs.add(directory);
            ICLog.d("ModLoader", "added minecraft pack: " + directory);
        }
    }

    public static void addMinecraftBehaviorPack(File directory) {
        if (!instance.behaviorPackDirs.contains(directory)) {
            instance.behaviorPackDirs.add(directory);
            ICLog.d("ModLoader", "added minecraft pack: " + directory);
        }
    }

    public static void addGlobalMinecraftPacks() {
        new File(FileTools.DIR_WORK, "resource_packs").mkdirs();
        new File(FileTools.DIR_WORK, "behavior_packs").mkdirs();
        DirectorySetRequestHandler globalResourcePacks = ModPackContext.getInstance().getCurrentModPack().getRequestHandler(ModPackDirectory.DirectoryType.RESOURCE_PACKS);
        for (String location : globalResourcePacks.getAllLocations()) {
            File packDir = globalResourcePacks.get(location);
            if (packDir.isDirectory()) {
                addMinecraftResourcePack(packDir);
            }
        }
        DirectorySetRequestHandler globalBehaviorPacks = ModPackContext.getInstance().getCurrentModPack().getRequestHandler(ModPackDirectory.DirectoryType.BEHAVIOR_PACKS);
        for (String location2 : globalBehaviorPacks.getAllLocations()) {
            File packDir2 = globalBehaviorPacks.get(location2);
            if (packDir2.isDirectory()) {
                addMinecraftBehaviorPack(packDir2);
            }
        }
    }

    public enum MinecraftPackType {
        RESOURCE,
        BEHAVIOR;

        public static MinecraftPackType[] valuesCustom() {
            MinecraftPackType[] valuesCustom = values();
            int length = valuesCustom.length;
            MinecraftPackType[] minecraftPackTypeArr = new MinecraftPackType[length];
            System.arraycopy(valuesCustom, 0, minecraftPackTypeArr, 0, length);
            return minecraftPackTypeArr;
        }

        public static MinecraftPackType fromString(String str) {
            char c;
            int hashCode = str.hashCode();
            if (hashCode == -406349635) {
                if (str.equals("behaviour")) {
                }
                c = 65535;
            } else if (hashCode != -341064690) {
                if (hashCode != 1510912594 || str.equals("behavior")) {
                }
                c = 65535;
            } else {
                if (str.equals("resource")) {
                }
                c = 65535;
            }
            switch (c) {
                case 0:
                    return RESOURCE;
                case 1:
                case 2:
                    return BEHAVIOR;
                default:
                    throw new IllegalArgumentException("invalid minecraft pack type: " + str);
            }
        }
    }

    private static class MinecraftPack {
        private final File directory;
        private final MinecraftPackType type;
        private final String uuid;
        private final JSONArray version;

        public MinecraftPack(File directory, MinecraftPackType type, String uuid, JSONArray version) {
            this.directory = directory;
            this.type = type;
            this.uuid = uuid;
            this.version = version;
        }

        public JSONObject getJsonForWorldPacks() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("version", this.version);
                obj.put("pack_id", this.uuid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj;
        }

        public static MinecraftPack fromDirectory(File directory, MinecraftPackType type) {
            File manifest = new File(directory, "manifest.json");
            if (manifest.isFile()) {
                try {
                    JSONObject manifestJson = FileUtils.readJSON(manifest);
                    JSONObject header = manifestJson.optJSONObject("header");
                    if (header != null) {
                        String uuid = header.optString("uuid", null);
                        JSONArray array = header.optJSONArray("version");
                        if (uuid != null && array != null) {
                            return new MinecraftPack(directory, type, uuid, array);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
            ICLog.i("ERROR", "failed to read minecraft pack uuid or version: " + directory);
            return null;
        }
    }

    private void loadMinecraftPacksIntoDirectory(MinecraftPackType type, File targetDir, List<File> files) {
        File target;
        if (targetDir.isDirectory()) {
            FileUtils.clearFileTree(targetDir, false);
        } else {
            targetDir.delete();
            targetDir.mkdirs();
        }
        for (File file : files) {
            File file2 = new File(targetDir, file.getName());
            while (true) {
                target = file2;
                if (!target.exists()) {
                    break;
                }
                file2 = new File(targetDir, String.valueOf(target.getName()) + "-");
            }
            target.mkdirs();
            try {
                FileUtils.copyFileTree(file, target, null, null);
                MinecraftPack pack = MinecraftPack.fromDirectory(target, type);
                if (pack != null) {
                    this.minecraftPacks.add(pack);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteTempPacksInDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File packDir : directory.listFiles()) {
                if (new File(packDir, ".tmp_resources").exists()) {
                    FileUtils.clearFileTree(packDir, true);
                }
            }
        }
    }

    private List<File> findPackInDirectory(File directory, MinecraftPack pack) {
        List<File> result = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File packDir : directory.listFiles()) {
                MinecraftPack packFromDir = MinecraftPack.fromDirectory(packDir, pack.type);
                if (packFromDir != null && packFromDir.uuid != null && packFromDir.uuid.equals(pack.uuid)) {
                    result.add(packDir);
                }
            }
        }
        return result;
    }

    private void injectPacksInDirectory(File directory, MinecraftPackType type) {
        deleteTempPacksInDirectory(directory);
        try {
            for (MinecraftPack pack : this.minecraftPacks) {
                if (pack.type == type) {
                    for (File packDir : findPackInDirectory(directory, pack)) {
                        FileUtils.clearFileTree(packDir, true);
                    }
                    File target = new File(directory, pack.directory.getName());
                    while (target.exists()) {
                        target = new File(directory, String.valueOf(target.getName()) + "-");
                    }
                    target.mkdirs();
                    try {
                        FileUtils.copyFileTree(pack.directory, target, null, null);
                        new File(target, ".tmp_resources").createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void deleteDuplicatePacksInDir(File directory, MinecraftPackType type) {
        if (directory.isDirectory()) {
            for (File packDir : directory.listFiles()) {
                MinecraftPack pack = MinecraftPack.fromDirectory(packDir, type);
                if (pack != null) {
                    for (MinecraftPack addedPack : this.minecraftPacks) {
                        if (addedPack.type == type && addedPack.uuid.equals(pack.uuid)) {
                            FileUtils.clearFileTree(packDir, true);
                        }
                    }
                }
            }
        }
    }

    public void loadResourceAndBehaviorPacks() {
        this.minecraftPacks.clear();
        loadMinecraftPacksIntoDirectory(MinecraftPackType.RESOURCE, new File(FileTools.DIR_HORIZON, "resource_packs"), this.resourcePackDirs);
        loadMinecraftPacksIntoDirectory(MinecraftPackType.BEHAVIOR, new File(FileTools.DIR_HORIZON, "behavior_packs"), this.behaviorPackDirs);
        Callback.invokeAPICallback("AddRuntimePacks", new Object[0]);
    }

    public File addRuntimePack(MinecraftPackType type, String name) {
        if (this.runtimePackDirs.containsKey(name)) {
            return this.runtimePackDirs.get(name);
        }
        File packsDirectory = null;
        String moduleType = null;
        String headerUuid = UUID.randomUUID().toString();
        switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$ModLoader$MinecraftPackType()[type.ordinal()]) {
            case 1:
                packsDirectory = new File(FileTools.DIR_HORIZON, "resource_packs");
                moduleType = "resources";
                break;
            case 2:
                packsDirectory = new File(FileTools.DIR_HORIZON, "behavior_packs");
                moduleType = "data";
                break;
        }
        File directory = new File(packsDirectory, "runtime_" + name);
        directory.mkdirs();
        JSONArray version001 = new JSONArray().put(0).put(0).put(1);
        try {
            FileTools.writeJSON(new File(directory, "manifest.json").getAbsolutePath(), MinecraftVersions.getCurrent().createRuntimePackManifest(name, headerUuid, moduleType, version001));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.minecraftPacks.add(new MinecraftPack(directory, type, headerUuid, version001));
        return directory;
    }

    public void addResourceAndBehaviorPacksInWorld(File worldDir) {
        File worldResourcePacks = new File(worldDir, "world_resource_packs.json");
        File worldResourcePacksDir = new File(worldDir, "resource_packs");
        File worldBehaviorPacks = new File(worldDir, "world_behavior_packs.json");
        File worldBehaviorPacksDir = new File(worldDir, "behavior_packs");
        JSONArray resourcePacksArray = new JSONArray();
        JSONArray behaviorPacksArray = new JSONArray();
        injectPacksInDirectory(worldResourcePacksDir, MinecraftPackType.RESOURCE);
        injectPacksInDirectory(worldBehaviorPacksDir, MinecraftPackType.BEHAVIOR);
        List<MinecraftPack> worldPacks = new ArrayList<>(this.minecraftPacks);
        File[] worldResourcePackDirs = worldResourcePacksDir.listFiles();
        if (worldResourcePackDirs != null) {
            for (File packDir : worldResourcePackDirs) {
                MinecraftPack pack = MinecraftPack.fromDirectory(packDir, MinecraftPackType.RESOURCE);
                if (pack != null) {
                    worldPacks.add(pack);
                }
            }
        }
        File[] worldBehaviorPackDirs = worldBehaviorPacksDir.listFiles();
        if (worldBehaviorPackDirs != null) {
            for (File packDir2 : worldBehaviorPackDirs) {
                MinecraftPack pack2 = MinecraftPack.fromDirectory(packDir2, MinecraftPackType.BEHAVIOR);
                if (pack2 != null) {
                    worldPacks.add(pack2);
                }
            }
        }
        for (MinecraftPack pack3 : worldPacks) {
            switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$ModLoader$MinecraftPackType()[pack3.type.ordinal()]) {
                case 1:
                    resourcePacksArray.put(pack3.getJsonForWorldPacks());
                    break;
                case 2:
                    behaviorPacksArray.put(pack3.getJsonForWorldPacks());
                    break;
            }
        }
        try {
            FileUtils.writeJSON(worldResourcePacks, resourcePacksArray);
            FileUtils.writeJSON(worldBehaviorPacks, behaviorPacksArray);
        } catch (IOException e) {
            e.printStackTrace();
            ICLog.e("ERROR", "failed to write world packs json", e);
        }
    }

    public static void loadModsAndSetupEnvViaNewModLoader() {
        List<ApparatusModLoader.AbstractModSource> modSources = new ArrayList<>();
        modSources.add(C$$Lambda$ModLoader$ubVxldxDVN9CfvLesKW_4qOJU.INSTANCE);
        ApparatusModLoader.getSingleton().reloadModsAndSetupEnvironment(modSources, InnerCore.getEnvironmentSetupProxy(), defaultLogReporter);
    }

    static void lambda$loadModsAndSetupEnvViaNewModLoader$1(List<ApparatusMod> modList, ModLoaderReporter reporter) {
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        DirectorySetRequestHandler requestHandler = modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);
        List<String> allModLocations = requestHandler.getAllLocations();
        Logger.debug("INNERCORE-MOD", "found " + allModLocations.size() + " potential mod dirs.");
        for (String locationName : allModLocations) {
            Logger.debug("INNERCORE-MOD", "investigating mod location: " + locationName);
            File file = requestHandler.get(locationName);
            if (file.isDirectory()) {
                String modDir = String.valueOf(file.getAbsolutePath()) + "/";
                if (ModBuilder.analyzeAndSetupModDir(modDir)) {
                    Logger.debug("INNERCORE-MOD", "building and importing mod: " + file.getName());
                    Mod mod = ModBuilder.buildModForDir(modDir, modPack, locationName);
                    if (mod != null) {
                        mod.onImport();
                        modList.add(new LegacyInnerCoreMod(mod));
                    } else {
                        Logger.debug("INNERCORE-MOD", "failed to build mod: build.config file could not be parsed.");
                    }
                }
            }
        }
    }

    public static void prepareResourcesViaNewModLoader() {
        ApparatusModLoader.getSingleton().prepareModResources(defaultLogReporter);
    }

    public static void runModsViaNewModLoader() {
        ApparatusModLoader.getSingleton().runMods(defaultLogReporter);
    }
}
