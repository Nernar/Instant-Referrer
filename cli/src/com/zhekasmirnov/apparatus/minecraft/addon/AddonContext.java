package com.zhekasmirnov.apparatus.minecraft.addon;

import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersion;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;

public class AddonContext {
    private static final AddonContext instance = MinecraftVersions.getCurrent().createAddonContext();
    private final AddonRecipeParser recipeParser;
    private final MinecraftVersion version;

    public static AddonContext getInstance() {
        return instance;
    }

    public AddonContext(MinecraftVersion version, AddonRecipeParser recipeParser) {
        this.version = version;
        this.recipeParser = recipeParser;
    }

    public MinecraftVersion getVersion() {
        return this.version;
    }

    public AddonRecipeParser getRecipeParser() {
        return this.recipeParser;
    }
}
