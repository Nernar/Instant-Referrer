package com.zhekasmirnov.apparatus.modloader;

import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import java.util.ArrayList;
import java.util.List;

public class ApparatusModLoader {
    private static final ApparatusModLoader singleton = new ApparatusModLoader();
    private final List<ApparatusMod> allMods = new ArrayList<>();

    public interface AbstractModSource {
        void addMods(List<ApparatusMod> list, ModLoaderReporter modLoaderReporter);
    }

    public static ApparatusModLoader getSingleton() {
        return singleton;
    }

    private ApparatusModLoader() {
    }

    public List<ApparatusMod> getAllMods() {
        return this.allMods;
    }

    public void shutdownAndClear(ModLoaderReporter reporter) {
        synchronized (this.allMods) {
            for (ApparatusMod mod : this.allMods) {
                mod.onShuttingDown(reporter);
            }
            this.allMods.clear();
        }
    }

    public void reloadModsAndSetupEnvironment(List<AbstractModSource> sources, EnvironmentSetupProxy proxy, ModLoaderReporter reporter) {
        shutdownAndClear(reporter);
        List<ApparatusMod> modsFromSources = new ArrayList<>();
        for (AbstractModSource source : sources) {
            source.addMods(modsFromSources, reporter);
        }
        for (ApparatusMod mod : modsFromSources) {
            if (mod.isEnabledAndAbleToRun()) {
                this.allMods.add(mod);
            }
        }
        for (ApparatusMod mod2 : this.allMods) {
            mod2.onSettingUpEnvironment(proxy, reporter);
            mod2.setModState(ApparatusMod.ModState.ENVIRONMENT_SETUP);
        }
    }

    public void prepareModResources(ModLoaderReporter reporter) {
        for (ApparatusMod mod : this.allMods) {
            mod.onPrepareResources(reporter);
            mod.setModState(ApparatusMod.ModState.PREPARED);
        }
    }

    public void runMods(ModLoaderReporter reporter) {
        int progress = 1;
        int total = this.allMods.size();
        for (ApparatusMod mod : this.allMods) {
            LoadingUI.setTextAndProgressBar("Running Mods  " + progress + "/" + total + "...", ((progress / total) * 0.3f) + 0.5f);
            LoadingUI.setTip(mod.getInfo().getString("displayed_name", ""));
            progress++;
            mod.onRunningMod(reporter);
            mod.setModState(ApparatusMod.ModState.RUNNING);
        }
    }
}
