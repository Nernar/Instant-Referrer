package com.zhekasmirnov.horizon.modloader.java;

import com.zhekasmirnov.horizon.launcher.env.ClassLoaderPatch;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JavaLibrary {
    private final List<File> dexFiles;
    private final JavaDirectory directory;
    private boolean initialized = false;

    public JavaLibrary(JavaDirectory javaDirectory, File file) {
        this.directory = javaDirectory;
        ArrayList<File> arrayList = new ArrayList<>(1);
        this.dexFiles = arrayList;
        arrayList.add(file);
    }

    public JavaLibrary(JavaDirectory javaDirectory, List<File> list) {
        this.directory = javaDirectory;
        this.dexFiles = list;
    }

    public JavaDirectory getDirectory() {
        return this.directory;
    }

    public List<File> getDexFiles() {
        return this.dexFiles;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public void initialize() {
        for (File file : this.dexFiles) {
            ClassLoaderPatch.addDexPath(JavaLibrary.class.getClassLoader(), file);
        }
        HashMap<String, String> hashMap = new HashMap<>();
        for (String str : this.directory.getBootClassNames()) {
            try {
                Method method = Class.forName(str).getMethod("boot", HashMap.class);
                hashMap.put("class_name", str);
                method.invoke(null, new HashMap<>(hashMap));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("failed to find boot class " + str + " in " + this.directory, e);
            } catch (IllegalAccessException e2) {
                throw new RuntimeException("failed to access boot method class " + str + " of " + this.directory, e2);
            } catch (NoSuchMethodException e3) {
                throw new RuntimeException("failed to find boot(HashMap) method in boot class " + str + " of " + this.directory, e3);
            } catch (InvocationTargetException e4) {
                throw new RuntimeException("failed to call boot method in class " + str + " of " + this.directory, e4);
            }
        }
        this.initialized = true;
    }
}
