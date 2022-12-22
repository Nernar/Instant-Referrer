package com.zhekasmirnov.horizon.modloader.java;

import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.modloader.ModContext;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.runtime.task.Task;
import com.zhekasmirnov.horizon.runtime.task.TaskManager;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.batch.Main;

public class JavaCompilerHolder {
    @SuppressWarnings("unused")
    private static final String COMPONENT_PATH = "sdk/java/";
    private static final HashMap<Object, JavaCompilerHolder> instances = new HashMap<>();
    private final ModContext context;
    private final File installationDir;
    private final Main main;
    @SuppressWarnings("unused")
    private final String COMPONENT_VERSION_UUID = "34b14f6e-d8d1-48af-86a7-8adcb41396ce";
    private final Component[] COMPONENTS = {new JarComponent(this, "sdk/java/", "android.jar", null), new JarComponent(this, "sdk/java/", "android-support-multidex.jar", null), new JarComponent(this, "sdk/java/", "android-support-v4.jar", null), new JarComponent(this, "sdk/java/", "dx.jar", null), new JarComponent(this, "sdk/java/", "support-annotations-25.3.1.jar", null), new JarComponent(this, "sdk/java/", "gson-2.6.2.jar", null), new JarComponent(this, "sdk/java/", "horizon-classes.jar", null)};
    private boolean configured = false;
    private boolean isInitializing = false;
    private boolean isInitialized = false;
    @SuppressWarnings("unused")
    private boolean isInstalled = false;

    private interface Component {
        List<File> getBootFiles();

        boolean install();

        boolean isInstalled();
    }

    private class JarComponent implements Component {
        private final String assetName;
        private final String assetPath;

        private JarComponent(String str, String str2) {
            this.assetPath = str;
            this.assetName = str2;
        }

        JarComponent(JavaCompilerHolder javaCompilerHolder, String str, String str2, JarComponent jarComponent) {
            this(str, str2);
        }

        private File getJarFile() {
            return new File(Environment.getJavacDir(JavaCompilerHolder.this.context.context), this.assetName);
        }

        private String getLockFile() {
            try {
                String javacDir = Environment.getJavacDir(JavaCompilerHolder.this.context.context);
                return FileUtils.readFileText(new File(javacDir, String.valueOf(this.assetName) + ".uuid")).trim();
            } catch (IOException | NullPointerException e) {
                return null;
            }
        }

        private void setLockFile() {
            try {
                String javacDir = Environment.getJavacDir(JavaCompilerHolder.this.context.context);
                FileUtils.writeFileText(new File(javacDir, String.valueOf(this.assetName) + ".uuid"), "34b14f6e-d8d1-48af-86a7-8adcb41396ce");
            } catch (IOException e) {
                throw new RuntimeException("failed to write UUID lock for jar component: " + this.assetName);
            }
        }

        @Override
        public boolean isInstalled() {
            return getJarFile().exists() && "34b14f6e-d8d1-48af-86a7-8adcb41396ce".equals(getLockFile());
        }

        @Override
        public boolean install() {
            try {
                File jarFile = getJarFile();
                FileUtils.unpackAssetOrDirectory(null, jarFile, String.valueOf(this.assetPath) + this.assetName);
                setLockFile();
                return true;
            } catch (IOException e) {
                Logger.info("JavaCompiler", "not found java component " + this + ", skipping");
                return false;
            } catch (Exception e) {
                throw new RuntimeException("failed to install jar component " + this.assetName, e);
            }
        }

        @Override
        public List<File> getBootFiles() {
            ArrayList<File> arrayList = new ArrayList<>();
            arrayList.add(getJarFile());
            return arrayList;
        }

        public String toString() {
            return "[jar component " + this.assetName + "]";
        }
    }

    public static JavaCompilerHolder getInstance(Object context) {
        synchronized (instances) {
            return instances.get(context);
        }
    }

    public static void initializeForContext(ModContext modContext, TaskManager taskManager) {
        if (getInstance(modContext.context) == null) {
            synchronized (instances) {
                JavaCompilerHolder javaCompilerHolder = new JavaCompilerHolder(modContext);
                instances.put(modContext.context, javaCompilerHolder);
                taskManager.addTask(javaCompilerHolder.getInitializationTask());
            }
        }
    }

    public JavaCompilerHolder(ModContext modContext) {
        this.context = modContext;
        File file = new File(Environment.getJavacDir(modContext.context));
        this.installationDir = file;
        if (!file.exists()) {
            this.installationDir.mkdirs();
        }
        if (!this.installationDir.isDirectory()) {
            throw new RuntimeException("failed to allocate installation directory " + this.installationDir);
        }
        this.main = new Main(new PrintWriter(modContext.getEventLogger().getStream(EventLogger.MessageType.INFO, "BUILD")), new PrintWriter(modContext.getEventLogger().getStream(EventLogger.MessageType.FAULT, "BUILD")), false, null, (CompilationProgress) null);
    }

    public void initialize() {
        this.isInstalled = true;
        for (Component component : this.COMPONENTS) {
            if (!component.isInstalled()) {
                Logger.debug("JavaCompiler", "installing or re-installing java component: " + component);
                if (!component.install()) {
                    this.isInstalled = false;
                }
            }
        }
    }

    public Task getInitializationTask() {
        return new Task() {
            @Override
            public String getDescription() {
                return "initializing javac";
            }

            @Override
            public Object getLock() {
                return "initialize_javac";
            }

            @Override
            public void run() {
                if (!JavaCompilerHolder.this.isInitializing) {
                    JavaCompilerHolder.this.isInitializing = true;
                    if (!JavaCompilerHolder.this.isInitialized) {
                        JavaCompilerHolder.this.initialize();
                        JavaCompilerHolder.this.isInitialized = true;
                    }
                    JavaCompilerHolder.this.isInitializing = false;
                }
            }
        };
    }

    private void awaitInitialization() {
        while (!this.isInitialized) {
            Thread.yield();
        }
    }

    public boolean compile(JavaCompilerArguments javaCompilerArguments) {
        if (!this.configured) {
            this.main.configure(javaCompilerArguments.toArray());
            this.configured = true;
        }
        awaitInitialization();
        return this.main.compile(new String[0]);
    }

    public List<File> getBootFiles() {
        ArrayList<File> arrayList = new ArrayList<>();
        for (Component component : this.COMPONENTS) {
            arrayList.addAll(component.getBootFiles());
        }
        return arrayList;
    }

    void installLibraries(List<File> list, File file) {
        for (File file2 : list) {
            String name = file2.getName();
            if (name.endsWith(".jar") || name.endsWith("zip")) {
                installJarLibrary(file2, file);
            } else if (name.endsWith(".dex")) {
                File file3 = new File(file2.getAbsolutePath().replace(".dex", ".jar"));
                installJarLibrary(file3, file);
                file3.delete();
            } else {
                throw new RuntimeException("Unsupported file format:  " + file2.getName());
            }
        }
    }

    private void installJarLibrary(File file, File file2) {
        try {
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            file2.mkdirs();
            while (entries.hasMoreElements()) {
                JarEntry nextElement = entries.nextElement();
                if (nextElement.getName().endsWith(".class")) {
                    File file3 = new File(file2 + File.separator + nextElement.getName());
                    if (nextElement.isDirectory()) {
                        file3.mkdir();
                    } else if (!file3.exists()) {
                        file3.getParentFile().mkdirs();
                        InputStream inputStream = jarFile.getInputStream(nextElement);
                        FileOutputStream fileOutputStream = new FileOutputStream(file3);
                        byte[] bArr = new byte[8192];
                        while (true) {
                            int read = inputStream.read(bArr);
                            if (read <= 0) {
                                break;
                            }
                            fileOutputStream.write(bArr, 0, read);
                        }
                        fileOutputStream.close();
                        inputStream.close();
                    }
                }
            }
            jarFile.close();
        } catch (IOException e) {
            throw new RuntimeException("failed to install jar library " + file.getName(), e);
        }
    }
}
