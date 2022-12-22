package com.zhekasmirnov.innercore.modpack.strategy.extract;

import java.io.File;
import java.util.function.Predicate;

public final class C$$Lambda$ConfigDirectoryExtractStrategy$nCTTs9UwvBynBjowc5uZMa0 implements Predicate<File> {
    public static final C$$Lambda$ConfigDirectoryExtractStrategy$nCTTs9UwvBynBjowc5uZMa0 INSTANCE = new C$$Lambda$ConfigDirectoryExtractStrategy$nCTTs9UwvBynBjowc5uZMa0();

    private C$$Lambda$ConfigDirectoryExtractStrategy$nCTTs9UwvBynBjowc5uZMa0() {
    }

    @Override
    public final boolean test(File obj) {
        return ConfigDirectoryExtractStrategy.lambda$getFilesToExtract$0(obj);
    }
}
