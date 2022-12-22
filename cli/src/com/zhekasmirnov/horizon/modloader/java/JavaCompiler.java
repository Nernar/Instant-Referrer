package com.zhekasmirnov.horizon.modloader.java;

import com.android.dx.command.dexer.Main;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JavaCompiler {
    @SuppressWarnings("unused")
    private final Object context;
    private final JavaCompilerHolder holder;

    public JavaCompiler(Object context) {
        this.context = context;
        this.holder = JavaCompilerHolder.getInstance(context);
    }

    private void getClassFiles(List<File> list, File file) {
        if (file.isDirectory()) {
            for (File file2 : file.listFiles()) {
                getClassFiles(list, file2);
            }
            return;
        }
        list.add(file);
    }

    public boolean compile(JavaDirectory javaDirectory) {
        JavaCompilerArguments javaCompilerArguments = new JavaCompilerArguments(new String[0]);
        File destinationDirectory = javaDirectory.getDestinationDirectory();
        FileUtils.clearFileTree(destinationDirectory, false);
        PrintStream printStream = System.out;
        printStream.println("BOOT PATHS: " + javaDirectory.getLibraryPaths(this.holder.getBootFiles()));
        javaDirectory.isVerboseRequired();
        javaCompilerArguments.add("-verbose");
        javaCompilerArguments.add("-nowarn");
        javaCompilerArguments.add("-bootclasspath", javaDirectory.getLibraryPaths(this.holder.getBootFiles()));
        javaCompilerArguments.add(javaDirectory.getArguments());
        javaCompilerArguments.add("-proc:none");
        javaCompilerArguments.add("-source", "1.7");
        javaCompilerArguments.add("-d", destinationDirectory.getAbsolutePath());
        javaCompilerArguments.add(javaDirectory.getAllSourceFiles());
        PrintStream printStream2 = System.out;
        printStream2.println("javac args=" + javaCompilerArguments);
        if (this.holder.compile(javaCompilerArguments)) {
            System.out.println("compilation complete, installing libraries...");
            this.holder.installLibraries(javaDirectory.manifest.libraryPaths, destinationDirectory);
            ArrayList<File> arrayList = new ArrayList<>();
            getClassFiles(arrayList, destinationDirectory);
            PrintStream printStream3 = System.out;
            printStream3.println("got " + arrayList.size() + " class files, adding...");
            ArrayList<String> arrayList2 = new ArrayList<>();
            Iterator<File> it = arrayList.iterator();
            while (it.hasNext()) {
                File file = it.next();
                arrayList2.add(file.getAbsolutePath());
            }
            Main.Arguments arguments = new Main.Arguments();
            arguments.fileNames = (String[]) arrayList2.toArray(new String[arrayList2.size()]);
            arguments.outName = javaDirectory.getBuildDexFile().getAbsolutePath();
            arguments.jarOutput = true;
            arguments.strictNameCheck = false;
            arguments.verbose = true;
            try {
                System.out.println("Dexing java library: " + javaDirectory);
                Main.run(arguments);
                System.out.println("Dexing java library complete: " + javaDirectory);
                return true;
            } catch (IOException e) {
                throw new RuntimeException("error occurred while building dex file for " + javaDirectory, e);
            }
        }
        return false;
    }
}
