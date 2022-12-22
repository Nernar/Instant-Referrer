package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.apparatus.modloader.ApparatusModLoader;
import com.zhekasmirnov.apparatus.modloader.ModLoaderReporter;
import java.util.List;

public final class C$$Lambda$ModLoader$ubVxldxDVN9CfvLesKW_4qOJU implements ApparatusModLoader.AbstractModSource {
    public static final C$$Lambda$ModLoader$ubVxldxDVN9CfvLesKW_4qOJU INSTANCE = new C$$Lambda$ModLoader$ubVxldxDVN9CfvLesKW_4qOJU();

    private C$$Lambda$ModLoader$ubVxldxDVN9CfvLesKW_4qOJU() {
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final void addMods(List list, ModLoaderReporter modLoaderReporter) {
        ModLoader.lambda$loadModsAndSetupEnvViaNewModLoader$1(list, modLoaderReporter);
    }
}
