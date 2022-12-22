package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.modpack.installation.ModPackExtractionTarget;
import com.zhekasmirnov.innercore.modpack.installation.ModpackInstallationSource;
import com.zhekasmirnov.innercore.modpack.strategy.extract.DirectoryExtractStrategy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.json.JSONException;

public class ModPack {
    private final File rootDirectory;
    private final ModPackManifest manifest = new ModPackManifest();
    private final List<ModPackDirectory> defaultDirectories = new ArrayList<>();
    private final List<ModPackDirectory> declaredDirectories = new ArrayList<>();
    private final ModPackPreferences preferences = new ModPackPreferences(this, "preferences.json");
    private final ModPackJsAdapter jsAdapter = new ModPackJsAdapter(this);

    public interface TaskReporter {
        void reportError(String str, Exception exc, boolean z) throws InterruptedException;

        void reportProgress(String str, int i, int i2, int i3) throws InterruptedException;

        void reportResult(boolean z);
    }

    public ModPack(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public ModPack addDirectory(ModPackDirectory directory) {
        directory.assignToModPack(this);
        this.defaultDirectories.add(directory);
        return this;
    }

    public File getRootDirectory() {
        return this.rootDirectory;
    }

    public File getManifestFile() {
        return new File(this.rootDirectory, "modpack.json");
    }

    public File getIconFile() {
        return new File(this.rootDirectory, "pack_icon.png");
    }

    public ModPackManifest getManifest() {
        return this.manifest;
    }

    public ModPackPreferences getPreferences() {
        return this.preferences;
    }

    public ModPackJsAdapter getJsAdapter() {
        return this.jsAdapter;
    }

    public boolean reloadAndValidateManifest() {
        try {
            this.declaredDirectories.clear();
            this.manifest.loadFile(getManifestFile());
            this.declaredDirectories.addAll(this.manifest.createDeclaredDirectoriesForModPack(this));
            return this.manifest.getPackName() != null;
        } catch (IOException | JSONException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public List<ModPackDirectory> getAllDirectories() {
        List<ModPackDirectory> directories = new ArrayList<>(this.defaultDirectories);
        directories.addAll(this.declaredDirectories);
        return directories;
    }

    public List<ModPackDirectory> getDirectoriesOfType(ModPackDirectory.DirectoryType type) {
        List<ModPackDirectory> result = new ArrayList<>();
        for (ModPackDirectory directory : this.defaultDirectories) {
            if (type == directory.getType()) {
                result.add(directory);
            }
        }
        for (ModPackDirectory directory2 : this.declaredDirectories) {
            if (type == directory2.getType()) {
                result.add(directory2);
            }
        }
        return result;
    }

    public ModPackDirectory getDirectoryOfType(ModPackDirectory.DirectoryType type) {
        List<ModPackDirectory> directories = getDirectoriesOfType(type);
        if (directories.size() > 0) {
            return directories.get(0);
        }
        return null;
    }

    public DirectorySetRequestHandler getRequestHandler(ModPackDirectory.DirectoryType type) {
        return new DirectorySetRequestHandler(getDirectoriesOfType(type));
    }

    public static void interruptTask(Exception exception, String message) throws InterruptedException {
        throw ((InterruptedException) new InterruptedException(message).initCause(exception));
    }

    public static void interruptTask(String message) throws InterruptedException {
        throw new InterruptedException(message);
    }

    public synchronized void installOrUpdate(ModpackInstallationSource source, TaskReporter reporter) throws InterruptedException {
        Enumeration<ModpackInstallationSource.Entry> entries = source.entries();
        try {
            File manifestFile = getManifestFile();
            manifestFile.getParentFile().mkdirs();
            FileUtils.writeFileText(manifestFile, source.getManifestContent());
            this.manifest.loadFile(manifestFile);
            reporter.reportProgress("loaded manifest", 0, 1, 1);
        } catch (IOException | JSONException exception) {
            reporter.reportResult(false);
            reporter.reportError("failed to get pack manifest", exception, true);
            interruptTask(exception, "failed to get pack manifest");
        }
        List<ModPackDirectory> directories = new ArrayList<>(this.defaultDirectories);
        directories.addAll(this.manifest.createDeclaredDirectoriesForModPack(this));
        int progress = 0;
        for (ModPackDirectory directory : directories) {
            try {
                progress++;
                reporter.reportProgress("preparing directory " + directory, 1, progress, directories.size());
                directory.getUpdateStrategy().beginUpdate();
            } catch (IOException exception2) {
                int progress2 = progress;
                reporter.reportError("failed to begin installation for directory " + directory, exception2, false);
                progress = progress2;
            }
        }
        int progress3 = 0;
        int entryCount = source.getEntryCount();
        while (entries.hasMoreElements()) {
            ModpackInstallationSource.Entry entry = entries.nextElement();
            String name = entry.getName();
            progress3++;
            reporter.reportProgress("updating entry " + name, 2, progress3, entryCount);
            for (ModPackDirectory directory2 : directories) {
                String localPath = directory2.getLocalPathFromEntry(name);
                if (localPath != null) {
                    try {
                        InputStream inputStream = entry.getInputStream();
                        directory2.getUpdateStrategy().updateFile(localPath, inputStream);
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException exception3) {
                        reporter.reportError("failed to update entry " + name + " (local path " + localPath + ") in directory " + directory2, exception3, false);
                    }
                }
            }
        }
        int progress4 = 0;
        for (ModPackDirectory directory3 : directories) {
            try {
                progress4++;
                reporter.reportProgress("completing directory " + directory3, 3, progress4, directories.size());
                directory3.getUpdateStrategy().finishUpdate();
            } catch (IOException exception4) {
                int progress5 = progress4;
                reporter.reportError("failed to complete installation for directory " + directory3, exception4, false);
                progress4 = progress5;
            }
        }
        reporter.reportResult(true);
    }

    public synchronized void extract(ModPackExtractionTarget target, TaskReporter reporter) throws InterruptedException {
        ModPackExtractionTarget modPackExtractionTarget = target;
        reloadAndValidateManifest();
        try {
            reporter.reportProgress("extracting modpack", 0, 0, 1);
            modPackExtractionTarget.writeFile("modpack.json", getManifestFile());
            File iconFile = getIconFile();
            if (iconFile.exists()) {
                modPackExtractionTarget.writeFile("pack_icon.png", iconFile);
            }
        } catch (IOException exception) {
            reporter.reportError("failed to extract manifest", exception, true);
            interruptTask(exception, "failed to extract manifest");
        }
        List<ModPackDirectory> directories = new ArrayList<>(this.defaultDirectories);
        directories.addAll(this.declaredDirectories);
        int stage = 0;
        for (ModPackDirectory directory : directories) {
            DirectoryExtractStrategy extractStrategy = directory.getExtractStrategy();
            List<File> filesToExtract = extractStrategy.getFilesToExtract();
            int index = 0;
            stage++;
            for (File file : filesToExtract) {
                String entry = extractStrategy.getFullEntryName(file);
                try {
                    index++;
                    reporter.reportProgress("extracting entry " + entry, stage, index, filesToExtract.size());
                    modPackExtractionTarget.writeFile(entry, file);
                } catch (IOException exception2) {
                    reporter.reportError("exception in extracting entry " + entry, exception2, false);
                }
                modPackExtractionTarget = target;
            }
            modPackExtractionTarget = target;
        }
    }
}
