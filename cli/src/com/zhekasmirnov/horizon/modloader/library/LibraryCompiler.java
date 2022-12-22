package com.zhekasmirnov.horizon.modloader.library;

import com.pdaxrom.utils.Utils;
import com.zhekasmirnov.horizon.compiler.CommandResult;
import com.zhekasmirnov.horizon.compiler.holder.CompilerHolder;
import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.modloader.ExecutionDirectory;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LibraryCompiler {
    private static File builtInIncludeDirectory = null;
    @SuppressWarnings("unused")
    private static final String compiler = "g++-4.9 -std=c++11 -v";
    private final CompilerHolder compilerHolder;
    private List<String> dependencies;
    private List<String> files;
    private List<String> includes;
    private final LibraryDirectory library;
    private final LibraryMakeFile make;
    private File target;

    public LibraryCompiler(CompilerHolder compilerHolder, LibraryDirectory libraryDirectory, File file) {
        this.files = new ArrayList<>();
        this.includes = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.compilerHolder = compilerHolder;
        this.library = libraryDirectory;
        this.target = file;
        this.make = libraryDirectory.makeFile;
    }

    public LibraryCompiler(CompilerHolder compilerHolder, LibraryDirectory libraryDirectory) {
        this(compilerHolder, libraryDirectory, null);
    }

    private void addSourceFilesFrom(File file) {
        for (File file2 : file.listFiles()) {
            if (file2.isDirectory()) {
                addSourceFilesFrom(file2);
            } else if (file2.getName().endsWith(".cpp")) {
                this.files.add(file2.getAbsolutePath());
            }
        }
    }

    public void initialize(Object context, ExecutionDirectory executionDirectory) {
        this.files.clear();
        this.includes.clear();
        this.dependencies.clear();
        List<String> list = this.dependencies;
        list.add("-L" + executionDirectory.directory.getAbsolutePath());
        List<String> list2 = this.dependencies;
        list2.add("-L" + new File(Environment.getApplicationLibraryDirectory(), "libhorizon.so").getAbsolutePath());
        this.dependencies.add("-landroid");
        this.dependencies.add("-lm");
        this.dependencies.add("-llog");
        if (this.make.getFiles() != null) {
            this.files.addAll(this.make.getFiles());
        } else {
            addSourceFilesFrom(this.library.directory);
        }
        Iterator<File> it = this.compilerHolder.getEnvironmentLibraries().iterator();
        while (it.hasNext()) {
            List<String> list3 = this.dependencies;
            list3.add("-l:" + new File("../../../../../../../../../../../", it.next().getAbsolutePath()));
        }
        String absolutePath = new File("../../../../../../../../../../../", executionDirectory.directory.getAbsolutePath()).getAbsolutePath();
        for (String str : this.library.getDependencyNames()) {
            File file = new File(absolutePath, "lib" + str + ".so");
            if (!file.exists()) {
                compilePlaceholder(context, file);
            }
            List<String> list4 = this.dependencies;
            list4.add("-l:" + file);
            LibraryDirectory libByName = executionDirectory.getLibByName(str);
            if (libByName != null) {
                Iterator<File> it2 = libByName.getIncludeDirs().iterator();
                while (it2.hasNext()) {
                    List<String> list5 = this.includes;
                    list5.add("-I" + it2.next().getAbsolutePath());
                }
            }
        }
    }

    private void compilePlaceholder(Object context, File file) {
        File file2 = new File(file.getParentFile(), ".placeholder.cpp");
        file2.delete();
        try {
            FileUtils.writeFileText(file2, "#define THIS_IS_A_PLACEHOLDER\n");
            Utils.emptyDirectory(new File(Environment.getTmpExeDir(context)));
            Utils.emptyDirectory(new File(Environment.getSdCardTmpDir()));
            CommandResult execute = this.compilerHolder.execute(context, file.getParentFile().getAbsolutePath(), "g++-4.9 -std=c++11 -v " + file2.getAbsolutePath() + " -shared -o " + file.getAbsolutePath() + " ");
            file2.delete();
            PrintStream printStream = System.out;
            printStream.println("COMPILER: compiled placeholder file " + file + " with result " + execute.getResultCode() + " in " + execute.getTime() + " ms");
        } catch (IOException e) {
            throw new RuntimeException("failed to create empty file for placeholder compilation " + file, e);
        }
    }

    public CommandResult compile(Object context) {
        StringBuilder sb = new StringBuilder();
        sb.append("g++-4.9 -std=c++11 -v");
        sb.append(" ");
        for (String str : this.files) {
            sb.append(str);
            sb.append(" ");
        }
        if (this.make.getCppFlags() != null) {
            sb.append(this.make.getCppFlags());
            sb.append(" ");
        }
        sb.append("-shared -o ");
        sb.append(this.target.getAbsolutePath());
        sb.append(" ");
        sb.append("-I");
        sb.append(unpackBuiltInIncludesIfRequired(context));
        sb.append(" ");
        for (String str2 : this.includes) {
            sb.append(str2);
            sb.append(" ");
        }
        sb.append("-L");
        sb.append(new File(Environment.getDataDirFile(context), "lib").getAbsolutePath());
        sb.append(" ");
        for (String str3 : this.dependencies) {
            sb.append(str3);
            sb.append(" ");
        }
        Utils.emptyDirectory(new File(Environment.getTmpExeDir(context)));
        Utils.emptyDirectory(new File(Environment.getSdCardTmpDir()));
        return this.compilerHolder.execute(context, this.library.directory.getAbsolutePath(), sb.toString());
    }

    private static synchronized String unpackBuiltInIncludesIfRequired(Object context) {
        if (builtInIncludeDirectory == null) {
            File file = new File(Environment.getDataDirFile(context), "includes/builtin");
            builtInIncludeDirectory = file;
            if (!file.exists()) {
                builtInIncludeDirectory.mkdirs();
            }
            FileUtils.clearFileTree(builtInIncludeDirectory, false);
            try {
                FileUtils.unpackAssetOrDirectory(null, builtInIncludeDirectory, "includes");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builtInIncludeDirectory.getAbsolutePath();
    }
}
