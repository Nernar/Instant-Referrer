package com.zhekasmirnov.horizon.modloader;

import com.zhekasmirnov.horizon.modloader.mod.Mod;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ModContext {
    public final Object context;
    public final ExecutionDirectory executionDirectory;
    public final ResourceManager resourceManager;
    private LaunchSequence sequence;
    private final List<Mod> disabledMods = new ArrayList<>();
    private final List<Mod> activeMods = new ArrayList<>();
    private final EventLogger logger = new EventLogger();
    private final HashMap<String, List<EventReceiver>> eventReceivers = new HashMap<>();

    public interface EventReceiver {
        void onEvent(Mod... modArr);
    }

    private static native void nativeClearModuleRegistry();

    private static native void nativeInitializeAllModules();

    public ModContext(Object context, ResourceManager resourceManager, ExecutionDirectory executionDirectory) {
        this.context = context;
        this.resourceManager = resourceManager;
        this.executionDirectory = executionDirectory;
    }

    public Object getActivityContext() {
        return this.context;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public ExecutionDirectory getExecutionDirectory() {
        return this.executionDirectory;
    }

    public List<Mod> getActiveMods() {
        return this.activeMods;
    }

    public List<Mod> getDisabledMods() {
        return this.disabledMods;
    }

    public EventLogger getEventLogger() {
        return this.logger;
    }

    public void clearContext() {
        this.resourceManager.clear();
        this.executionDirectory.clear();
        sendEvent("clearContext", new Mod[0]);
    }

    public void clearModsAndContext() {
        clearContext();
        this.activeMods.clear();
        this.disabledMods.clear();
        sendEvent("clearMods", new Mod[0]);
    }

    private void handleModSafety(Mod mod) {
        Mod.SafetyInterface safetyInterface = mod.getSafetyInterface();
        Mod.ConfigurationInterface configurationInterface = mod.getConfigurationInterface();
        if (safetyInterface.isCrashRegistered()) {
            safetyInterface.setDisabledDueToCrash(true);
            safetyInterface.removeCrashLock();
            configurationInterface.setActive(false);
        }
        if (safetyInterface.isDisabledDueToCrash()) {
            if (configurationInterface.isActive()) {
                safetyInterface.setDisabledDueToCrash(false);
            } else {
                sendEvent("disabledDueToCrash", mod);
            }
        }
    }

    public void addMod(Mod mod) {
        Mod.ConfigurationInterface configurationInterface = mod.getConfigurationInterface();
        handleModSafety(mod);
        if (configurationInterface.isActive()) {
            this.activeMods.add(mod);
        } else {
            this.disabledMods.add(mod);
        }
    }

    public void addMods(Collection<Mod> collection) {
        for (Mod mod : collection) {
            addMod(mod);
        }
    }

    public void injectAll() {
        sendEvent("injectAll", new Mod[0]);
        for (Mod mod : this.activeMods) {
            mod.inject();
        }
    }

    public void buildAll() {
        sendEvent("buildAll", new Mod[0]);
        getEventLogger().section("build");
        LaunchSequence build = this.executionDirectory.build(this.context, getEventLogger());
        this.sequence = build;
        build.buildSequence(getEventLogger());
    }

    public void initializeAll() {
        getEventLogger().section("initialize");
        sendEvent("initializeAll", new Mod[0]);
        this.sequence.loadAll(getEventLogger());
        for (Mod mod : this.activeMods) {
            mod.initialize();
        }
    }

    public void launchAll() {
        sendEvent("launchAll", new Mod[0]);
        this.resourceManager.deployAllOverrides();
    }

    public void addEventReceiver(String str, EventReceiver eventReceiver) {
        List<EventReceiver> list = this.eventReceivers.get(str);
        if (list == null) {
            list = new ArrayList<>();
            this.eventReceivers.put(str, list);
        }
        list.add(eventReceiver);
    }

    private void sendEvent(String str, Mod... modArr) {
        List<EventReceiver> list = this.eventReceivers.get(str);
        if (list != null) {
            for (EventReceiver eventReceiver : list) {
                eventReceiver.onEvent(modArr);
            }
        }
    }
}
