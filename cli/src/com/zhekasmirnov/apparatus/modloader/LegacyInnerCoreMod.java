package com.zhekasmirnov.apparatus.modloader;

import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;
import com.zhekasmirnov.apparatus.minecraft.version.ResourceGameVersion;
import com.zhekasmirnov.innercore.mod.build.BuildConfig;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.enums.ResourceDirType;
import java.io.File;
import java.util.Iterator;

public class LegacyInnerCoreMod extends DirectoryBasedMod {
    private final Mod legacyModInstance;
    private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$ResourceDirType;

    static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$ResourceDirType() {
        int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$ResourceDirType;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[ResourceDirType.valuesCustom().length];
        try {
            iArr2[ResourceDirType.GUI.ordinal()] = 2;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[ResourceDirType.RESOURCE.ordinal()] = 1;
        } catch (NoSuchFieldError unused2) {
        }
        $SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$ResourceDirType = iArr2;
        return iArr2;
    }

    public LegacyInnerCoreMod(Mod mod) {
        super(new File(mod.dir));
        getInfo().pullLegacyModProperties(mod);
        this.legacyModInstance = mod;
    }

    public Mod getLegacyModInstance() {
        return this.legacyModInstance;
    }

    @Override
    public boolean isEnabledAndAbleToRun() {
        if (!this.legacyModInstance.getConfig().getBool("enabled")) {
            return false;
        }
        return this.legacyModInstance.buildConfig.defaultConfig.gameVersion.isCompatible();
    }

    @Override
    public void onSettingUpEnvironment(EnvironmentSetupProxy proxy, ModLoaderReporter reporter) {
        File[] resourcePacks;
        File[] behaviorPacks;
        Iterator<BuildConfig.ResourceDir> it = this.legacyModInstance.buildConfig.resourceDirs.iterator();
        while (it.hasNext()) {
            BuildConfig.ResourceDir resourceDir = it.next();
            if (resourceDir.gameVersion.isCompatible()) {
                switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$mod$build$enums$ResourceDirType()[resourceDir.resourceType.ordinal()]) {
                    case 1:
                        proxy.addResourceDirectory(this, new File(getDirectory(), resourceDir.path));
                        continue;
                    case 2:
                        proxy.addGuiAssetsDirectory(this, new File(getDirectory(), resourceDir.path));
                        continue;
                }
            }
        }
        String behaviorPackDirName = this.legacyModInstance.buildConfig.defaultConfig.behaviorPacksDir;
        if (behaviorPackDirName != null && (behaviorPacks = new File(getDirectory(), behaviorPackDirName).listFiles()) != null) {
            for (File behaviorPack : behaviorPacks) {
                if (new ResourceGameVersion(new File(behaviorPack, "game_version.json")).isCompatible()) {
                    proxy.addBehaviorPackDirectory(this, behaviorPack);
                }
            }
        }
        String resourcePackDirName = this.legacyModInstance.buildConfig.defaultConfig.resourcePacksDir;
        if (resourcePackDirName != null && (resourcePacks = new File(getDirectory(), resourcePackDirName).listFiles()) != null) {
            for (File resourcePack : resourcePacks) {
                if (new ResourceGameVersion(new File(resourcePack, "game_version.json")).isCompatible()) {
                    proxy.addResourcePackDirectory(this, resourcePack);
                }
            }
        }
        Iterator<BuildConfig.DeclaredDirectory> it2 = this.legacyModInstance.buildConfig.javaDirectories.iterator();
        while (it2.hasNext()) {
            BuildConfig.DeclaredDirectory directory = it2.next();
            if (directory.version.isCompatible()) {
                proxy.addJavaDirectory(this, directory.getFile(getDirectory()));
            }
        }
        Iterator<BuildConfig.DeclaredDirectory> it3 = this.legacyModInstance.buildConfig.nativeDirectories.iterator();
        while (it3.hasNext()) {
            BuildConfig.DeclaredDirectory directory2 = it3.next();
            if (directory2.version.isCompatible()) {
                proxy.addNativeDirectory(this, directory2.getFile(getDirectory()));
            }
        }
    }

    @Override
    public void onPrepareResources(ModLoaderReporter reporter) {
        this.legacyModInstance.RunPreloaderScripts();
    }

    @Override
    public void onRunningMod(ModLoaderReporter reporter) {
        this.legacyModInstance.RunLauncherScripts();
        getInfo().pullLegacyModProperties(this.legacyModInstance);
    }

    @Override
    public void onShuttingDown(ModLoaderReporter reporter) {
    }
}
