package com.zhekasmirnov.horizon.modloader;

import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.java.JavaLibrary;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExecutionDirectory {
    public final File directory;
    public final boolean isPackDriven;
    private final HashMap<String, List<RuntimeLibrary>> libraries = new HashMap<>();
    private final List<JavaDirectory> javaDirectories = new ArrayList<>();

    private class RuntimeLibrary {
        final LibraryDirectory library;
        final String name;
        final File soFile;

        private RuntimeLibrary(LibraryDirectory libraryDirectory, String str) {
            this.library = libraryDirectory;
            this.name = str;
            this.soFile = new File(ExecutionDirectory.this.directory, this.name);
        }

        RuntimeLibrary(ExecutionDirectory executionDirectory, LibraryDirectory libraryDirectory, String str, RuntimeLibrary runtimeLibrary) {
            this(libraryDirectory, str);
        }

        public RuntimeLibrary rename(String str) {
            return new RuntimeLibrary(this.library, str);
        }
    }

    public ExecutionDirectory(File file, boolean z) {
        this.directory = file;
        this.isPackDriven = z;
        if (!file.exists()) {
            this.directory.mkdirs();
        }
        if (!this.directory.isDirectory()) {
            throw new IllegalArgumentException("Non-directory file passed to ExecutionDirectory constructor");
        }
    }

    public synchronized void addLibraryDirectory(LibraryDirectory libraryDirectory) {
        String soFileName = libraryDirectory.getSoFileName();
        List<RuntimeLibrary> list = this.libraries.get(soFileName);
        if (list != null) {
            if (libraryDirectory.isSharedLibrary()) {
                RuntimeLibrary runtimeLibrary = list.get(0);
                LibraryDirectory libraryDirectory2 = runtimeLibrary.library;
                if (libraryDirectory2.isSharedLibrary()) {
                    if (libraryDirectory2.getVersionCode() < libraryDirectory.getVersionCode()) {
                        list.set(0, new RuntimeLibrary(this, libraryDirectory, soFileName, null));
                        return;
                    }
                    return;
                }
                list.add(runtimeLibrary.rename(String.valueOf(soFileName) + list.size()));
                list.set(0, new RuntimeLibrary(this, libraryDirectory, soFileName, null));
                return;
            }
            list.add(new RuntimeLibrary(this, libraryDirectory, String.valueOf(soFileName) + list.size(), null));
            return;
        }
        ArrayList<RuntimeLibrary> arrayList = new ArrayList<>();
        arrayList.add(new RuntimeLibrary(this, libraryDirectory, soFileName, null));
        this.libraries.put(soFileName, arrayList);
    }

    public LibraryDirectory getLibByName(String str) {
        HashMap<String, List<RuntimeLibrary>> hashMap = this.libraries;
        List<RuntimeLibrary> list = hashMap.get("lib" + str + ".so");
        if (list != null) {
            return list.get(0).library;
        }
        return null;
    }

    public void addJavaDirectory(JavaDirectory javaDirectory) {
        this.javaDirectories.add(javaDirectory);
    }

    public LaunchSequence build(Object context, EventLogger eventLogger) {
        if (!this.isPackDriven) {
            FileUtils.clearFileTree(this.directory, false);
        }
        ArrayList<LibraryDirectory> arrayList = new ArrayList<>();
        for (List<RuntimeLibrary> list : this.libraries.values()) {
            for (RuntimeLibrary runtimeLibrary : list) {
                LibraryDirectory libraryDirectory = runtimeLibrary.library;
                try {
                    libraryDirectory.addToExecutionDirectory(this, context, runtimeLibrary.soFile);
                    arrayList.add(libraryDirectory);
                } catch (Throwable th) {
                    eventLogger.fault("BUILD", "details: lang=c++ dir=" + libraryDirectory.directory, th);
                }
            }
        }
        ArrayList<JavaLibrary> arrayList2 = new ArrayList<>();
        for (JavaDirectory javaDirectory : this.javaDirectories) {
            try {
                arrayList2.add(javaDirectory.addToExecutionDirectory(this, context));
            } catch (Throwable th2) {
                eventLogger.fault("BUILD", "details: lang=java dir=" + javaDirectory.directory, th2);
            }
        }
        return new LaunchSequence(this, arrayList, arrayList2);
    }

    public void clear() {
        this.libraries.clear();
        this.javaDirectories.clear();
        if (this.isPackDriven) {
            return;
        }
        FileUtils.clearFileTree(this.directory, false);
    }
}
