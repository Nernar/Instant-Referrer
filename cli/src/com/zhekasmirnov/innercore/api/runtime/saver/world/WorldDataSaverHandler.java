package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.OperationTimeLogger;
import java.io.File;

public class WorldDataSaverHandler {
    private static final WorldDataSaverHandler instance = new WorldDataSaverHandler();
    private WorldDataSaver worldDataSaver = null;
    private boolean autoSaveEnabled = true;
    private boolean autoSaveMinecraftWorld = true;
    private int autoSaveInterval = 30000;
    private boolean saveWasQueued = false;
    private long previousAutoSave = 0;

    public static WorldDataSaverHandler getInstance() {
        return instance;
    }

    public void fetchParamsFromConfig() {
        setParams(EngineConfig.getBoolean("background.auto_save", true), EngineConfig.getBoolean("background.auto_save_world", true), EngineConfig.getInt("background.auto_save_period", 30) * 1000);
    }

    public void setParams(boolean autoSaveEnabled, boolean autoSaveMinecraftWorld, int autoSaveInterval) {
        this.autoSaveEnabled = autoSaveEnabled;
        this.autoSaveMinecraftWorld = autoSaveMinecraftWorld;
        this.autoSaveInterval = autoSaveInterval;
    }

    public void queueSave() {
        this.saveWasQueued = true;
    }

    public void onLevelSelected(File worldDirectory) {
        ICLog.d("WorldDataSaverHandler", "level selected: " + worldDirectory.getAbsolutePath());
        initSaverOnNewWorldLoad(worldDirectory);
    }

    public void onConnectedToRemoteWorld() {
        initSaverOnNewWorldLoad(null);
    }

    public void onLevelLoading() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("reading data: ");
        if (this.worldDataSaver != null) {
            str = "dir=" + this.worldDataSaver.getWorldDirectory();
        } else {
            str = "save is null";
        }
        sb.append(str);
        ICLog.d("WorldDataSaverHandler", sb.toString());
        readDataOnLoad();
        this.previousAutoSave = System.currentTimeMillis();
    }

    public void onLevelLeft() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("level left: ");
        if (this.worldDataSaver != null) {
            str = "dir=" + this.worldDataSaver.getWorldDirectory();
        } else {
            str = "save is null";
        }
        sb.append(str);
        ICLog.d("WorldDataSaverHandler", sb.toString());
        saveAndReleaseSaver();
    }

    public void onPauseScreenOpened() {
        queueSave();
    }

    public void onTick() {
        if (this.saveWasQueued) {
            this.saveWasQueued = false;
            runWorldAndDataSave();
        }
        if (this.autoSaveEnabled && this.autoSaveInterval + this.previousAutoSave < System.currentTimeMillis()) {
            this.previousAutoSave = System.currentTimeMillis();
            runWorldAndDataSave();
        }
    }

    public synchronized WorldDataSaver getWorldDataSaver() {
        return this.worldDataSaver;
    }

    private synchronized WorldDataSaver initSaverOnNewWorldLoad(File worldDirectory) {
        if (this.worldDataSaver != null && (worldDirectory == null || !worldDirectory.equals(this.worldDataSaver.getWorldDirectory()))) {
            saveAndReleaseSaver();
        }
        this.worldDataSaver = new WorldDataSaver(worldDirectory);
        return this.worldDataSaver;
    }

    private synchronized void runWorldAndDataSave() {
        if (this.worldDataSaver != null) {
            OperationTimeLogger logger = new OperationTimeLogger(false).start();
            this.worldDataSaver.saveAllData(true);
            logger.finish("saving all mod data done in %f seconds");
            if (this.autoSaveMinecraftWorld) {
                logger.finish("minecraft world done in %f seconds");
                return;
            }
            return;
        }
        reportUnexpectedStateError("World data saver was not initialized during runWorldAndDataSave() call");
    }

    private synchronized void readDataOnLoad() {
        if (this.worldDataSaver != null) {
            OperationTimeLogger logger = new OperationTimeLogger(EngineConfig.isDeveloperMode()).start();
            this.worldDataSaver.readAllData(true);
            logger.finish("reading all mod data done in %f seconds");
            return;
        }
        reportUnexpectedStateError("World data saver was not initialized during readDataOnLoad() call");
    }

    private synchronized void saveAndReleaseSaver() {
        if (this.worldDataSaver != null) {
            OperationTimeLogger logger = new OperationTimeLogger(EngineConfig.isDeveloperMode()).start();
            this.worldDataSaver.saveAllData(true);
            logger.finish("saving all mod data done in %f seconds").start();
            this.worldDataSaver = null;
            return;
        }
        reportUnexpectedStateError("World data saver was not initialized during saveAndReleaseSaver() call");
    }

    private void reportUnexpectedStateError(String message) {
        Logger.error("UNEXPECTED WORLD SAVER STATE", message);
    }
}
