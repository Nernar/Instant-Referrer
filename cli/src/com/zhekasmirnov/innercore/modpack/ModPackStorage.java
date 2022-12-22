package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.modpack.installation.ExternalZipFileInstallationSource;
import com.zhekasmirnov.innercore.modpack.installation.ModpackInstallationSource;
import com.zhekasmirnov.innercore.modpack.installation.ZipFileExtractionTarget;
import com.zhekasmirnov.innercore.modpack.installation.ZipFileInstallationSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipFile;
import org.json.JSONException;

public class ModPackStorage {
    private final ModPack defaultModPack;
    private final File defaultModPackDirectory;
    private final File packsArchiveDirectory;
    private final File packsDirectory;
    private final ModPackContext context = ModPackContext.getInstance();
    private final ModPackFactory factory = ModPackFactory.getInstance();
    private final List<ModPack> modPacks = new ArrayList<>();

    public ModPackStorage(File packsDirectory, File packsArchiveDirectory, File defaultModPackDirectory) {
        this.packsDirectory = packsDirectory;
        this.packsArchiveDirectory = packsArchiveDirectory;
        this.defaultModPackDirectory = defaultModPackDirectory;
        packsDirectory.mkdirs();
        this.defaultModPack = this.factory.createDefault(defaultModPackDirectory);
    }

    public synchronized void rebuildModPackList() {
        this.modPacks.clear();
        File[] filesInDir = this.packsDirectory.listFiles();
        if (filesInDir != null) {
            for (File packDir : filesInDir) {
                if (packDir.isDirectory()) {
                    ModPack modPack = this.factory.createFromDirectory(packDir);
                    if (modPack.reloadAndValidateManifest()) {
                        this.modPacks.add(modPack);
                    }
                }
            }
        }
    }

    public ModPackContext getContext() {
        return this.context;
    }

    public File getPacksDirectory() {
        return this.packsDirectory;
    }

    public File getPacksArchiveDirectory() {
        return this.packsArchiveDirectory;
    }

    public ModPack getDefaultModPack() {
        return this.defaultModPack;
    }

    public File getDefaultModPackDirectory() {
        return this.defaultModPackDirectory;
    }

    public List<ModPack> getNonDefaultModPacks() {
        return this.modPacks;
    }

    public List<ModPack> getAllModPacks() {
        List<ModPack> result = new ArrayList<>();
        result.add(this.defaultModPack);
        result.addAll(getNonDefaultModPacks());
        return result;
    }

    public boolean isDefaultModPack(ModPack modPack) {
        return this.defaultModPackDirectory.equals(modPack.getRootDirectory());
    }

    private static String normalizeFileName(String name) {
        if (name == null || name.equals("")) {
            return "unnamed";
        }
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private static String getAvailablePackFileName(ModPackManifest manifest, Predicate<String> checkAvailable) {
        String name = normalizeFileName(manifest.getPackName());
        if (!checkAvailable.test(name)) {
            String name2 = String.valueOf(name) + "-" + normalizeFileName(manifest.getDisplayedName());
            if (!checkAvailable.test(name2)) {
                int index = 0;
                while (!checkAvailable.test(String.valueOf(name2) + "-" + index)) {
                    index++;
                }
                return String.valueOf(name2) + "-" + index;
            }
            return name2;
        }
        return name;
    }

    public ModPack installNewModPack(ModpackInstallationSource source, ModPack.TaskReporter taskReporter) throws InterruptedException {
        try {
            ModPackManifest manifest = source.getTempManifest();
            File packDirectory = new File(this.packsDirectory, getAvailablePackFileName(manifest, new Predicate<String>() {
                @Override
                public final boolean test(String obj) {
                    return ModPackStorage.lambda$installNewModPack$0(ModPackStorage.this, obj);
                }
            }));
            packDirectory.mkdirs();
            if (!packDirectory.isDirectory()) {
                taskReporter.reportError("failed to create pack directory", new IOException(), true);
                ModPack.interruptTask("failed to create pack directory");
            }
            ModPack modPack = ModPackFactory.getInstance().createFromDirectory(packDirectory);
            modPack.installOrUpdate(source, taskReporter);
            if (modPack.reloadAndValidateManifest()) {
                this.modPacks.add(modPack);
            } else {
                rebuildModPackList();
            }
            return modPack;
        } catch (IOException | JSONException exception) {
            taskReporter.reportError("failed to get manifest from installation source", exception, true);
            ModPack.interruptTask(exception, "failed to get manifest");
            return null;
        }
    }

    static boolean lambda$installNewModPack$0(ModPackStorage modPackStorage, String name) {
        return !new File(modPackStorage.packsDirectory, name).exists();
    }

    public ModPack installNewModPack(InputStream inputStream, ModPack.TaskReporter taskReporter) throws InterruptedException {
        try {
            ExternalZipFileInstallationSource installationSource = new ExternalZipFileInstallationSource(inputStream);
            ModPack installNewModPack = installNewModPack(installationSource, taskReporter);
            if (installationSource != null) {
                installationSource.close();
            }
            return installNewModPack;
        } catch (IOException exception) {
            taskReporter.reportError("failed to create installation source", exception, false);
            ModPack.interruptTask(exception, "failed to create installation source");
            return null;
        }
    }

    public File archivePack(ModPack modPack, ModPack.TaskReporter taskReporter) throws InterruptedException {
        if (!modPack.reloadAndValidateManifest() || modPack.getManifest().getPackName() == null) {
            ModPack.interruptTask("failed to load pack manifest");
        }
        final File file = this.packsArchiveDirectory;
        File archiveFile = new File(file, String.valueOf(getAvailablePackFileName(modPack.getManifest(), new Predicate<String>() {
            @Override
            public final boolean test(String obj) {
                return file == ModPackStorage.this.packsArchiveDirectory;
            }
        })) + ".zip");
        archiveFile.getParentFile().mkdirs();
        try {
            ZipFileExtractionTarget extractionTarget = new ZipFileExtractionTarget(archiveFile);
            modPack.extract(extractionTarget, taskReporter);
            if (extractionTarget != null) {
                extractionTarget.close();
            }
        } catch (IOException exception) {
            archiveFile.delete();
            ModPack.interruptTask(exception, "failed to create extraction target");
        }
        taskReporter.reportResult(true);
        return archiveFile;
    }

    public void deletePack(ModPack modPack) {
        if (isDefaultModPack(modPack)) {
            throw new IllegalArgumentException("default modpack cannot be deleted");
        }
        this.modPacks.remove(modPack);
        FileUtils.clearFileTree(modPack.getRootDirectory(), true);
    }

    public File archiveAndDeletePack(ModPack modPack, ModPack.TaskReporter taskReporter) throws InterruptedException {
        if (isDefaultModPack(modPack)) {
            throw new IllegalArgumentException("default modpack cannot be deleted");
        }
        File archive = archivePack(modPack, taskReporter);
        FileUtils.clearFileTree(modPack.getRootDirectory(), true);
        return archive;
    }

    public ModPack unarchivePack(File archivedPackFile, ModPack.TaskReporter taskReporter, boolean deleteArchiveFile) throws InterruptedException {
        try {
            ZipFileInstallationSource installationSource = new ZipFileInstallationSource(new ZipFile(archivedPackFile));
            ModPack modPack = installNewModPack(installationSource, taskReporter);
            if (deleteArchiveFile) {
                archivedPackFile.delete();
            }
            return modPack;
        } catch (IOException exception) {
            taskReporter.reportError("failed to create installation source", exception, true);
            ModPack.interruptTask(exception, "failed to create installation source");
            return null;
        }
    }

    public List<File> getAllArchivedPacks() {
        List<File> result = new ArrayList<>();
        File[] archivedFiles = this.packsArchiveDirectory.listFiles();
        if (archivedFiles != null) {
            result.addAll(Arrays.asList(archivedFiles));
        }
        return result;
    }
}
