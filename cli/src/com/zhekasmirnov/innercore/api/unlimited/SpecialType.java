package com.zhekasmirnov.innercore.api.unlimited;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import java.util.HashMap;
import org.mozilla.javascript.ScriptableObject;

public class SpecialType {
    @SuppressWarnings("unused")
    private static final String NONE_NAME = "__none__";
    public final String name;
    private static HashMap<String, SpecialType> specialTypeByName = new HashMap<>();
    public static final SpecialType NONE = getSpecialType("__none__");
    public static final SpecialType DEFAULT = getSpecialType("__default__");
    public static final SpecialType OPAQUE = createSpecialType("opaque");
    private boolean isApproved = false;
    public String sound = "";
    public int material = 3;
    public int base = 0;
    public int rendertype = 0;
    public int renderlayer = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "alpha"));
    public int lightlevel = 0;
    public int lightopacity = 0;
    public float explosionres = 3.0f;
    public float destroytime = 1.0f;
    public float friction = 0.6f;
    public float translucency = 1.0f;
    public boolean solid = false;
    public boolean can_contain_liquid = false;
    public boolean can_be_extra_block = false;
    public boolean renderallfaces = false;
    public int mapcolor = 0;
    public String color_source = "";

    static {
        OPAQUE.solid = true;
        OPAQUE.base = 1;
        OPAQUE.lightopacity = 15;
        OPAQUE.explosionres = 4.0f;
        OPAQUE.renderlayer = 2;
        OPAQUE.translucency = 0.0f;
        OPAQUE.sound = "stone";
        OPAQUE.approve();
    }

    enum BlockColorSource {
        NONE(0),
        LEAVES(1),
        GRASS(2),
        WATER(3);
        
        public final int id;

        public static BlockColorSource[] valuesCustom() {
            BlockColorSource[] valuesCustom = values();
            int length = valuesCustom.length;
            BlockColorSource[] blockColorSourceArr = new BlockColorSource[length];
            System.arraycopy(valuesCustom, 0, blockColorSourceArr, 0, length);
            return blockColorSourceArr;
        }

        BlockColorSource(int id) {
            this.id = id;
        }
    }

    public SpecialType approve() {
        this.isApproved = true;
        return this;
    }

    public boolean isApproved() {
        return this.isApproved;
    }

    public SpecialType(String name) {
        this.name = name;
    }

    public static SpecialType getSpecialType(String name) {
        if (specialTypeByName.containsKey(name)) {
            return specialTypeByName.get(name);
        }
        SpecialType type = new SpecialType(name);
        specialTypeByName.put(name, type);
        return type;
    }

    public static SpecialType createSpecialType(String name) {
        SpecialType type = getSpecialType(name);
        if (!type.equals(DEFAULT)) {
            type.approve();
        }
        return type;
    }

    public boolean equals(Object obj) {
        if (obj instanceof SpecialType) {
            SpecialType type = (SpecialType) obj;
            if (this.name.equals("__none__") || type.name.equals("__none__")) {
                return true;
            }
            return type.name.equals(this.name);
        }
        return super.equals(obj);
    }

    public void setupBlock(int id) {
        throw new UnsupportedOperationException();
    }

    public void setupProperties(ScriptableObject properties) {
        if (properties != null) {
            this.base = ScriptableObjectHelper.getIntProperty(properties, "base", this.base);
            this.material = ScriptableObjectHelper.getIntProperty(properties, "material", this.material);
            this.sound = ScriptableObjectHelper.getStringProperty(properties, "sound", this.sound);
            this.solid = ScriptableObjectHelper.getBooleanProperty(properties, "solid", this.solid);
            this.can_contain_liquid = ScriptableObjectHelper.getBooleanProperty(properties, "can_contain_liquid", this.can_contain_liquid);
            this.can_be_extra_block = ScriptableObjectHelper.getBooleanProperty(properties, "can_be_extra_block", this.can_be_extra_block);
            this.renderallfaces = ScriptableObjectHelper.getBooleanProperty(properties, "renderallfaces", this.renderallfaces);
            this.rendertype = ScriptableObjectHelper.getIntProperty(properties, "rendertype", this.rendertype);
            this.renderlayer = GameEnums.getSingleton().getIntEnumOrConvertFromLegacyVersion("block_render_layer", ScriptableObjectHelper.getProperty(properties, "renderlayer", null), this.renderlayer, MinecraftVersions.MINECRAFT_1_11_4);
            this.lightlevel = ScriptableObjectHelper.getIntProperty(properties, "lightlevel", this.lightlevel);
            this.lightopacity = ScriptableObjectHelper.getIntProperty(properties, "lightopacity", this.lightopacity);
            this.mapcolor = ScriptableObjectHelper.getIntProperty(properties, "mapcolor", this.mapcolor);
            this.explosionres = ScriptableObjectHelper.getFloatProperty(properties, "explosionres", this.explosionres);
            this.friction = ScriptableObjectHelper.getFloatProperty(properties, "friction", this.friction);
            this.destroytime = ScriptableObjectHelper.getFloatProperty(properties, "destroytime", this.destroytime);
            this.translucency = ScriptableObjectHelper.getFloatProperty(properties, "translucency", this.translucency);
            this.color_source = ScriptableObjectHelper.getStringProperty(properties, "color_source", this.color_source);
        }
    }
}
