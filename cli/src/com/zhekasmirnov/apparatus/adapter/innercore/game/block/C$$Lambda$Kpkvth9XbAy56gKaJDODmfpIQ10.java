package com.zhekasmirnov.apparatus.adapter.innercore.game.block;

import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import java.util.function.Function;
import org.mozilla.javascript.ScriptableObject;

public final class C$$Lambda$Kpkvth9XbAy56gKaJDODmfpIQ10 implements Function<ItemStack, ScriptableObject> {
    public static final C$$Lambda$Kpkvth9XbAy56gKaJDODmfpIQ10 INSTANCE = new C$$Lambda$Kpkvth9XbAy56gKaJDODmfpIQ10();

    private C$$Lambda$Kpkvth9XbAy56gKaJDODmfpIQ10() {
    }

    @Override
    public final ScriptableObject apply(ItemStack obj) {
        return obj.asScriptable();
    }
}
