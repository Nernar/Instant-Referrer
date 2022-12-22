package com.zhekasmirnov.apparatus.minecraft.version;

import java.util.Map;
import java.util.function.Function;

public final class C$$Lambda$VanillaIdConversionMap$nVjPDSo0oHtc8aR6oRcKKe7Bf8 implements Function<String, Map<String, Integer>> {
    public static final C$$Lambda$VanillaIdConversionMap$nVjPDSo0oHtc8aR6oRcKKe7Bf8 INSTANCE = new C$$Lambda$VanillaIdConversionMap$nVjPDSo0oHtc8aR6oRcKKe7Bf8();

    private C$$Lambda$VanillaIdConversionMap$nVjPDSo0oHtc8aR6oRcKKe7Bf8() {
    }

    @Override
    public final Map<String, Integer> apply(String obj) {
        return VanillaIdConversionMap.lambda$loadJsonIntoMap$0(obj);
    }
}
