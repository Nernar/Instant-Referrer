package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.innercore.api.runtime.saver.serializer.ScriptableSerializer;

public final class C$$Lambda$ScriptableSaverScope$SKsTsK8MFXdC42bmQt3ZeMrD8mA implements ScriptableSerializer.SerializationErrorHandler {
    public static final C$$Lambda$ScriptableSaverScope$SKsTsK8MFXdC42bmQt3ZeMrD8mA INSTANCE = new C$$Lambda$ScriptableSaverScope$SKsTsK8MFXdC42bmQt3ZeMrD8mA();

    private C$$Lambda$ScriptableSaverScope$SKsTsK8MFXdC42bmQt3ZeMrD8mA() {
    }

    @Override
    public final void handle(Exception exc) {
        WorldDataSaver.logErrorStatic("error in serializer", exc);
    }
}
