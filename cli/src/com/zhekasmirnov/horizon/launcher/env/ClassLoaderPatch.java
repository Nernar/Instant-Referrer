package com.zhekasmirnov.horizon.launcher.env;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.googlecode.d2j.dex.Dex2jar;
import com.zhekasmirnov.horizon.runtime.logger.CoreLogger;

public class ClassLoaderPatch {
    private static final List<Object> patchedObjects = new ArrayList<>();

    public static Object newGenericArrayOfType(Class<?> cls, int i) {
        return Array.newInstance(cls, i);
    }

    public static synchronized void addNativeLibraryPath(ClassLoader classLoader, File file) {
        try {
            String currentlyPathes = System.getProperty("java.library.path", "");
            String libraryPath = file.getAbsolutePath();
            String[] pathes = currentlyPathes.split(File.pathSeparator);
            for (String path : pathes) {
                if (path == libraryPath || path + "/" == libraryPath || path == libraryPath + "/") {
                    return;
                }
            }
            if (currentlyPathes.length() == 0) {
                currentlyPathes = "" + libraryPath;
            } else {
                currentlyPathes += File.pathSeparator + libraryPath;
            }
            System.setProperty("java.library.path", currentlyPathes);
        } catch (RuntimeException e) {
            throw new RuntimeException("failed to patch classloader with following error", e);
        }
    }

    public static synchronized void addClasspath(ClassLoader classLoader, File file) {
        try {
            try {
                Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, file.toURI().toURL());
            } catch (NoSuchMethodException e) {
                Method method = classLoader.getClass()
                    .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
                method.setAccessible(true);
                method.invoke(classLoader, file.getAbsolutePath());
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | RuntimeException e) {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            if (classLoader != systemClassLoader) {
                addClasspath(systemClassLoader, file);
                return;
            }
            throw new RuntimeException("failed to patch classloader with following error", e);
        } catch (MalformedURLException e) {
            throw new RuntimeException("failed to patch classloader with following error", e);
        }
    }

    public static synchronized void addDexPath(ClassLoader classLoader, File file) {
        try {
            File jarFile = new File(file.getAbsolutePath().replace(".dex", "") + ".jar");
            if (!jarFile.isFile()) {
                Dex2jar dex2jar = Dex2jar.from(file);
                dex2jar.to(jarFile.toPath());
            }
            addClasspath(classLoader, jarFile);
            return;
        } catch (IOException e) {
            CoreLogger.log("ClassLoaderPath", e);
            CoreLogger.log("ClassLoaderPath", "Dex2jar not completed due to IOException, maybe classpath will be modified later");
        }
        addClasspath(classLoader, file);
    }

    @SuppressWarnings("unused")
    private static Object removePatchesFromGenericArray(Object[] objArr, Class<?> cls) {
        ArrayList<Object> arrayList = new ArrayList<>();
        int i = 0;
        for (Object obj : objArr) {
            if (!patchedObjects.contains(obj)) {
                arrayList.add(obj);
            }
        }
        Object newGenericArrayOfType = newGenericArrayOfType(cls, arrayList.size());
        Iterator<Object> it = arrayList.iterator();
        while (it.hasNext()) {
            Object obj2 = it.next();
            ((Object[]) newGenericArrayOfType)[i] = obj2;
            i++;
        }
        return newGenericArrayOfType;
    }

    public static synchronized void revertClassLoaderPatches(ClassLoader classLoader) {
        throw new UnsupportedOperationException();
    }
}
