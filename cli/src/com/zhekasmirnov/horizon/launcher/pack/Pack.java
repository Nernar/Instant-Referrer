package com.zhekasmirnov.horizon.launcher.pack;

import com.zhekasmirnov.horizon.compiler.holder.CompilerHolder;
import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.launcher.ContextHolder;
import com.zhekasmirnov.horizon.launcher.env.AssetPatch;
import com.zhekasmirnov.horizon.launcher.env.ClassLoaderPatch;
import com.zhekasmirnov.horizon.modloader.ExecutionDirectory;
import com.zhekasmirnov.horizon.modloader.ModContext;
import com.zhekasmirnov.horizon.modloader.java.JavaCompilerHolder;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.java.JavaLibrary;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.modloader.mod.Mod;
import com.zhekasmirnov.horizon.modloader.repo.ModList;
import com.zhekasmirnov.horizon.modloader.repo.storage.DirectoryRepository;
import com.zhekasmirnov.horizon.modloader.repo.storage.ModRepository;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.ResourceDirectory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.ReflectionHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;

public class Pack {
    public final ContextHolder contextHolder;
    public final File directory;
    public final PackManifest manifest;
    public ModContext modContext;
    public ModList modList;
    public ModRepository modRepository;
    public ResourceManager resourceManager;
    private List<File> executableSoFiles = new ArrayList<>();
    private List<JavaLibrary> bootJavaLibraries = new ArrayList<>();
    private List<LibraryDirectory> bootNativeLibraries = new ArrayList<>();
    private final List<MenuActivityFactory> menuActivityFactories = new ArrayList<>();
    private boolean isLaunchAborted = false;

    public Pack(ContextHolder contextHolder, File file) {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("File is not a directory " + file);
        }
        this.directory = file;
        File file2 = new File(this.directory, "manifest");
        if (!file2.exists()) {
            file2 = new File(this.directory, "manifest.json");
            if (!file2.exists()) {
                throw new IllegalArgumentException("pack missing manifest.json: " + file);
            }
        }
        try {
            this.manifest = new PackManifest(file2);
            this.contextHolder = contextHolder;
        } catch (IOException e) {
            throw new RuntimeException("failed to read pack manifest for: " + file, e);
        } catch (JSONException e2) {
            throw new RuntimeException("failed to read pack manifest for: " + file, e2);
        }
    }

    public File getWorkingDirectory() {
        return this.directory;
    }

    public ModContext getModContext() {
        return this.modContext;
    }

    public ContextHolder getContextHolder() {
        return this.contextHolder;
    }

    public ModList getModList() {
        return this.modList;
    }

    public ModRepository getModRepository() {
        return this.modRepository;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    private List<String> loadOrderFile(File file) {
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        try {
            ArrayList<String> arrayList = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                    }
                    break;
                } else {
                    String trim = readLine.trim();
                    if (trim.length() > 0) {
                        arrayList.add(trim);
                    }
                }
            }
            return arrayList;
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }

    private boolean initializeSharedObjects() {
        File file = new File(this.directory, "so");
        if (file.exists() && file.isDirectory()) {
            File packExecutionDir = Environment.getPackExecutionDir(this.contextHolder.getContext());
            if (packExecutionDir.exists()) {
                if (packExecutionDir.isDirectory()) {
                    FileUtils.clearFileTree(packExecutionDir, false);
                }
                packExecutionDir.delete();
            }
            packExecutionDir.mkdirs();
            this.executableSoFiles.clear();
            List<String> loadOrderFile = loadOrderFile(new File(file, "order.txt"));
            ClassLoaderPatch.addNativeLibraryPath(getClass().getClassLoader(), packExecutionDir);
            for (String str : Environment.getSupportedABIs()) {
                File file2 = new File(file, str);
                if (file2.exists() && file2.isDirectory()) {
                    if (loadOrderFile == null) {
                        loadOrderFile = new ArrayList<>();
                        for (File file3 : file2.listFiles()) {
                            loadOrderFile.add(file3.getName());
                        }
                    }
                    for (String str2 : loadOrderFile) {
                        File file4 = new File(file2, str2);
                        if (!file4.exists() || file4.isDirectory()) {
                            Logger.debug("Pack", "invalid so file: " + str2 + " for " + str);
                        }
                        File file5 = new File(packExecutionDir, str2);
                        try {
                            FileUtils.copy(file4, file5);
                            this.executableSoFiles.add(file5);
                        } catch (IOException e) {
                            Logger.debug("Pack", "failed to unpack so file " + str2 + ": " + e.toString());
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private void initializeAssetsAndResources() {
        AssetPatch.setAssetDirectory(new File(this.directory, "assets").getAbsolutePath());
        this.resourceManager = new ResourceManager(this.contextHolder.getContext());
    }

    private void initializeModContext() {
        this.modContext = new ModContext(this.contextHolder.getContext(), this.resourceManager, this.contextHolder.getExecutionDir());
        this.modRepository = new DirectoryRepository(new File(this.directory, "native_mods"));
        ModList modList = new ModList(this.modContext, this.contextHolder.getTaskManager(), this.contextHolder.getTemporaryStorage());
        this.modList = modList;
        modList.addModRepository(this.modRepository);
        this.modContext.addEventReceiver("injectAll", new ModContext.EventReceiver() {
            @Override
            public void onEvent(Mod... modArr) {
                ExecutionDirectory executionDir = Pack.this.contextHolder.getExecutionDir();
                for (LibraryDirectory libraryDirectory : Pack.this.bootNativeLibraries) {
                    executionDir.addLibraryDirectory(libraryDirectory);
                }
                Pack pack = Pack.this;
                pack.invokeEnvironmentClassMethod("prepareForInjection", new Class[]{Pack.class}, new Object[]{pack}, false);
                ArrayList<LibraryDirectory> arrayList = new ArrayList<>();
                Pack.this.invokeEnvironmentClassMethod("getAdditionalNativeDirectories", new Class[]{Pack.class, arrayList.getClass()}, new Object[]{Pack.this, arrayList}, false);
                Iterator<LibraryDirectory> it = arrayList.iterator();
                while (it.hasNext()) {
                    LibraryDirectory libraryDirectory2 = it.next();
                    executionDir.addLibraryDirectory(libraryDirectory2);
                }
                ArrayList<JavaDirectory> arrayList2 = new ArrayList<>();
                Pack.this.invokeEnvironmentClassMethod("getAdditionalJavaDirectories", new Class[]{Pack.class, arrayList2.getClass()}, new Object[]{Pack.this, arrayList2}, false);
                Iterator<JavaDirectory> it2 = arrayList2.iterator();
                while (it2.hasNext()) {
                    JavaDirectory javaDirectory = it2.next();
                    executionDir.addJavaDirectory(javaDirectory);
                }
                ArrayList<ResourceDirectory> arrayList3 = new ArrayList<>();
                Pack.this.invokeEnvironmentClassMethod("getAdditionalResourceDirectories", new Class[]{Pack.class, arrayList3.getClass()}, new Object[]{Pack.this, arrayList3}, false);
                Iterator<ResourceDirectory> it3 = arrayList3.iterator();
                while (it3.hasNext()) {
                    ResourceDirectory resourceDirectory = it3.next();
                    Pack.this.resourceManager.addResourceDirectory(resourceDirectory);
                }
                Pack pack2 = Pack.this;
                pack2.invokeEnvironmentClassMethod("injectIntoModContext", new Class[]{Pack.class}, new Object[]{pack2}, false);
            }
        });
    }

    private void initializeBootJavaDirs() {
        JavaCompilerHolder.initializeForContext(this.modContext, this.contextHolder.getTaskManager());
        File file = new File(this.directory, "java");
        if (file.exists() && file.isDirectory()) {
            List<String> loadOrderFile = loadOrderFile(new File(file, "order.txt"));
            if (loadOrderFile == null) {
                loadOrderFile = new ArrayList<>();
                for (File file2 : file.listFiles()) {
                    if (file2.isDirectory()) {
                        loadOrderFile.add(file2.getName());
                    }
                }
            }
            this.bootJavaLibraries.clear();
            for (String str : loadOrderFile) {
                File file3 = new File(file, str);
                if (file3.isDirectory()) {
                    this.bootJavaLibraries.add(new JavaDirectory(null, file3).addToExecutionDirectory(null, this.contextHolder.getContext()));
                }
            }
        }
    }

    private void initializeNativeDirectories() {
        CompilerHolder.initializeForContext(this.contextHolder.getContext(), this.contextHolder.getTaskManager());
        File file = new File(this.directory, "native");
        if (file.exists() && file.isDirectory()) {
            this.bootNativeLibraries.clear();
            for (File file2 : file.listFiles()) {
                if (file2.isDirectory()) {
                    this.bootNativeLibraries.add(new LibraryDirectory(file2));
                }
            }
        }
    }

    public void invokeEnvironmentClassMethod(String str, Class<?>[] clsArr, Object[] objArr, boolean z) {
        ClassLoader classLoader = getClass().getClassLoader();
        for (PackManifest.ClassInfo classInfo : this.manifest.environmentClasses) {
            Class<?> declaredClass = classInfo.getDeclaredClass(classLoader);
            try {
                ReflectionHelper.invokeMethod(null, declaredClass, str, clsArr, objArr);
                Logger.debug("Pack", "environment class " + declaredClass + " called method " + str);
            } catch (NoSuchMethodException e) {
                if (z) {
                    throw new RuntimeException("environment class " + declaredClass + " missing required method " + str, e);
                }
            }
        }
    }

    public void initialize() {
        initializeAssetsAndResources();
        initializeModContext();
        initializeBootJavaDirs();
        initializeSharedObjects();
        initializeNativeDirectories();
        loadBootJavaLibraries();
        loadMenuActivityFactories();
        initializeAds();
    }

    private void loadBootJavaLibraries() {
        for (JavaLibrary javaLibrary : this.bootJavaLibraries) {
            javaLibrary.initialize();
        }
        ClassLoader classLoader = getClass().getClassLoader();
        for (PackManifest.ClassInfo classInfo : this.manifest.environmentClasses) {
            classInfo.getDeclaredClass(classLoader);
        }
        for (PackManifest.ClassInfo classInfo2 : this.manifest.activities) {
            classInfo2.getDeclaredClass(classLoader);
        }
    }

    private void loadSharedObjects() {
        for (File file : this.executableSoFiles) {
            String name = file.getName();
            if (name.startsWith("lib") && name.endsWith(".so")) {
                System.loadLibrary(name.substring(3, name.length() - 3));
            } else {
                System.load(file.getAbsolutePath());
            }
        }
    }

    private void loadResourceManager() {
        invokeEnvironmentClassMethod("setupResourceManager", new Class[]{ResourceManager.class}, new Object[]{this.resourceManager}, false);
    }

    private void loadNativeDirectories() {
        Object packExecutionDir = Environment.getPackExecutionDir(this.contextHolder.getContext());
        ArrayList<File> arrayList = new ArrayList<>();
        invokeEnvironmentClassMethod("addEnvironmentLibraries", new Class[]{arrayList.getClass(), File.class}, new Object[]{arrayList, packExecutionDir}, false);
        Iterator<File> it = arrayList.iterator();
        while (it.hasNext()) {
            File file = it.next();
            if (!file.exists()) {
                throw new RuntimeException("pack declared non-existing environment library: " + file.getAbsolutePath());
            }
            Logger.info("Pack", "added environment library: " + file.getName() + "  (" + file.getAbsolutePath() + ")");
        }
        CompilerHolder compilerHolder = CompilerHolder.getInstance(this.contextHolder.getContext());
        compilerHolder.clearEnvironmentLibraries();
        compilerHolder.addEnvironmentLibraries(arrayList);
    }

    private void loadMenuActivityFactories() {
        this.menuActivityFactories.clear();
        invokeEnvironmentClassMethod("addMenuActivities", new Class[]{Pack.class, this.menuActivityFactories.getClass()}, new Object[]{this, this.menuActivityFactories}, false);
        for (MenuActivityFactory menuActivityFactory : this.menuActivityFactories) {
            menuActivityFactory.pack = this;
        }
    }

    public void load() {
        loadResourceManager();
        loadSharedObjects();
        loadNativeDirectories();
    }

    public void unload() {
        ModContext modContext = this.modContext;
        if (modContext != null) {
            modContext.clearModsAndContext();
        }
        this.modContext = null;
        this.resourceManager = null;
    }

    private void initializeAds() {
        Object[] objArr = new Object[3];
        objArr[0] = this;
        invokeEnvironmentClassMethod("initializePackRelatedAds", new Class[]{Pack.class, Object.class, Object.class}, objArr, false);
    }

    public void abortLaunch() {
        this.isLaunchAborted = true;
    }

    public boolean abortIfRequired(Object activity, Runnable runnable) {
        if (this.isLaunchAborted) {
            if (runnable != null) {
                runnable.run();
                return true;
            }
            return true;
        }
        return false;
    }

    public void launch(final Object activity, final String str, final Runnable runnable) {
        File file = new File(this.directory, "assets");
        if (file.exists() && file.isDirectory()) {
            AssetPatch.setAssetDirectory(file.getAbsolutePath());
        } else {
            AssetPatch.setAssetDirectory(null);
        }
        this.isLaunchAborted = false;
        invokeEnvironmentClassMethod("abortLaunchIfRequired", new Class[]{Pack.class}, new Object[]{this}, false);
        if (abortIfRequired(activity, runnable)) {
            return;
        }
        this.modList.startLaunchTask(new Runnable() {
            @Override
            public void run() {
                PackManifest.ClassInfo activityInfoForName;
                Pack pack = Pack.this;
                pack.invokeEnvironmentClassMethod("prepareForLaunch", new Class[]{Pack.class}, new Object[]{pack}, false);
                pack.invokeEnvironmentClassMethod("instantLaunch", new Class[]{Pack.class}, new Object[]{pack}, false);
                if (Pack.this.abortIfRequired(activity, runnable) || (activityInfoForName = Pack.this.manifest.getActivityInfoForName(str)) == null) {
                    return;
                }
                Class<?> declaredClass = activityInfoForName.getDeclaredClass(getClass().getClassLoader());
                if (declaredClass != null) {
                    System.out.println("launching pack activity");
                    PrintStream printStream2 = System.out;
                    printStream2.println("pack activity class loader: " + declaredClass.getClassLoader());
                    PrintStream printStream3 = System.out;
                    printStream3.println("patched library class loader: " + JavaLibrary.class.getClassLoader());
                    File file2 = new File(Pack.this.directory.getAbsolutePath(), "base.apk.zip");
                    if (file2.exists()) {
                        AssetPatch.setRootOverrideDirectory(file2.getAbsolutePath());
                    } else {
                        AssetPatch.setRootOverrideDirectory(null);
                    }
                    Pack.this.invokeEnvironmentClassMethod("onActivityStarted", new Class[]{Pack.class, String.class}, new Object[]{Pack.this, str}, false);
                    return;
                }
                throw new RuntimeException("failed to get launching class for some reason");
            }
        }, runnable);
    }

    public List<MenuActivityFactory> getMenuActivityFactories() {
        return this.menuActivityFactories;
    }

    public List<Object> getCustomDrawables(String str) {
        return new ArrayList<>();
    }

    public Object getRandomCustomDrawable(String str) {
        return null;
    }

    public void buildCustomMenuLayout(Object view, Object view2) {
        invokeEnvironmentClassMethod("buildCustomMenuLayout", new Class[]{Object.class, Object.class}, new Object[]{view, view2}, false);
    }

    public static class MenuActivityFactory {
        private Pack pack;

        public String getIconGraphics() {
            return null;
        }

        public Collection<Object> getIconGraphicsBitmaps() {
            return null;
        }

        public String getMenuTitle() {
            return null;
        }

        public boolean onBackPressed() {
            return false;
        }

        public void onCreateLayout(Object activity, Object relativeLayout) {
        }

        public Pack getPack() {
            return this.pack;
        }
    }
}
