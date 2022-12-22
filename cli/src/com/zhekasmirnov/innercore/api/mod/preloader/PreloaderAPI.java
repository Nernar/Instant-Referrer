package com.zhekasmirnov.innercore.api.mod.preloader;

import com.zhekasmirnov.innercore.api.annotations.APIStaticModule;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.mcpe161.InnerCore;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.annotations.JSStaticFunction;

public class PreloaderAPI extends API {
    @Override
    public String getName() {
        return "Preloader";
    }

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public void onLoaded() {
    }

    @Override
    public void onModLoaded(Mod mod) {
    }

    @Override
    public void onCallback(String name, Object[] args) {
    }

    @Override
    public void setupCallbacks(Executable executable) {
    }

    @JSStaticFunction
    public static void log(String str) {
        ICLog.d("PRELOADER", str);
    }

    @JSStaticFunction
    public static void print(String str) {
        ICLog.d("PRELOADER-PRINT", str);
    }

    @APIStaticModule
    public static class Resources extends AdaptedScriptAPI.Resources {
        @Deprecated
        @JSStaticFunction
        public static void __placeholder() {
        }

        @JSStaticFunction
        public static String addRuntimePack(String typeStr, String name) {
            ModLoader.MinecraftPackType type = ModLoader.MinecraftPackType.fromString(typeStr);
            return ModLoader.instance.addRuntimePack(type, name).getAbsolutePath();
        }

        @JSStaticFunction
        public static NativeArray getAllResourceDirectories() {
            return ScriptableObjectHelper.createArray(InnerCore.getInstance().allResourceDirectories.toArray());
        }

        @JSStaticFunction
        public static NativeArray getAllResourceDirectoriesPaths() {
            List<String> result = new ArrayList<>();
            for (File dir : InnerCore.getInstance().allResourceDirectories) {
                result.add(dir.getAbsolutePath());
            }
            return ScriptableObjectHelper.createArray(result.toArray());
        }

        private static void searchFilesInDir(Collection<String> result, File baseDir, File file, String regex) {
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    searchFilesInDir(result, baseDir, child, regex);
                }
                return;
            }
            String path = file.getAbsolutePath().substring(baseDir.getAbsolutePath().length() + 1);
            if (path.matches(regex)) {
                result.add(file.getAbsolutePath());
            }
        }

        @JSStaticFunction
        public static NativeArray getAllMatchingResourcesInDir(Object _directory, String regex) {
            File directory = (File) Context.jsToJava(_directory, File.class);
            HashSet<String> result = new HashSet<>();
            searchFilesInDir(result, directory, directory, regex);
            return ScriptableObjectHelper.createArray(result.toArray());
        }

        @JSStaticFunction
        public static NativeArray getAllMatchingResourcesInPath(String _directory, String regex) {
            File directory = new File(_directory);
            HashSet<String> result = new HashSet<>();
            searchFilesInDir(result, directory, directory, regex);
            return ScriptableObjectHelper.createArray(result.toArray());
        }

        @JSStaticFunction
        public static NativeArray getAllMatchingResources(String regex) {
            HashSet<String> result = new HashSet<>();
            for (File dir : InnerCore.getInstance().allResourceDirectories) {
                searchFilesInDir(result, dir, dir, regex);
            }
            return ScriptableObjectHelper.createArray(result.toArray());
        }

        private static File getResourcePathNoVariants(String path) {
            InnerCore innerCore = InnerCore.getInstance();
            for (File dir : innerCore.allResourceDirectories) {
                File file = new File(dir, path);
                if (file.isFile()) {
                    return file;
                }
            }
            File assetsDir = new File(innerCore.getWorkingDirectory(), "assets");
            File file2 = new File(assetsDir, path);
            if (file2.isFile()) {
                return file2;
            }
            File vanillaResourcesDir = new File(assetsDir, "resource_packs/vanilla");
            File file3 = new File(vanillaResourcesDir, path);
            if (file3.isFile()) {
                return file3;
            }
            return null;
        }

        @JSStaticFunction
        public static String getResourcePath(String path) {
            File file = getResourcePathNoVariants(path);
            if (file != null) {
                return file.getAbsolutePath();
            }
            File file2 = getResourcePathNoVariants(String.valueOf(path) + ".png");
            if (file2 != null) {
                return file2.getAbsolutePath();
            }
            File file3 = getResourcePathNoVariants(String.valueOf(path) + ".tga");
            if (file3 != null) {
                return file3.getAbsolutePath();
            }
            return null;
        }
    }

    @APIStaticModule
    public static class Callback {
        @JSStaticFunction
        public static void addCallback(String name, Function func, int priority) {
            com.zhekasmirnov.innercore.api.runtime.Callback.addCallback(name, func, priority);
        }

        @JSStaticFunction
        public static void invokeCallback(String name, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10) {
            com.zhekasmirnov.innercore.api.runtime.Callback.invokeCallback(name, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10);
        }
    }

    @APIStaticModule
    public static class Textures {
        @Deprecated
        @JSStaticFunction
        public static void __placeholder() {
        }
    }
}
