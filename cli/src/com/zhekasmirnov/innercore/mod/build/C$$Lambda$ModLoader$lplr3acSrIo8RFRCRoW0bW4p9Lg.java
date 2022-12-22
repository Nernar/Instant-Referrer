package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.apparatus.modloader.ModLoaderReporter;
import com.zhekasmirnov.innercore.api.log.ICLog;

public final class C$$Lambda$ModLoader$lplr3acSrIo8RFRCRoW0bW4p9Lg implements ModLoaderReporter {
    public static final C$$Lambda$ModLoader$lplr3acSrIo8RFRCRoW0bW4p9Lg INSTANCE = new C$$Lambda$ModLoader$lplr3acSrIo8RFRCRoW0bW4p9Lg();

    private C$$Lambda$ModLoader$lplr3acSrIo8RFRCRoW0bW4p9Lg() {
    }

    @Override
    public final void reportError(String str, Throwable th) {
        ICLog.e("ERROR", str, th);
    }
}
