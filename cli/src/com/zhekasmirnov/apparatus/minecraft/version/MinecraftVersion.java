package com.zhekasmirnov.apparatus.minecraft.version;

import com.zhekasmirnov.apparatus.minecraft.addon.AddonContext;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class MinecraftVersion {
    public static final String FEATURE_ACTOR_RENDER_OVERRIDE = "actor_render_override";
    public static final String FEATURE_ATTACHABLE_RENDER = "attachable_render";
    public static final String FEATURE_GLOBAL_SHADER_UNIFORM_SET = "global_shader_uniform_set";
    public static final String FEATURE_VANILLA_ID_MAPPING = "vanilla_id_mapping";
    public static final String FEATURE_VANILLA_WORLD_GENERATION_LEVELS = "vanilla_world_generation_levels";
    private final int code;
    private final boolean isBeta;
    private final String name;
    private final Set<String> supportedFeatures = new HashSet<>();

    public abstract void addSupportedFeatures(Set<String> set);

    public abstract AddonContext createAddonContext();

    public abstract JSONObject createRuntimePackManifest(String str, String str2, String str3, JSONArray jSONArray);

    public abstract File getMinecraftExternalStoragePath();

    public abstract String[] getVanillaBehaviorPacksDirs();

    public abstract String[] getVanillaResourcePacksDirs();

    protected MinecraftVersion(String name, int code, boolean isBeta) {
        this.name = name;
        this.code = code;
        this.isBeta = isBeta;
        addSupportedFeatures(this.supportedFeatures);
    }

    public String getName() {
        return this.name;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isBeta() {
        return this.isBeta;
    }

    public String getMainVanillaResourcePack() {
        return getVanillaResourcePacksDirs()[0];
    }

    public String getMainVanillaBehaviorPack() {
        return getVanillaBehaviorPacksDirs()[0];
    }

    public Set<String> getSupportedFeatures() {
        return this.supportedFeatures;
    }

    public boolean isFeatureSupported(String feature) {
        return this.supportedFeatures.contains(feature);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MinecraftVersion that = (MinecraftVersion) o;
        return this.code == that.code && Java8BackComp.equals(this.name, that.name);
    }

    public int hashCode() {
        return Java8BackComp.hash(Integer.valueOf(this.code), this.name);
    }
}
