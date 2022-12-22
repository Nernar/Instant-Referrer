package com.zhekasmirnov.innercore.modpack.strategy.extract;

import java.io.File;
import java.util.function.Predicate;

public final class C$$Lambda$ModsWithoutConfigExtractStrategy$qqnuqbFL3_OxQOYN52aLAQ139oI implements Predicate<File> {
    public static final C$$Lambda$ModsWithoutConfigExtractStrategy$qqnuqbFL3_OxQOYN52aLAQ139oI INSTANCE = new C$$Lambda$ModsWithoutConfigExtractStrategy$qqnuqbFL3_OxQOYN52aLAQ139oI();

    private C$$Lambda$ModsWithoutConfigExtractStrategy$qqnuqbFL3_OxQOYN52aLAQ139oI() {
    }

    @Override
    public final boolean test(File obj) {
        return ModsWithoutConfigExtractStrategy.lambda$getFilesToExtract$0(obj);
    }
}
