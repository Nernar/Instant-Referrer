package com.zhekasmirnov.innercore.mod.executable;

import com.googlecode.d2j.dex.Dex2jar;
import com.googlecode.dex2jar.tools.Jar2Dex;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.mod.build.BuildConfig;
import com.zhekasmirnov.innercore.mod.build.BuildHelper;
import com.zhekasmirnov.innercore.mod.build.CompiledSources;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.library.Library;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.IMessageReceiver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.optimizer.ClassCompiler;

public class Compiler {
    private static ContextFactory rhinoAndroidHelper;
    @SuppressWarnings("unused")
    private static File classCacheDir = new File(System.getProperty("java.io.tmpdir", "."), "classes");
    @SuppressWarnings("unused")
    private static int tempDexCounter = 0;
    private static Context defaultContext = null;

    public static Executable compileReader(Reader input, CompilerConfig compilerConfig) throws IOException {
        Context ctx = enter(compilerConfig.getOptimizationLevel());
        LoadingUI.setTip("Compiling " + compilerConfig.getFullName());
        Script script = ctx.compileReader(input, compilerConfig.getFullName(), 0, null);
        LoadingUI.setTip("");
        return wrapScript(ctx, script, compilerConfig);
    }

    public static Executable loadDex(File dex, CompilerConfig compilerConfig) throws IOException {
        Context ctx = enter(compilerConfig.getOptimizationLevel());
        Script script = loadScriptFromDex(dex);
        if (script == null) {
            return null;
        }
        Executable exec = wrapScript(ctx, script, compilerConfig);
        exec.isLoadedFromDex = true;
        return exec;
    }

    public static Executable loadDexList(File[] dexes, CompilerConfig compilerConfig) {
        Context ctx = enter(compilerConfig.getOptimizationLevel());
        MultidexScript multidex = new MultidexScript();
        LoadingUI.setTip("Wrapping " + compilerConfig.getFullName());
        for (File dex : dexes) {
            try {
                Script script = loadScriptFromDex(dex);
                if (script != null) {
                    multidex.addScript(script);
                }
            } catch (IOException e) {
                ICLog.e("COMPILER", "failed to load dex file into multi-dex executable: file=" + dex, e);
            }
        }
        if (multidex.getScriptCount() == 0) {
            return null;
        }
        Executable exec = wrapScript(ctx, multidex, compilerConfig);
        exec.isLoadedFromDex = true;
        LoadingUI.setTip("");
        return exec;
    }

    private static String genUniqueId() {
        return String.valueOf(Integer.toHexString((int) (Math.random() * 1.6777216E7d))) + "_" + Integer.toHexString((int) (Math.random() * 1.6777216E7d));
    }

    public static void compileScriptToFile(Reader input, String name, String targetFile, boolean dexConversion) throws IOException {
        File targetFileOut = new File(targetFile.endsWith(".dex") ? targetFile : targetFile + ".dex");
        if (targetFileOut.isDirectory()) {
            throw new IllegalArgumentException("Target script " + targetFileOut.getName() + " is directory!");
        }
        File targetFileOutJar = new File(targetFile.replace(".dex", "") + ".jar");
        if (targetFileOutJar.isDirectory()) {
            throw new IllegalArgumentException("Target jar script " + targetFileOutJar.getName() + " is directory!");
        }
        String inputSource = Kit.readReader(input);
        if (inputSource == null) {
            throw new IOException("input == null");
        }
        CompilerEnvirons compilerEnv = new CompilerEnvirons();
        compilerEnv.setLanguageVersion(Context.VERSION_ES6);
        compilerEnv.setOptimizationLevel(-1);
        ClassCompiler compiler = new ClassCompiler(compilerEnv);
        String mainClassName = name + "_" + genUniqueId();
        Object[] compiled = compiler.compileToClassFiles(inputSource, name, 0, mainClassName);
        if (compiled == null || compiled.length == 0) {
            return;
        }
        Manifest jarManifest = new Manifest();
        jarManifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClassName);
        jarManifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(targetFileOutJar), jarManifest);
        for (int j = 0; j != compiled.length; j += 2) {
            String className = (String) compiled[j];
            JarEntry entry = new JarEntry(className.replace('.', '/').concat(".class"));
            jarOutputStream.putNextEntry(entry);
            jarOutputStream.write((byte[]) compiled[j + 1]);
            jarOutputStream.closeEntry();
        }
        jarOutputStream.close();
        if (dexConversion) {
            Jar2Dex.main(new String[] {
                "--force",
                "--output",
                targetFileOut.getAbsolutePath(),
                targetFileOutJar.getAbsolutePath()
            });
        }
    }

    public static void compileScriptToFile(Reader input, String name, String targetFile) throws IOException {
        compileScriptToFile(input, name, targetFile, true);
    }

    public static Script loadScriptFromJar(File jarFile) throws IOException {
        URLClassLoader classLoader = new URLClassLoader(new URL[] {
            jarFile.toURI().toURL()
        }, Compiler.class.getClassLoader());
        JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile));
        Manifest manifest = jarInputStream.getManifest();
        Script script = null;
        if (manifest != null) {
            String className = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (className != null) {
                script = loadScriptFromClassLoader(classLoader, className);
            }
        }
        if (script == null) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (!entry.isDirectory() && entryName.endsWith(".class")) {
                    if ((script = loadScriptFromClassLoader(classLoader, entryName.replace('/', '.').substring(0, entryName.length() - 6))) != null) {
                        break;
                    }
                }
            }
        }
        try {
            classLoader.close();
        } catch (IOException e) {}
        try {
            jarInputStream.close();
        } catch (IOException e) {}
        return script;
    }

    public static Script loadScriptFromDex(File dexFile) throws IOException {
        File jarFile = new File(dexFile.getAbsolutePath().replace(".dex", "") + ".jar");
        if (!jarFile.isFile()) {
            Dex2jar dex2jar = Dex2jar.from(dexFile);
            dex2jar.to(jarFile.toPath());
        }
        return loadScriptFromJar(jarFile);
    }

    private static Script loadScriptFromClassLoader(ClassLoader classLoader, String className) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            if (Script.class.isAssignableFrom(clazz)) {
                return (Script) clazz.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            ICLog.d("COMPILER", "dex loading failed: " + className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            ICLog.d("COMPILER", "class " + className + " not found");
        }
        return null;
    }

    private static Executable wrapScript(Context ctx, Script script, CompilerConfig compilerConfig) {
        API apiInstance = compilerConfig.getApiInstance();
        ScriptableObject scope = ctx.initStandardObjects(apiInstance == null ? ScriptableObjectHelper.createEmpty() : apiInstance.newInstance(), false);
        if (compilerConfig.isLibrary) {
            return new Library(ctx, script, scope, compilerConfig, compilerConfig.getApiInstance());
        }
        if (apiInstance != null) {
            apiInstance.injectIntoScope(scope);
        }
        return new Executable(ctx, script, scope, compilerConfig, compilerConfig.getApiInstance());
    }

    public static Context getDefaultContext() {
        if (defaultContext == null) {
            defaultContext = Context.enter();
        }
        return defaultContext;
    }

    public static Context enter(int level) {
        if (rhinoAndroidHelper == null) {
            rhinoAndroidHelper = new ContextFactory();
        }
        Context ctx = rhinoAndroidHelper.enterContext();
        ctx.setOptimizationLevel(level);
        ctx.setLanguageVersion(200);
        return ctx;
    }

    public static Context assureContextForCurrentThread() {
        Context ctx = Context.getCurrentContext();
        if (ctx == null) {
            return enter(9);
        }
        return ctx;
    }

    public static boolean compileMod(Mod mod, IMessageReceiver logger) {
        IMessageReceiver logger2;
        if (logger == null) {
            logger2 = new IMessageReceiver() {
                @Override
                public void message(String string) {
                    ICLog.i("COMPILER", string);
                }
            };
        } else {
            logger2 = logger;
        }
        BuildConfig buildConfig2 = mod.buildConfig;
        ArrayList<BuildConfig.Source> sourceList = buildConfig2.getAllSourcesToCompile(false);
        CompiledSources compiledSources = mod.createCompiledSources();
        logger2.message("compiling mod " + mod.getName() + " (" + sourceList.size() + " source files)");
        logger2.message("cleaning up");
        compiledSources.reset();
        boolean isSucceeded = true;
        int uuid2 = 1;
        Iterator<BuildConfig.Source> it = sourceList.iterator();
        while (it.hasNext()) {
            BuildConfig.Source source = it.next();
            logger2.message("compiling source: path=" + source.path + " type=" + source.sourceType);
            BuildConfig.BuildableDir relatedDir = buildConfig2.findRelatedBuildableDir(source);
            ArrayList<File> sourceFiles = null;
            if (relatedDir != null) {
                try {
                    sourceFiles = BuildHelper.readIncludesFile(new File(mod.dir, relatedDir.dir));
                } catch (IOException e2) {
                    logger2.message("failed read includes, compiling result file: " + e2);
                }
            }
            if (sourceFiles == null) {
                sourceFiles = new ArrayList<>();
                sourceFiles.add(new File(String.valueOf(mod.dir) + source.path));
            }
            int uuid3 = uuid2;
            boolean isSucceeded2 = isSucceeded;
            int i = 0;
            while (i < sourceFiles.size()) {
                File file = sourceFiles.get(i);
                try {
                    logger2.message("$compiling: " + file.getName() + " (" + (i + 1) + "/" + sourceFiles.size() + ")");
                    FileReader reader = new FileReader(file);
                    StringBuilder sb2 = new StringBuilder();
                    int uuid = uuid3 + 1;
                    try {
                        sb2.append(uuid3);
                        sb2.append("");
                        File target = compiledSources.getTargetCompilationFile(sb2.toString());
                        new StringBuilder();
                        BuildConfig buildConfig = buildConfig2;
                        try {
                            compileScriptToFile(reader, mod.getName() + "$" + source.sourceName + "$" + file.getName(), target.getAbsolutePath());
                            compiledSources.addCompiledSource(source.path, target, source.sourceName);
                        } catch (Exception e5) {
                            e5.printStackTrace();
                            logger2.message("failed: " + e5);
                            isSucceeded2 = false;
                            i++;
                        }
                        uuid3 = uuid;
                        i++;
                        buildConfig2 = buildConfig;
                    } catch (Exception e) {
                    }
                } catch (Exception e3) {
                }
            }
            isSucceeded = isSucceeded2;
            uuid2 = uuid3;
        }
        logger2.message("compilation finished");
        return isSucceeded;
    }
}
