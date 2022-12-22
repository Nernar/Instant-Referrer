package com.zhekasmirnov.apparatus.adapter.innercore.game.block;

import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mozilla.javascript.ScriptableObject;

public class BlockBreakResult {
    private int experience;
    private final List<ItemStack> items = new ArrayList<>();

    public List<ItemStack> getItems() {
        return this.items;
    }

    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void addExperience(int experience) {
        this.experience += experience;
    }

    public ScriptableObject asScriptable() {
        ScriptableObject result = ScriptableObjectHelper.createEmpty();
        result.put("experience", result, Integer.valueOf(this.experience));
        result.put("items", result, ScriptableObjectHelper.createArray(Java8BackComp.stream(this.items).map(C$$Lambda$Kpkvth9XbAy56gKaJDODmfpIQ10.INSTANCE).collect(Collectors.toList())));
        return result;
    }
}
