package com.zhekasmirnov.apparatus.util;

import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.mod.Mod;
import com.zhekasmirnov.mcpe161.InnerCore;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HorizonPackUtils {
	private static final List<Class<?>> allClasses = new ArrayList<>();
	
	public static Pack getPack() {
		try {
			InnerCore instance = InnerCore.getInstance();
			return (Pack) instance.getClass().getMethod("getPack").invoke(instance);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	public static List<File> getAllDexFiles() {
		List<File> result = new ArrayList<>();
		try {
			result.addAll(new JavaDirectory((Mod) null, new File(getPack().directory, "java/instant")).getCompiledClassesFiles());
		} catch (Exception e) {}
		try {
			result.addAll(new JavaDirectory((Mod) null, new File(getPack().directory, "java/innercore")).getCompiledClassesFiles());
		} catch (Exception e) {}
		try {
			Method getJavaDirectoriesFromProxy = InnerCore.class.getMethod("getJavaDirectoriesFromProxy");
			for (File file : (File[]) getJavaDirectoriesFromProxy.invoke(null)) {
				result.addAll(new JavaDirectory((Mod) null, file).getCompiledClassesFiles());
			}
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new UnsupportedOperationException(e);
		}
		return result;
	}
	
	private static void rebuildClassCacheIfRequired() {
		synchronized (allClasses) {
			if (allClasses.isEmpty()) {
				for (File file : getAllDexFiles()) {
					try {
						Enumeration<String> entries = new DexFile(file).entries();
						while (entries.hasMoreElements()) {
							allClasses.add(Class.forName(entries.nextElement()));
						}
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static List<Class<?>> getAllClasses(Predicate<Class<?>> filter) {
		rebuildClassCacheIfRequired();
		synchronized (allClasses) {
			return (List) Java8BackComp.stream(allClasses).filter(filter).collect(Collectors.toList());
		}
	}
	
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
