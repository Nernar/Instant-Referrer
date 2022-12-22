package com.zhekasmirnov.apparatus.util;

import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.mcpe161.InnerCore;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HorizonPackUtils {
    private static final List<Class<?>> allClasses = new ArrayList<>();

    public static Pack getPack() {
        return InnerCore.getInstance().getPack();
    }

    public static List<File> getAllDexFiles() {
        List<File> result = new ArrayList<>();
        try {
            result.addAll(new JavaDirectory(null, new File(getPack().directory, "java/innercore")).getCompiledClassesFiles());
        } catch (Exception e) {
        }
        for (File file : InnerCore.getJavaDirectoriesFromProxy()) {
            result.addAll(new JavaDirectory(null, file).getCompiledClassesFiles());
        }
        return result;
    }

    private static void rebuildClassCacheIfRequired() {
        throw new UnsupportedOperationException();
    }

    public static List<Class<?>> getAllClasses(Predicate<Class<?>> filter) {
        rebuildClassCacheIfRequired();
        synchronized (allClasses) {
            return Java8BackComp.stream(allClasses).filter(filter).collect(Collectors.toList());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Class<? extends T>> getAll(Class<T> type) {
        rebuildClassCacheIfRequired();
        synchronized (allClasses) {
            ArrayList<Class<? extends T>> arrayList = new ArrayList<>();
            for (Class<?> clazz : allClasses) {
                if (type.isAssignableFrom(clazz)) {
                    arrayList.add((Class<? extends T>) clazz);
                }
            }
            return arrayList;
        }
    }
}
