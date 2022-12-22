package com.zhekasmirnov.horizon.util;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {
    public static String getClassStructureString(Class<?> cls, Object obj, String str, boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(cls.toString());
        sb.append("\n");
        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                sb.append(str);
                sb.append("  ");
                sb.append(field);
                sb.append(" = ");
                sb.append(field.get(obj));
                sb.append("\n");
            } catch (IllegalAccessException e) {
                sb.append(e);
                sb.append("\n");
            } catch (NullPointerException e2) {
            }
        }
        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            sb.append(str);
            sb.append("  ");
            sb.append(constructor);
            sb.append("\n");
        }
        for (Method method : cls.getDeclaredMethods()) {
            sb.append(str);
            sb.append("  ");
            sb.append(method);
            sb.append("\n");
        }
        if (z && cls.getSuperclass() != Object.class && cls.getSuperclass() != null) {
            sb.append("\n");
            sb.append(getClassStructureString(cls.getSuperclass(), obj, str, z));
        }
        return sb.toString();
    }

    public static void printClassStructure(Class<?> cls, Object obj, String str, String str2, boolean z) {
        String[] split = getClassStructureString(cls, obj, str2, z).split("\n");
        for (String str3 : split) {
            Logger.info(str, str3);
        }
    }

    public static Method getDeclaredMethod(Class<?> cls, String str, Class<?>... clsArr) {
        try {
            return cls.getDeclaredMethod(str, clsArr);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Object invokeMethod(Object obj, Class<?> cls, String str, Class<?>[] clsArr, Object[] objArr) throws NoSuchMethodException {
        try {
            return cls.getDeclaredMethod(str, clsArr).invoke(obj, objArr);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
            throw new RuntimeException(e2);
        }
    }
}
