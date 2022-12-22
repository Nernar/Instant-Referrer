package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.nbt.NbtDataType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import org.mozilla.javascript.ScriptableObject;

public class ConstantRegistry {
    private static ArrayList<Class<?>> constantClasses = new ArrayList<>();

    static {
        registerClass(ArmorType.class);
        registerClass(BlockFace.class);
        registerClass(BlockRenderLayer.class);
        registerClass(ChatColor.class);
        registerClass(DimensionId.class);
        registerClass(Enchantment.class);
        registerClass(EnchantType.class);
        registerClass(EntityRenderType.class);
        registerClass(EntityType.class);
        registerClass(ItemCategory.class);
        registerClass(GameDifficulty.class);
        registerClass(GameMode.class);
        registerClass(MobEffect.class);
        registerClass(ParticleType.class);
        registerClass(UseAnimation.class);
        registerClass(PlayerAbility.class);
        registerClass(TileEntityType.class);
        registerClass(NbtDataType.class);
    }

    public static void registerClass(Class<?> clazz) {
        if (!constantClasses.contains(clazz)) {
            constantClasses.add(clazz);
        }
    }

    public static void injectConstants(ScriptableObject scope) {
        Iterator<Class<?>> it = constantClasses.iterator();
        while (it.hasNext()) {
            Class<?> clazz = it.next();
            Field[] fields = clazz.getFields();
            ScriptableObject constantScope = ScriptableObjectHelper.createEmpty();
            scope.put(clazz.getSimpleName(), scope, constantScope);
            for (Field field : fields) {
                try {
                    constantScope.put(field.getName(), constantScope, field.get(null));
                } catch (IllegalAccessException e) {
                    ICLog.e("API", "failed to inject constant " + clazz.getSimpleName() + "." + field.getName(), e);
                }
            }
        }
    }
}
