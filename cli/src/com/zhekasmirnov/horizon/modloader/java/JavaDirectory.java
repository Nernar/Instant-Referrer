package com.zhekasmirnov.horizon.modloader.java;

import com.googlecode.d2j.dex.Dex2jar;
import com.zhekasmirnov.horizon.modloader.ExecutionDirectory;
import com.zhekasmirnov.horizon.modloader.mod.Mod;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

public class JavaDirectory {
    public final File directory;
    public final JavaLibraryManifest manifest;
    public final Mod mod;

    public JavaDirectory(Mod mod, File file) {
        this.mod = mod;
        PrintStream printStream = System.out;
        printStream.println("java dir=" + file);
        if (!file.isDirectory()) {
            throw new IllegalStateException("non-directory file passed to JavaDirectory constructor: " + file);
        }
        this.directory = file;
        File file2 = new File(file, "oat");
        if (file2.isDirectory()) {
            FileUtils.clearFileTree(file2, true);
        }
        try {
            this.manifest = new JavaLibraryManifest(new File(file, "manifest"));
        } catch (IOException e) {
            throw new RuntimeException("failed to read java library manifest for " + file, e);
        } catch (JSONException e2) {
            throw new RuntimeException("failed to read java library manifest for " + file, e2);
        }
    }

    public String getName() {
        return this.directory.getName();
    }

    public File getSubDirectory(String str, boolean z) {
        File file = new File(this.directory, str);
        if (!file.exists()) {
            if (!z) {
                return null;
            }
            file.mkdirs();
        }
        if (file.isDirectory()) {
            return file;
        }
        return null;
    }

    public File getDestinationDirectory() {
        return getSubDirectory(".build/classes", true);
    }

    public File getJarDirectory() {
        return getSubDirectory(".build/jar", true);
    }

    private static String makeSeparatedString(List<File> list) {
        StringBuilder sb = new StringBuilder();
        for (File file : list) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(file.getAbsolutePath());
        }
        return sb.toString();
    }

    public File getBuildDexFile() {
        File subDirectory = getSubDirectory(".build", true);
        if (subDirectory != null) {
            return new File(subDirectory, "build.dex");
        }
        return null;
    }

    public File getCompiledDexFile() {
        return new File(this.directory, ".compiled.dex");
    }

    public String getSourceDirectories() {
        return makeSeparatedString(this.manifest.sourceDirs);
    }

    public String getLibraryPaths(List<File> list) {
        ArrayList<File> arrayList = new ArrayList<>();
        arrayList.addAll(list);
        for (File file : this.manifest.libraryPaths) {
            if (file.getName().endsWith(".dex")) {
                try {
                    Dex2jar from = Dex2jar.from(file);
                    File file2 = new File(file.getAbsolutePath().replace(".dex", ".jar"));
                    from.to(file2.toPath());
                    arrayList.add(file2);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot create jar file of dex " + file, e);
                }
            } else {
                arrayList.add(file);
            }
        }
        arrayList.addAll(this.manifest.libraryPaths);
        return makeSeparatedString(arrayList);
    }

    public String[] getArguments() {
        return this.manifest.arguments;
    }

    public boolean isVerboseRequired() {
        return this.manifest.verbose;
    }

    public String[] getAllSourceFiles() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (File file : this.manifest.sourceDirs) {
            getAllSourceFiles(arrayList, file);
        }
        PrintStream printStream = System.out;
        printStream.println("source size: " + arrayList.size());
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }

    private void getAllSourceFiles(ArrayList<String> arrayList, File file) {
        if (file.exists()) {
            for (File file2 : file.listFiles()) {
                if (file2.isDirectory()) {
                    getAllSourceFiles(arrayList, file2);
                } else if (file2.exists() && file2.isFile() && file2.getName().endsWith(".java")) {
                    arrayList.add(file2.getAbsolutePath());
                }
            }
        }
    }

    public List<String> getBootClassNames() {
        return this.manifest.bootClasses;
    }

    public JavaLibrary addToExecutionDirectory(ExecutionDirectory executionDirectory, Object context) {
        File compiledClassesFile = getCompiledClassesFile();
        if (compiledClassesFile.exists() && !compiledClassesFile.isDirectory()) {
            return new JavaLibrary(this, getCompiledClassesFiles());
        }
        new JavaCompiler(context).compile(this);
        File compiledDexFile = getCompiledDexFile();
        if (compiledDexFile.exists()) {
            return new JavaLibrary(this, compiledDexFile);
        }
        File buildDexFile = getBuildDexFile();
        if (buildDexFile.exists()) {
            return new JavaLibrary(this, buildDexFile);
        }
        throw new RuntimeException("failed to build library " + this + " for some reason");
    }

    public void compileToClassesFile(Object context) {
        new JavaCompiler(context).compile(this);
        File compiledDexFile = getCompiledDexFile();
        if (compiledDexFile.exists()) {
            try {
                FileUtils.copy(compiledDexFile, getCompiledClassesFile());
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("failed to build library " + this + " for some reason");
    }

    public File getCompiledClassesFile() {
        return new File(this.directory, "classes.dex");
    }

    public List<File> getCompiledClassesFiles() {
        String[] list = this.directory.list();
        ArrayList<File> arrayList = new ArrayList<>(list.length);
        for (String str : list) {
            if (str.matches("classes[0-9]*\\.dex")) {
                arrayList.add(new File(this.directory, str));
            }
        }
        return arrayList;
    }

    public boolean isInDevMode() {
        return getCompiledClassesFile().exists();
    }

    public void setPreCompiled(boolean z) {
        FileUtils.setFileFlag(this.directory, "not_precompiled", !z);
    }

    public boolean isPreCompiled() {
        return !FileUtils.getFileFlag(this.directory, "not_precompiled");
    }
}
