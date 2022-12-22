package com.zhekasmirnov.apparatus.modloader;

import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;

public abstract class ApparatusMod {
    private ModState modState = ModState.INITIALIZED;
    private final ApparatusModInfo info = new ApparatusModInfo();

    public enum ModState {
        INITIALIZED,
        ENVIRONMENT_SETUP,
        PREPARED,
        RUNNING,
        SHUTDOWN;

        public static ModState[] valuesCustom() {
            ModState[] valuesCustom = values();
            int length = valuesCustom.length;
            ModState[] modStateArr = new ModState[length];
            System.arraycopy(valuesCustom, 0, modStateArr, 0, length);
            return modStateArr;
        }
    }

    public abstract boolean isEnabledAndAbleToRun();

    public abstract void onPrepareResources(ModLoaderReporter modLoaderReporter);

    public abstract void onRunningMod(ModLoaderReporter modLoaderReporter);

    public abstract void onSettingUpEnvironment(EnvironmentSetupProxy environmentSetupProxy, ModLoaderReporter modLoaderReporter);

    public abstract void onShuttingDown(ModLoaderReporter modLoaderReporter);

    public ApparatusModInfo getInfo() {
        return this.info;
    }

    public ModState getModState() {
        return this.modState;
    }

    void setModState(ModState modState) {
        this.modState = modState;
    }
}
