package com.zhekasmirnov.horizon.modloader;

import com.zhekasmirnov.horizon.modloader.java.JavaLibrary;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LaunchSequence {
    private final ExecutionDirectory directory;
    private final List<JavaLibrary> javaLibraries;
    private final List<LibraryDirectory> libraries;
    private List<LibraryTreeNode> sequence = new ArrayList<>();

    private class LibraryTreeNode {
        private List<LibraryTreeNode> dependencies = new ArrayList<>();
        public final LibraryDirectory library;

        public LibraryTreeNode(LibraryDirectory libraryDirectory) {
            this.library = libraryDirectory;
        }

        public void addDependency(LibraryTreeNode libraryTreeNode) {
            this.dependencies.add(libraryTreeNode);
        }

        public List<LibraryTreeNode> getDependencies() {
            return this.dependencies;
        }

        public String getName() {
            return this.library.getName();
        }
    }

    public LaunchSequence(ExecutionDirectory executionDirectory, List<LibraryDirectory> list, List<JavaLibrary> list2) {
        this.directory = executionDirectory;
        this.libraries = list;
        this.javaLibraries = list2;
    }

    private void resolveDependencies(List<LibraryTreeNode> list, List<LibraryTreeNode> list2, LibraryTreeNode libraryTreeNode, EventLogger eventLogger) {
        list.remove(libraryTreeNode);
        for (LibraryTreeNode libraryTreeNode2 : libraryTreeNode.getDependencies()) {
            if (list.contains(libraryTreeNode2)) {
                resolveDependencies(list, list2, libraryTreeNode2, eventLogger);
            } else if (!list2.contains(libraryTreeNode2)) {
                eventLogger.fault("BUILD", "Failed to resolve dependency " + libraryTreeNode2.getName() + " of " + libraryTreeNode.getName() + " due to cyclic dependency. Loading order for this two will be reversed");
            }
        }
        list2.add(libraryTreeNode);
    }

    public void buildSequence(EventLogger eventLogger) {
        HashMap<LibraryDirectory, LibraryTreeNode> hashMap = new HashMap<>();
        for (LibraryDirectory libraryDirectory : this.libraries) {
            hashMap.put(libraryDirectory, new LibraryTreeNode(libraryDirectory));
        }
        for (LibraryTreeNode libraryTreeNode : hashMap.values()) {
            for (String str : libraryTreeNode.library.getDependencyNames()) {
                LibraryDirectory libByName = this.directory.getLibByName(str);
                if (libByName != null) {
                    LibraryTreeNode libraryTreeNode2 = hashMap.get(libByName);
                    if (libraryTreeNode2 == null) {
                        eventLogger.fault("BUILD", "Assertion error occurred during dependency resolving: failed to find node for directory " + libByName.directory);
                    } else if (libraryTreeNode2 != libraryTreeNode) {
                        libraryTreeNode.addDependency(libraryTreeNode2);
                    } else {
                        eventLogger.fault("BUILD", "Self-dependency for " + libByName.getName() + " detected and will not be added");
                    }
                }
            }
        }
        ArrayList<LibraryTreeNode> arrayList = new ArrayList<>(hashMap.values());
        ArrayList<LibraryTreeNode> arrayList2 = new ArrayList<>();
        while (arrayList.size() > 0) {
            resolveDependencies(arrayList, arrayList2, arrayList.get(0), eventLogger);
        }
        this.sequence.clear();
        this.sequence.addAll(arrayList2);
    }

    public void loadAll(EventLogger eventLogger) {
        for (LibraryTreeNode libraryTreeNode : this.sequence) {
            LibraryDirectory libraryDirectory = libraryTreeNode.library;
            try {
                libraryDirectory.loadExecutableFile();
            } catch (Throwable th) {
                eventLogger.fault("LOAD", "failed to load library " + libraryDirectory.getName(), th);
            }
        }
        for (JavaLibrary javaLibrary : this.javaLibraries) {
            if (!javaLibrary.isInitialized()) {
                try {
                    javaLibrary.initialize();
                } catch (Throwable th2) {
                    eventLogger.fault("LOAD", "failed to load java library " + javaLibrary.getDirectory(), th2);
                }
            }
        }
    }

    public List<LibraryDirectory> getAllLibraries() {
        return this.libraries;
    }
}
