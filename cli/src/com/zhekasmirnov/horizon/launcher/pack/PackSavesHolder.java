package com.zhekasmirnov.horizon.launcher.pack;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

public class PackSavesHolder {
    public static final String ARCHIVE_PATH = "games/horizon/archived-saves";
    public static final Object LOCK = new Object();
    public final File packDirectory;
    public final File packSavesDirectory;
    public final File watchedSavesDirectory;

    public PackSavesHolder(File file, File file2, File file3) {
        this.packDirectory = file;
        this.packSavesDirectory = file2;
        this.watchedSavesDirectory = file3;
    }

    public static class PackSavesInfo {
        public final File packDirectory;
        public final File packSavesDirectory;
        public final String uuid;

        private PackSavesInfo(File file, File file2, String str) {
            this.packDirectory = file;
            this.packSavesDirectory = file2;
            this.uuid = str;
        }

        PackSavesInfo(File file, File file2, String str, PackSavesInfo packSavesInfo) {
            this(file, file2, str);
        }

        public JSONObject toJson() {
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("pack", this.packDirectory.getAbsolutePath());
                jSONObject.put("pack-saves", this.packSavesDirectory.getAbsolutePath());
                jSONObject.put("uuid", this.uuid);
            } catch (JSONException e) {
            }
            return jSONObject;
        }

        public static PackSavesInfo fromJson(JSONObject jSONObject) {
            String optString = jSONObject.optString("pack", null);
            String optString2 = jSONObject.optString("pack-saves", null);
            String optString3 = jSONObject.optString("uuid", null);
            if (optString == null || optString2 == null) {
                return null;
            }
            return new PackSavesInfo(new File(optString), new File(optString2), optString3);
        }

        public static PackSavesInfo fromFile(File file) {
            try {
                return fromJson(FileUtils.readJSON(file));
            } catch (IOException | JSONException e) {
                return null;
            }
        }
    }

    private static void copySaves(File file, File file2, List<String> list) throws IOException {
        if (file.isFile() && !list.contains(file.getName())) {
            FileUtils.copy(file, file2);
        } else if (file.isDirectory()) {
            file2.mkdirs();
            ArrayList<String> arrayList = new ArrayList<>();
            for (File file3 : file.listFiles()) {
                copySaves(file3, new File(file2, file3.getName()), arrayList);
            }
        }
    }

    private boolean pullSavesToDirectory(File file) {
        synchronized (LOCK) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(".horizon");
            try {
                if (file.isDirectory()) {
                    FileUtils.clearFileTree(file, false);
                } else {
                    if (file.exists()) {
                        file.delete();
                    }
                    file.mkdirs();
                }
                copySaves(this.watchedSavesDirectory, file, arrayList);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private File getSavesInfoFile() {
        return new File(this.watchedSavesDirectory, ".horizon/saves-info");
    }

    public boolean pullSavesToStoredDirectory() {
        PackSavesInfo fromFile = PackSavesInfo.fromFile(getSavesInfoFile());
        if (fromFile == null || !fromFile.packDirectory.isDirectory()) {
            return false;
        }
        return pullSavesToDirectory(fromFile.packSavesDirectory);
    }

    public boolean pullSavesForThisPack() {
        PackSavesInfo fromFile = PackSavesInfo.fromFile(getSavesInfoFile());
        if (fromFile == null || !fromFile.packDirectory.equals(this.packDirectory)) {
            return false;
        }
        return pullSavesToDirectory(this.packSavesDirectory);
    }

    private void prepareToPush() {
        if (!pullSavesToStoredDirectory()) {
            archiveSaves();
        }
        if (this.watchedSavesDirectory.isDirectory()) {
            for (File file : this.watchedSavesDirectory.listFiles()) {
                if (!file.getName().startsWith(".horizon")) {
                    if (file.isDirectory()) {
                        FileUtils.clearFileTree(file, true);
                    } else {
                        file.delete();
                    }
                }
            }
            return;
        }
        if (this.watchedSavesDirectory.isFile()) {
            this.watchedSavesDirectory.delete();
        }
        this.watchedSavesDirectory.mkdirs();
    }

    public void pushSavesIfRequired() {
        PackSavesInfo fromFile = PackSavesInfo.fromFile(getSavesInfoFile());
        if (fromFile == null || !fromFile.packDirectory.equals(this.packDirectory)) {
            synchronized (LOCK) {
                this.prepareToPush();
                try {
                    copySaves(this.packSavesDirectory, this.watchedSavesDirectory, new ArrayList<>());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PackSavesInfo packSavesInfo = new PackSavesInfo(this.packDirectory, this.packSavesDirectory, UUID.randomUUID().toString(), null);
                File savesInfoFile = getSavesInfoFile();
                savesInfoFile.getParentFile().mkdirs();
                try {
                    FileUtils.writeJSON(savesInfoFile, packSavesInfo.toJson());
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public void showArchiveInfo(PackSavesInfo packSavesInfo, File file, boolean z) {
        if (z) {
            Logger.info("PackSavesHolder", file.getAbsolutePath());
            Logger.info("PackSavesHolder", this.watchedSavesDirectory.getAbsolutePath());
            return;
        }
        Logger.error("PackSavesHolder", "failed to archive replaced game saves");
    }

    private void showArchiveInfo(final PackSavesInfo packSavesInfo, final File file, final boolean z, final int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(i);
                } catch (InterruptedException e) {
                }
                PackSavesHolder.this.showArchiveInfo(packSavesInfo, file, z);
            }
        }).start();
    }

    public void archiveSaves() {
        File file;
        File file2 = new File(System.getProperty("user.dir"), "games/horizon/archived-saves");
        if (file2.isFile()) {
            file2.delete();
        }
        file2.mkdirs();
        String format = new SimpleDateFormat("_yyyy-MM-dd_HH:mm", Locale.getDefault()).format(new Date());
        File savesInfoFile = getSavesInfoFile();
        PackSavesInfo fromFile = PackSavesInfo.fromFile(savesInfoFile);
        savesInfoFile.delete();
        do {
            StringBuilder sb = new StringBuilder();
            sb.append(fromFile != null ? fromFile.packDirectory.getName() : "unknown");
            sb.append(format);
            sb.append(".");
            sb.append(0);
            sb.append(".zip");
            file = new File(file2, sb.toString());
        } while (file.exists());
        synchronized (LOCK) {
            try {
                if (FileUtils.zipDirectory(this.watchedSavesDirectory, file)) {
                    this.showArchiveInfo(fromFile, file, true, 1000);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
