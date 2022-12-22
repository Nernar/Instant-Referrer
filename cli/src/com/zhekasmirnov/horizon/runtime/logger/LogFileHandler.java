package com.zhekasmirnov.horizon.runtime.logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LogFileHandler {
    private static LogFileHandler instance = new LogFileHandler(new File(System.getProperty("user.dir"), "games/horizon/logs"), 5);
    private final File directory;
    private boolean initializedWithErrors = false;
    private final int numLastLogsStored;

    public static LogFileHandler getInstance() {
        return instance;
    }

    public LogFileHandler(File file, int i) {
        this.directory = file;
        this.numLastLogsStored = i;
    }

    private void initializeInDirectory() {
        if (this.directory.isDirectory()) {
            return;
        }
        this.directory.mkdirs();
        if (this.directory.isDirectory()) {
            this.initializedWithErrors = true;
        }
    }

    public File getExistingLogFile(String str) {
        initializeInDirectory();
        if (this.initializedWithErrors) {
            return null;
        }
        File file = new File(this.directory, str);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    private boolean renameOldRecursive(File file, String str, int i) {
        File file2 = this.directory;
        File file3 = new File(file2, String.valueOf(str) + "." + i);
        if (file3.exists()) {
            if (i > this.numLastLogsStored) {
                return file3.delete();
            }
            if (!renameOldRecursive(file3, str, i + 1)) {
                return false;
            }
        }
        return file.renameTo(file3);
    }

    public void archiveOldFile(String str) {
        File file = new File(this.directory, str);
        if (file.exists()) {
            renameOldRecursive(file, str, 0);
        }
    }

    public File getNewLogFile(String str) {
        initializeInDirectory();
        if (this.initializedWithErrors) {
            return null;
        }
        File file = new File(this.directory, str);
        if (file.exists() && file.length() > 0) {
            renameOldRecursive(file, str, 0);
        }
        return file;
    }

    public List<File> getAllFiles(String str) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] listFiles = this.directory.listFiles();
        if (listFiles == null) {
            return arrayList;
        }
        for (File file : listFiles) {
            if (file.getName().startsWith(str) && file.length() > 0) {
                arrayList.add(file);
            }
        }
        return arrayList;
    }
}
