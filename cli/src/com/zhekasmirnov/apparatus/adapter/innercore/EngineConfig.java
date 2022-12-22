package com.zhekasmirnov.apparatus.adapter.innercore;

import com.zhekasmirnov.innercore.api.InnerCoreConfig;

public class EngineConfig {

    public interface PropertyValidator<T> {
        T validate(T t);
    }

    public static void reload() {
        InnerCoreConfig.reload();
    }

    public static boolean isDeveloperMode() {
        return InnerCoreConfig.getBool("developer_mode");
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String name, Class<T> type, PropertyValidator<T> validator) {
        T t = (T) InnerCoreConfig.get(name);
        if (validator != null) {
            try {
                return validator.validate(t);
            } catch (ClassCastException e) {
                if (validator != null) {
                    return validator.validate(null);
                }
            }
        }
        return t;
    }

    public static boolean getBoolean(String name, final boolean fallback) {
        return ((Boolean) get(name, Boolean.class, new PropertyValidator<Boolean>() {
            @Override
            public final Boolean validate(Boolean obj) {
                return Boolean.valueOf(obj != null ? obj.booleanValue() : fallback);
            }
        })).booleanValue();
    }

    public static Number getNumber(String name, final Number fallback) {
        return (Number) get(name, Number.class, new PropertyValidator<Number>() {
            @Override
            public final Number validate(Number obj) {
                return EngineConfig.lambda$getNumber$1(fallback, obj);
            }
        });
    }

    static Number lambda$getNumber$1(Number fallback, Number value) {
        return value != null ? value : fallback;
    }

    public static int getInt(String name, int fallback) {
        return getNumber(name, Integer.valueOf(fallback)).intValue();
    }

    public static float getFloat(String name, float fallback) {
        return getNumber(name, Float.valueOf(fallback)).floatValue();
    }

    public static double getDouble(String name, double fallback) {
        return getNumber(name, Double.valueOf(fallback)).doubleValue();
    }

    public static long getLong(String name, long fallback) {
        return getNumber(name, Long.valueOf(fallback)).longValue();
    }
}
