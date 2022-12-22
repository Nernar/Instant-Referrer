package com.zhekasmirnov.horizon.compiler.holder;

import com.zhekasmirnov.horizon.compiler.CommandResult;
import com.zhekasmirnov.horizon.compiler.Shell;
import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.runtime.task.Task;
import com.zhekasmirnov.horizon.runtime.task.TaskManager;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CompilerHolder {
    private static final HashMap<Object, CompilerHolder> instances = new HashMap<>();
    private final Object context;
    private final File downloadDir;
    private final File installationDir;
    private final CompilerInstaller installer;
    private final List<File> environmentLibraries = new ArrayList<>();
    private boolean isInitializing = false;
    private boolean isInitialized = false;
    private InstallationStatus installationStatus = InstallationStatus.NOT_INSTALLED;

    enum InstallationStatus {
        NOT_INSTALLED,
        DOWNLOADED,
        INSTALLED,
        CORRUPT;

        public static InstallationStatus[] valuesCustom() {
            InstallationStatus[] valuesCustom = values();
            int length = valuesCustom.length;
            InstallationStatus[] installationStatusArr = new InstallationStatus[length];
            System.arraycopy(valuesCustom, 0, installationStatusArr, 0, length);
            return installationStatusArr;
        }
    }

    public static CompilerHolder getInstance(Object context) {
        synchronized (instances) {
            return instances.get(context);
        }
    }

    public static void initializeForContext(Object activity, TaskManager taskManager) {
        if (getInstance(activity) == null) {
            synchronized (instances) {
                CompilerHolder compilerHolder = new CompilerHolder(activity);
                instances.put(activity, compilerHolder);
                taskManager.addTask(compilerHolder.getInitializationTask());
            }
        }
    }

    public CompilerHolder(Object activity) {
        this.context = activity;
        File file = new File(Environment.getToolchainsDir(activity));
        this.installationDir = file;
        if (!file.exists()) {
            this.installationDir.mkdirs();
        }
        if (!this.installationDir.isDirectory()) {
            throw new RuntimeException("failed to allocate installation directory " + this.installationDir);
        }
        File file2 = new File(this.installationDir.getParentFile(), "compiler-download");
        this.downloadDir = file2;
        if (!file2.exists()) {
            this.downloadDir.mkdirs();
        }
        if (!this.downloadDir.isDirectory()) {
            throw new RuntimeException("failed to allocate download directory " + this.downloadDir);
        }
        this.installer = new CompilerInstaller(activity, this.downloadDir);
    }

    private boolean getInstallationLock() {
        return new File(this.downloadDir, ".first-installation-lock").exists();
    }

    private boolean createInstallationLock() {
        try {
            return new File(this.downloadDir, ".first-installation-lock").createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isGCCCommandAvailable() {
        CommandResult exec = Shell.exec(this.context, this.downloadDir.getAbsolutePath(), "g++-4.9");
        PrintStream printStream = System.out;
        printStream.println("test command: " + exec);
        return exec.getResultCode() == 0 || exec.getResultCode() == 1;
    }

    public void initialize() {
        if (getInstallationLock()) {
            if (isGCCCommandAvailable()) {
                this.installationStatus = InstallationStatus.INSTALLED;
                return;
            } else if (this.installer.runSilentInstallationTasks(2) != null || !this.installer.areInstallationPackagesFound()) {
                this.installationStatus = InstallationStatus.CORRUPT;
                return;
            } else {
                this.installationStatus = InstallationStatus.DOWNLOADED;
                return;
            }
        }
        this.installationStatus = InstallationStatus.NOT_INSTALLED;
    }

    public Task getInitializationTask() {
        return new Task() {
            @Override
            public String getDescription() {
                return "initializing compiler";
            }

            @Override
            public Object getLock() {
                return "initialize_compiler";
            }

            @Override
            public void run() {
                if (!CompilerHolder.this.isInitializing) {
                    CompilerHolder.this.isInitializing = true;
                    if (!CompilerHolder.this.isInitialized) {
                        CompilerHolder.this.initialize();
                        CompilerHolder.this.isInitialized = true;
                    }
                    CompilerHolder.this.isInitializing = false;
                }
            }
        };
    }

    public InstallationStatus getInstallationStatus() {
        return this.installationStatus;
    }

    private void awaitInitialization() {
        while (!this.isInitialized) {
            Thread.yield();
        }
    }

    private CompilerInstaller.InstallationStatus requestCompilerInstallation() {
        if (this.installationStatus == InstallationStatus.NOT_INSTALLED) {
            return this.installer.runInstallationSequence(false);
        }
        if (this.installationStatus == InstallationStatus.DOWNLOADED || this.installationStatus == InstallationStatus.CORRUPT) {
            return this.installer.runInstallationSequence(true);
        }
        return CompilerInstaller.InstallationStatus.FAILED;
    }

    public CommandResult execute(Object context, String str, String str2) {
        awaitInitialization();
        if (this.installationStatus != InstallationStatus.INSTALLED) {
            CompilerInstaller.InstallationStatus requestCompilerInstallation = requestCompilerInstallation();
            PrintStream printStream = System.out;
            printStream.println("installation result: " + requestCompilerInstallation);
            if (requestCompilerInstallation == CompilerInstaller.InstallationStatus.COMPLETED) {
                this.installationStatus = InstallationStatus.INSTALLED;
                createInstallationLock();
            }
        }
        return Shell.exec(context, str, str2);
    }

    public List<File> getEnvironmentLibraries() {
        return this.environmentLibraries;
    }

    public void clearEnvironmentLibraries() {
        this.environmentLibraries.clear();
    }

    public void addEnvironmentLibraries(Collection<File> collection) {
        this.environmentLibraries.addAll(collection);
    }
}
