package com.zhekasmirnov.horizon.modloader.library;

import com.zhekasmirnov.horizon.compiler.CommandResult;
import com.zhekasmirnov.horizon.compiler.holder.CompilerHolder;
import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.modloader.ExecutionDirectory;
import com.zhekasmirnov.horizon.modloader.mod.Mod;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

public class LibraryDirectory {
    public final File directory;
    private File executableFile;
    private Library libraryInstance;
    public final LibraryMakeFile makeFile;
    public final LibraryManifest manifest;
    public final Mod mod;
    public final File soFile;

    public LibraryDirectory(Mod mod, File file) {
        this.executableFile = null;
        this.libraryInstance = null;
        this.mod = mod;
        if (!file.isDirectory()) {
            throw new IllegalStateException("non-directory file passed to LibraryDirectory constructor: " + file);
        }
        this.directory = file;
        try {
            this.manifest = new LibraryManifest(new File(file, "manifest"));
            this.makeFile = new LibraryMakeFile(new File(file, "make.txt"));
            this.soFile = findSharedObjectFile();
        } catch (IOException e) {
            throw new RuntimeException("failed to read library manifest for " + file, e);
        } catch (JSONException e2) {
            throw new RuntimeException("failed to read library manifest for " + file, e2);
        }
    }

    private File findSharedObjectFile() {
        File file = new File(this.directory, this.manifest.getSoName());
        if (file.exists() && file.isFile()) {
            return file;
        }
        File file2 = new File(this.directory, "so");
        for (String str : Environment.getSupportedABIs()) {
            File file3 = new File(file2, str);
            if (file3.exists() && file3.isDirectory()) {
                File file4 = new File(file3, this.manifest.getSoName());
                File file5 = new File(String.valueOf(file4.getAbsolutePath()) + ".o3");
                if (file5.exists() && file5.isFile()) {
                    return file5;
                }
                if (file4.exists() && file4.isFile()) {
                    return file4;
                }
            }
        }
        return file;
    }

    public LibraryDirectory(File file) {
        this(null, file);
    }

    public boolean isInDevMode() {
        return !this.soFile.exists();
    }

    public boolean isPreCompiled() {
        return !FileUtils.getFileFlag(this.directory, "not_precompiled");
    }

    public boolean isSharedLibrary() {
        return this.manifest.isSharedLibrary();
    }

    public boolean isOptimized() {
        return this.soFile.getName().endsWith(".o3");
    }

    public int getVersionCode() {
        return this.manifest.getVersion();
    }

    public String getName() {
        return this.manifest.getName();
    }

    public String getSoFileName() {
        String name = this.soFile.getName();
        return name.endsWith(".o3") ? name.substring(0, name.length() - 3) : name;
    }

    public List<File> getIncludeDirs() {
        List<String> include = this.manifest.getInclude();
        ArrayList<File> arrayList = new ArrayList<>();
        for (String str : include) {
            arrayList.add(new File(this.directory, str));
        }
        return arrayList;
    }

    public List<String> getDependencyNames() {
        return this.manifest.getDependencies();
    }

    public File getExecutableFile() {
        return this.executableFile;
    }

    public Library getLibrary() {
        return this.libraryInstance;
    }

    public void compileToTargetFile(ExecutionDirectory executionDirectory, Object context, File file) {
        CompilerHolder compilerHolder = CompilerHolder.getInstance(context);
        if (compilerHolder == null) {
            throw new RuntimeException("failed to compile " + getName() + ": no compiler holder found for context " + context);
        }
        LibraryCompiler libraryCompiler = new LibraryCompiler(compilerHolder, this, file);
        libraryCompiler.initialize(context, executionDirectory);
        CommandResult compile = libraryCompiler.compile(context);
        if (compile.getResultCode() != 0) {
            if (file.exists()) {
                file.delete();
            }
            throw new RuntimeException("failed to compile " + getName() + ": " + compile.getMessage());
        }
    }

    public void setPreCompiled(boolean z) {
        FileUtils.setFileFlag(this.directory, "not_precompiled", !z);
    }

    public void addToExecutionDirectory(ExecutionDirectory executionDirectory, Object context, File file) {
        this.executableFile = file;
        if (this.soFile.exists()) {
            try {
                FileUtils.copy(this.soFile, file);
                return;
            } catch (IOException e) {
                throw new RuntimeException("failed to deploy " + getName() + ": " + e.toString(), e);
            }
        }
        compileToTargetFile(executionDirectory, context, file);
    }

    public void loadExecutableFile() {
        if (this.executableFile != null) {
            Mod mod = this.mod;
            if (mod != null && !mod.getSafetyInterface().beginUnsafeSection()) {
                Logger.error("LibraryDirectory", "failed to create crash lock for some reason: mod=" + this.mod);
            }
            this.libraryInstance = Library.load(this.executableFile.getAbsolutePath(), isOptimized());
            Mod mod2 = this.mod;
            if (mod2 == null || mod2.getSafetyInterface().endUnsafeSection()) {
                return;
            }
            Logger.error("LibraryDirectory", "failed to remove crash lock for some reason: mod=" + this.mod);
            return;
        }
        throw new IllegalStateException("trying to load library directory with no execution file (dir = " + this.directory + ")");
    }

    public int hashCode() {
        return this.directory.getAbsolutePath().hashCode();
    }
}
