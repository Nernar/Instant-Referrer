package com.zhekasmirnov.innercore.mod.build.enums;

public enum AnalyzedModType {
    INNER_CORE_MOD,
    CORE_ENGINE_MOD,
    MODPE_MOD_ARRAY,
    RESOUCE_PACK,
    UNKNOWN;

    public static AnalyzedModType[] valuesCustom() {
        AnalyzedModType[] valuesCustom = values();
        int length = valuesCustom.length;
        AnalyzedModType[] analyzedModTypeArr = new AnalyzedModType[length];
        System.arraycopy(valuesCustom, 0, analyzedModTypeArr, 0, length);
        return analyzedModTypeArr;
    }
}
