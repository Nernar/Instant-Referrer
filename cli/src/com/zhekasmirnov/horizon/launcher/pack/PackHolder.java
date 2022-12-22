package com.zhekasmirnov.horizon.launcher.pack;

import com.zhekasmirnov.horizon.activity.util.DialogHelper;
import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.launcher.ContextHolder;
import com.zhekasmirnov.horizon.modloader.ModContext;
import com.zhekasmirnov.horizon.modloader.repo.ModList;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.runtime.task.TaskSequence;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONException;

public class PackHolder {
    @SuppressWarnings("unused")
    private static final String FLAG_INSTALLATION_COMPLETE = "installation_complete";
    @SuppressWarnings("unused")
    private static final String FLAG_INSTALLATION_STARTED = "installation_started";
    @SuppressWarnings("unused")
    private static final int FREE_SPACE_REQUIRED = 419430400;
    private static final List<PackHolder> loadedPackHolders = new ArrayList<>();
    public final PackDirectory packDirectory;
    public final PackStorage storage;
    public final List<PackSavesHolder> savesHolders = new ArrayList<>();
    private State state = State.NOT_INITIALIZED;
    private File installationDir = null;
    private Pack pack = null;
    private boolean isUpdateAvailable = false;
    private Object graphics = null;
    private boolean isPreparedForLaunch = false;

    enum InstallationResult {
        SUCCESS,
        ABORT,
        ERROR;

        public static InstallationResult[] valuesCustom() {
            InstallationResult[] valuesCustom = values();
            int length = valuesCustom.length;
            InstallationResult[] installationResultArr = new InstallationResult[length];
            System.arraycopy(valuesCustom, 0, installationResultArr, 0, length);
            return installationResultArr;
        }
    }

    public enum State {
        NOT_INITIALIZED,
        NOT_INSTALLED,
        PENDING,
        CORRUPT,
        INSTALLED;

        public static State[] valuesCustom() {
            State[] valuesCustom = values();
            int length = valuesCustom.length;
            State[] stateArr = new State[length];
            System.arraycopy(valuesCustom, 0, stateArr, 0, length);
            return stateArr;
        }
    }

    public PackHolder(PackStorage packStorage, PackDirectory packDirectory) {
        this.storage = packStorage;
        this.packDirectory = packDirectory;
        refreshSavesHolders(false);
    }

    private void refreshSavesHolders(boolean z) {
        PackManifest localManifest;
        this.savesHolders.clear();
        this.savesHolders.add(new PackSavesHolder(this.packDirectory.directory, new File(this.packDirectory.directory, "saves/data"), new File(Environment.getDataDirFile(), "cache")));
        if (!z || (localManifest = this.packDirectory.getLocalManifest()) == null) {
            return;
        }
        for (String str : localManifest.savesHoldersInfo.keySet()) {
            String str2 = localManifest.savesHoldersInfo.get(str);
            if (str2 != null) {
                List<PackSavesHolder> list = this.savesHolders;
                File file = this.packDirectory.directory;
                File file2 = this.packDirectory.directory;
                list.add(new PackSavesHolder(file, new File(file2, "saves/" + str), new File(str2)));
            }
        }
    }

    public PackManifest getManifest() {
        try {
            PackManifest manifest = this.packDirectory.getManifest();
            if (manifest != null || this.installationDir == null) {
                return manifest;
            }
            try {
                return new PackManifest(new File(this.installationDir, "manifest.json"));
            } catch (IOException | JSONException e) {
                return manifest;
            } catch (RuntimeException e2) {
                e2.printStackTrace();
                return manifest;
            }
        } catch (Exception e22) {
            e22.printStackTrace();
            return null;
        }
    }

    public void runRefreshUpdateInfoTask(final Runnable runnable) {
        getContextHolder().getTaskManager().addTask(new TaskSequence.AnonymousTask() {
            @Override
            public void run() {
                PackHolder packHolder = PackHolder.this;
                packHolder.isUpdateAvailable = packHolder.packDirectory.isUpdateAvailable();
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
            }
        });
    }

    public void refreshUpdateInfoNow() {
        this.isUpdateAvailable = this.packDirectory.isUpdateAvailable();
    }

    public boolean isUpdateAvailable() {
        return this.isUpdateAvailable;
    }

    @SuppressWarnings("unused")
    private Object tryToLoadGraphics(InputStream inputStream) {
        return null;
    }

    public void pullAllSaves() {
        refreshSavesHolders(true);
        for (PackSavesHolder packSavesHolder : this.savesHolders) {
            packSavesHolder.pullSavesForThisPack();
        }
    }

    public void pushAllSaves() {
        refreshSavesHolders(true);
        for (PackSavesHolder packSavesHolder : this.savesHolders) {
            packSavesHolder.pushSavesIfRequired();
        }
    }

    public synchronized Object getGraphics() {
        return this.graphics;
    }

    public void initialize() {
        File file = this.packDirectory.directory;
        if (file.exists()) {
            if (file.isDirectory()) {
                if (FileUtils.getFileFlag(file, "installation_complete")) {
                    this.state = State.INSTALLED;
                } else if (FileUtils.getFileFlag(file, "installation_started")) {
                    this.state = State.CORRUPT;
                } else {
                    this.state = State.PENDING;
                }
            } else {
                throw new RuntimeException("pack location returned non-directory file as local dir: " + file);
            }
        } else {
            this.state = State.NOT_INSTALLED;
        }
        this.installationDir = file;
    }

    public void uninstall(List<String> list) {
        File file = this.installationDir;
        if (file == null || !file.exists()) {
            return;
        }
        for (File file2 : this.installationDir.listFiles()) {
            if (!list.contains(file2.getName())) {
                Logger.debug("PackHolder", "deleting " + file2);
                if (file2.isDirectory()) {
                    FileUtils.clearFileTree(file2, true);
                } else {
                    file2.delete();
                }
            }
        }
        this.state = State.NOT_INSTALLED;
    }

    public void deletePack() {
        uninstall(new ArrayList<>());
        this.installationDir.delete();
    }

    private static String beautifyUnpackMessage(String str) {
        return str.replaceAll("\\.dex", ".zip").replaceAll("classes", "resources").replaceAll("lib", "").replaceAll("\\.so", ".zip").replaceAll("so/", "").replaceAll("armeabi-v7a", "1").replaceAll("x86", "2");
    }

    public PackHolder.InstallationResult install(List<String> list) {
        if (this.installationDir == null || this.state == State.NOT_INITIALIZED) {
            throw new RuntimeException("installing pack without completed initialization");
        }
        DialogHelper.ProgressDialogHolder progressDialogHolder = new DialogHelper.ProgressDialogHolder(2131624078, 2131624081);
        progressDialogHolder.open();
        progressDialogHolder.setText(2131624093);
        if (getFreeSpace() < 419430400) {
            this.state = State.NOT_INSTALLED;
            return InstallationResult.ERROR;
        }
        InputStream installationPackageStream = this.packDirectory.getInstallationPackageStream();
        if (installationPackageStream != null) {
            this.installationDir.mkdirs();
            File file = new File(this.installationDir, ".installation_package");
            if (file.exists()) {
                file.delete();
            }
            this.packDirectory.updateLocalUUID();
            progressDialogHolder.setText(2131624088);
            try {
                int installationPackageSize = this.packDirectory.getInstallationPackageSize();
                byte[] bArr = new byte[8192];
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                int i = 0;
                int i2 = 0;
                while (true) {
                    int read = installationPackageStream.read(bArr);
                    if (read > 0) {
                        fileOutputStream.write(bArr, i, read);
                        int i3 = i2 + read;
                        double d2 = i3;
                        double d3 = installationPackageSize;
                        Double.isNaN(d2);
                        Double.isNaN(d3);
                        progressDialogHolder.onProgress(d2 / d3);
                        progressDialogHolder.onDownloadMessage("Downloading: " + Math.round((d2 / d3) * 100.0d) + "%");
                        if (progressDialogHolder.isTerminated()) {
                            fileOutputStream.close();
                            installationPackageStream.close();
                            file.delete();
                            progressDialogHolder.close();
                            return InstallationResult.ABORT;
                        }
                        i2 = i3;
                        i = 0;
                    } else {
                        installationPackageStream.close();
                        fileOutputStream.close();
                        progressDialogHolder.setText(2131624094);
                        this.state = State.PENDING;
                        List<String> buildReinstallWhitelist = buildReinstallWhitelist(true);
                        buildReinstallWhitelist.add(".installation_package");
                        uninstall(buildReinstallWhitelist);
                        FileUtils.setFileFlag(this.installationDir, "installation_started", true);
                        FileUtils.setFileFlag(this.installationDir, "installation_complete", false);
                        this.state = State.CORRUPT;
                        try {
                            progressDialogHolder.setText(2131624099);
                            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
                            do {
                                ZipEntry nextEntry = zipInputStream.getNextEntry();
                                if (nextEntry != null) {
                                    if (!nextEntry.isDirectory()) {
                                        progressDialogHolder.onDownloadMessage("Unpacking: " + beautifyUnpackMessage(nextEntry.getName()));
                                        File file2 = new File(this.installationDir, nextEntry.getName());
                                        file2.getParentFile().mkdirs();
                                        FileUtils.unpackInputStream(zipInputStream, file2, false);
                                    }
                                } else {
                                    FileUtils.setFileFlag(this.installationDir, "installation_complete", true);
                                    progressDialogHolder.setText(2131624090);
                                    this.graphics = null;
                                    getGraphics();
                                }
                            } while (!progressDialogHolder.isTerminated());
                            FileUtils.setFileFlag(this.installationDir, "installation_started", false);
                            progressDialogHolder.setText(2131624082);
                            uninstall(list);
                            progressDialogHolder.close();
                            return InstallationResult.ABORT;
                        } catch (IOException e) {
                            progressDialogHolder.close();
                            e.printStackTrace();
                            return InstallationResult.ERROR;
                        }
                    }
                }
            } catch (IOException e2) {
                progressDialogHolder.close();
                e2.printStackTrace();
                return InstallationResult.ERROR;
            }
        } else {
            FileUtils.setFileFlag(this.installationDir, "installation_started", true);
            FileUtils.setFileFlag(this.installationDir, "installation_complete", true);
        }
        progressDialogHolder.close();
        this.state = State.INSTALLED;
        return InstallationResult.SUCCESS;
    }

    public InstallationResult reinstall() {
        if (this.installationDir == null || this.state == State.NOT_INITIALIZED) {
            throw new RuntimeException("installing pack without completed initialization");
        }
        pullAllSaves();
        List<String> buildReinstallWhitelist = buildReinstallWhitelist(false);
        buildReinstallWhitelist.add(".installation_package");
        uninstall(buildReinstallWhitelist);
        File file = new File(this.installationDir, ".installation_package");
        if (file.exists()) {
            DialogHelper.ProgressDialogHolder progressDialogHolder = new DialogHelper.ProgressDialogHolder(null, 2131624078, 2131624081);
            progressDialogHolder.open();
            FileUtils.setFileFlag(this.installationDir, "installation_complete", false);
            FileUtils.setFileFlag(this.installationDir, "installation_started", true);
            try {
                progressDialogHolder.setText(2131624096);
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
                do {
                    ZipEntry nextEntry = zipInputStream.getNextEntry();
                    if (nextEntry != null) {
                        if (!nextEntry.isDirectory()) {
                            progressDialogHolder.onDownloadMessage("Unpacking: " + nextEntry.getName());
                            File file2 = new File(this.installationDir, nextEntry.getName());
                            file2.getParentFile().mkdirs();
                            FileUtils.unpackInputStream(zipInputStream, file2, false);
                        }
                    } else {
                        FileUtils.setFileFlag(this.installationDir, "installation_complete", true);
                        this.state = State.INSTALLED;
                        progressDialogHolder.close();
                        return InstallationResult.SUCCESS;
                    }
                } while (!progressDialogHolder.isTerminated());
                FileUtils.setFileFlag(this.installationDir, "installation_started", false);
                progressDialogHolder.setText(2131624082);
                uninstall(buildReinstallWhitelist);
                progressDialogHolder.close();
                return InstallationResult.ABORT;
            } catch (IOException e) {
                e.printStackTrace();
                progressDialogHolder.close();
            }
        }
        if (DialogHelper.awaitDecision(2131624014, 2131624089, 2131624110, 17039360)) {
            return install(buildReinstallWhitelist);
        }
        return InstallationResult.ABORT;
    }

    private List<String> buildReinstallWhitelist(boolean z) {
        PackManifest externalManifest;
        ArrayList<String> arrayList = new ArrayList<>();
        if (z && (externalManifest = this.packDirectory.getExternalManifest()) != null) {
            arrayList.addAll(externalManifest.keepDirectories);
        }
        PackManifest localManifest = this.packDirectory.getLocalManifest();
        if (localManifest != null) {
            arrayList.addAll(localManifest.keepDirectories);
        }
        arrayList.add("mods");
        arrayList.add("native_mods");
        arrayList.add("saves");
        arrayList.add("worlds");
        arrayList.add("config");
        arrayList.add("visual");
        arrayList.add(".installation_package");
        arrayList.add(".installation_info");
        return arrayList;
    }

    public InstallationResult update() {
        if (this.installationDir == null || this.state == State.NOT_INITIALIZED) {
            throw new RuntimeException("installing pack without completed initialization");
        }
        pullAllSaves();
        InstallationResult install = install(buildReinstallWhitelist(true));
        if (install == InstallationResult.SUCCESS) {
            this.packDirectory.reloadLocalManifest();
            this.isUpdateAvailable = false;
        }
        return install;
    }

    public PackHolder clone(PackRepository packRepository, String str) {
        if (this.state != State.INSTALLED) {
            return null;
        }
        pullAllSaves();
        DialogHelper.ProgressDialogHolder progressDialogHolder = new DialogHelper.ProgressDialogHolder(2131624078, 2131623982);
        progressDialogHolder.setText(2131624086);
        progressDialogHolder.open();
        if (getFreeSpace() < 419430400) {
            progressDialogHolder.close();
            return null;
        }
        PackDirectory makeNewPackDirectory = this.storage.makeNewPackDirectory(str);
        if (makeNewPackDirectory == null) {
            progressDialogHolder.close();
            return null;
        }
        try {
            FileUtils.copyFileTree(this.installationDir, makeNewPackDirectory.directory, progressDialogHolder, "");
            if (progressDialogHolder.isTerminated()) {
                FileUtils.clearFileTree(makeNewPackDirectory.directory, true);
                progressDialogHolder.close();
                return null;
            }
            this.storage.fetchLocationsFromRepo(packRepository);
            PackHolder loadPackHolderFromDirectory = this.storage.loadPackHolderFromDirectory(makeNewPackDirectory);
            if (loadPackHolderFromDirectory != null) {
                loadPackHolderFromDirectory.packDirectory.generateNewInternalID();
                loadPackHolderFromDirectory.packDirectory.updateTimestamp();
                loadPackHolderFromDirectory.packDirectory.setCustomName(str);
                progressDialogHolder.close();
                return loadPackHolderFromDirectory;
            }
            FileUtils.clearFileTree(makeNewPackDirectory.directory, true);
            progressDialogHolder.close();
            return null;
        } catch (IOException e) {
            FileUtils.clearFileTree(makeNewPackDirectory.directory, true);
            progressDialogHolder.close();
            return null;
        }
    }

    public void deselectAndUnload() {
        synchronized (loadedPackHolders) {
            if (loadedPackHolders.contains(this)) {
                loadedPackHolders.remove(this);
                if (this.pack != null) {
                    this.pack.unload();
                }
            }
        }
    }

    public State getState() {
        return this.state;
    }

    public File getInstallationDir() {
        return this.installationDir;
    }

    public PackStorage getStorage() {
        return this.storage;
    }

    public String getPackUUID() {
        return this.packDirectory.getUUID();
    }

    public String getInternalPackID() {
        return this.packDirectory.getInternalID();
    }

    public ContextHolder getContextHolder() {
        return this.storage.contextHolder;
    }

    public ModContext getModContext() {
        Pack pack = this.pack;
        if (pack != null) {
            return pack.modContext;
        }
        return null;
    }

    public ModList getModList() {
        Pack pack = this.pack;
        if (pack != null) {
            return pack.modList;
        }
        return null;
    }

    public ResourceManager getResourceManager() {
        Pack pack = this.pack;
        if (pack != null) {
            return pack.resourceManager;
        }
        return null;
    }

    public boolean isLoaded() {
        return loadedPackHolders.contains(this);
    }

    public boolean selectAndLoadPack() {
        initialize();
        if (this.state != State.INSTALLED) {
            InstallationResult installationResult = InstallationResult.ABORT;
            if (this.state == State.CORRUPT) {
                if (DialogHelper.awaitDecision(2131624014, 2131624080, 2131624110, 17039360)) {
                    installationResult = reinstall();
                }
            } else if (DialogHelper.awaitDecision(2131624014, 2131624079, 2131624110, 17039360)) {
                List<String> buildReinstallWhitelist = buildReinstallWhitelist(true);
                buildReinstallWhitelist.add(".installation_info");
                buildReinstallWhitelist.add("manifest.json");
                installationResult = install(buildReinstallWhitelist);
            }
            if (installationResult != InstallationResult.SUCCESS) {
                return false;
            }
        }
        if (this.packDirectory.getLocalManifest() == null) {
            return false;
        }
        while (loadedPackHolders.size() > 0) {
            loadedPackHolders.get(0).deselectAndUnload();
        }
        synchronized (loadedPackHolders) {
            loadedPackHolders.add(this);
            Pack pack = new Pack(this.storage.contextHolder, this.installationDir);
            this.pack = pack;
            pack.initialize();
            pushAllSaves();
            List<EventLogger.Message> messages = pack.modContext.getEventLogger().getMessages(new EventLogger.Filter() {
                @Override
                public boolean filter(EventLogger.Message message) {
                    return message.type == EventLogger.MessageType.EXCEPTION || message.type == EventLogger.MessageType.FAULT;
                }
            });
            pack.modContext.getEventLogger().clear();
            for (EventLogger.Message message : messages) {
                Logger.error(message.tag, message.message);
            }
            return true;
        }
    }

    public Pack getPack() {
        return this.pack;
    }

    public boolean isPreparedForLaunch() {
        return this.isPreparedForLaunch;
    }

    public synchronized void prepareForLaunch() {
        if (!this.isPreparedForLaunch && this.pack != null) {
            this.pack.load();
            this.isPreparedForLaunch = true;
        }
    }

    public void showDialogWithPackInfo(Object activity, boolean z) {
        String str;
        String str2;
        PackManifest manifest = getManifest();
        PackManifest externalManifest = z ? this.packDirectory.getExternalManifest() : null;
        IPackLocation location = this.packDirectory.getLocation();
        String changelog = (!z || location == null) ? null : location.getChangelog();
        String packUUID = getPackUUID();
        State state = getState();
        StringBuilder sb = new StringBuilder();
        sb.append(manifest.pack);
        String str3 = "";
        if (manifest.packVersion != null) {
            str = " " + manifest.packVersion;
        } else {
            str = "";
        }
        sb.append(str);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(manifest.description);
        sb2.append("\n\nGame: ");
        sb2.append(manifest.game);
        if (manifest.gameVersion != null) {
            str2 = " " + manifest.gameVersion;
        } else {
            str2 = "";
        }
        sb2.append(str2);
        sb2.append("\nVersion: ");
        sb2.append(manifest.packVersion != null ? manifest.packVersion : Integer.valueOf(manifest.packVersionCode));
        if (externalManifest != null) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(", newest is ");
            sb3.append(externalManifest.packVersion != null ? externalManifest.packVersion : Integer.valueOf(externalManifest.packVersionCode));
            str3 = sb3.toString();
        }
        sb2.append(str3);
        sb2.append("\nState: ");
        sb2.append(state.toString());
        sb2.append("\nLocal directory: ");
        sb2.append(this.packDirectory.directory.getName());
        sb2.append("\nExternal UUID: ");
        if (packUUID == null) {
            packUUID = "this is local pack";
        }
        sb2.append(packUUID);
        sb2.append("\n");
        Logger.info("PackHolder", sb.toString());
        Logger.info("PackHolder", sb2.toString());
        showChangelogDialog(activity, changelog, null);
    }

    private void showChangelogDialog(Object activity, String str, Runnable runnable) {
        if (str == null || str.length() <= 0) {
            return;
        }
        Logger.info("PackHolder", "Changelog:\n" + str);
        if (runnable != null) {
            runnable.run();
        }
    }

    public void showDialogWithChangelog(Object activity, Runnable runnable) {
        IPackLocation location = this.packDirectory.getLocation();
        String changelog = location != null ? location.getChangelog() : null;
        if (changelog == null || changelog.length() <= 0) {
            return;
        }
        showChangelogDialog(activity, changelog, runnable);
    }

    private long getFreeSpace() {
        return Long.MAX_VALUE;
    }
}
