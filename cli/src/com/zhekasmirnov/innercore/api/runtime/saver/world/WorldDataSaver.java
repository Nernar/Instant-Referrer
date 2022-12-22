package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class WorldDataSaver {
    private static final String[] worldSavesFileNames = {"moddata.json", "moddata_backup.json"};
    private final List<LoggedSavesError> errorLog = new ArrayList<>();
    private final Map<String, Object> missingScopeData = new HashMap<>();
    private SaverState state = SaverState.IDLE;
    private final File worldDirectory;

    public enum SaverState {
        IDLE,
        READ,
        SAVE;

        public static SaverState[] valuesCustom() {
            SaverState[] valuesCustom = values();
            int length = valuesCustom.length;
            SaverState[] saverStateArr = new SaverState[length];
            System.arraycopy(valuesCustom, 0, saverStateArr, 0, length);
            return saverStateArr;
        }
    }

    private static class LoggedSavesError {
        final Throwable error;
        final String message;
        @SuppressWarnings("unused")
        final SaverState state;

        LoggedSavesError(String message, SaverState state, Throwable error) {
            this.message = message;
            this.state = state;
            this.error = error;
        }
    }

    public WorldDataSaver(File worldDirectory) {
        this.worldDirectory = worldDirectory;
    }

    public File getWorldDirectory() {
        return this.worldDirectory;
    }

    public SaverState getState() {
        return this.state;
    }

    private JSONObject readJsonWithLockCheck(File directory, String name, boolean lockCheck) {
        File file = new File(directory, name);
        if (file.exists()) {
            if (lockCheck && FileUtils.getFileFlag(directory, String.valueOf(name) + "-opened")) {
                return null;
            }
            try {
                return FileUtils.readJSON(file);
            } catch (IOException | JSONException e) {
                Logger.error("FAILED TO READ SAVES", "Failed to read this world saves");
                return null;
            }
        }
        return null;
    }

    public synchronized void readAllData(boolean showLogOnError) {
        if (this.worldDirectory == null) {
            return;
        }
        JSONObject json = null;
        for (String name : worldSavesFileNames) {
            json = readJsonWithLockCheck(this.worldDirectory, name, true);
            if (json != null) {
                break;
            }
        }
        if (json == null) {
            JSONObject readJsonWithLockCheck = readJsonWithLockCheck(this.worldDirectory, "moddata.json", false);
            json = readJsonWithLockCheck;
            if (readJsonWithLockCheck == null) {
                return;
            }
        }
        this.state = SaverState.READ;
        this.missingScopeData.clear();
        WorldDataScopeRegistry worldDataScopeRegistry = WorldDataScopeRegistry.getInstance();
        WorldDataScopeRegistry.SavesErrorHandler savesErrorHandler = new WorldDataScopeRegistry.SavesErrorHandler() {
            @Override
            public final void handle(String str, Throwable th) {
                WorldDataSaver.this.logError("While reading scope " + str, SaverState.READ, th);
            }
        };
        final Map<String, Object> map = this.missingScopeData;
        map.getClass();
        worldDataScopeRegistry.readAllScopes(json, savesErrorHandler, new WorldDataScopeRegistry.MissingScopeHandler() {
            @Override
            public final void handle(String str, Object obj) {
                map.put(str, obj);
            }
        });
        this.state = SaverState.IDLE;
        if (showLogOnError) {
            showAndClearErrorLog(SaverState.SAVE);
        }
    }

    public synchronized void saveAllData(boolean showLogOnError) {
        if (this.worldDirectory == null) {
            return;
        }
        if (!this.worldDirectory.isDirectory()) {
            this.worldDirectory.mkdirs();
        }
        this.state = SaverState.SAVE;
        JSONObject json = new JSONObject((Map<?, ?>) this.missingScopeData);
        WorldDataScopeRegistry.getInstance().saveAllScopes(json, new WorldDataScopeRegistry.SavesErrorHandler() {
            @Override
            public final void handle(String str, Throwable th) {
                WorldDataSaver.this.logError("While saving scope " + str, SaverState.SAVE, th);
            }
        });
        this.state = SaverState.IDLE;
        for (String name : worldSavesFileNames) {
            try {
                FileUtils.setFileFlag(this.worldDirectory, String.valueOf(name) + "-opened", true);
                FileUtils.writeJSON(new File(this.worldDirectory, name), json);
                FileUtils.setFileFlag(this.worldDirectory, String.valueOf(name) + "-opened", false);
            } catch (Exception e) {
                Logger.error("FAILED TO WRITE SAVES", "Failed to write this world saves");
            }
        }
        if (showLogOnError) {
            showAndClearErrorLog(SaverState.SAVE);
        }
    }

    public void logError(String message, SaverState state, Throwable err) {
        this.errorLog.add(new LoggedSavesError(message, state, err));
    }

    public void logError(String message, Throwable err) {
        this.errorLog.add(new LoggedSavesError(message, this.state, err));
    }

    public void clearErrorLog() {
        this.errorLog.clear();
    }

    public void showAndClearErrorLog(SaverState state) {
        if (!this.errorLog.isEmpty()) {
            StringBuilder log = new StringBuilder();
            for (LoggedSavesError err : this.errorLog) {
                log.append(err.message);
                log.append("\n");
                log.append(DialogHelper.getFormattedStackTrace(err.error));
                log.append("\n\n");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Errors occurred while ");
            sb.append(state == SaverState.READ ? "reading" : "saving");
            sb.append(" data:\n\n");
            sb.append((Object) log);
            String sb2 = sb.toString();
            StringBuilder sb3 = new StringBuilder();
            sb3.append("SOME ");
            sb3.append(state == SaverState.READ ? "READING" : "SAVING");
            sb3.append(" ERRORS OCCURRED");
            DialogHelper.openFormattedDialog(sb2, sb3.toString());
        }
        clearErrorLog();
    }

    public List<LoggedSavesError> getErrorLog() {
        return this.errorLog;
    }

    public static void logErrorStatic(String message, Throwable err) {
        WorldDataSaver saver = WorldDataSaverHandler.getInstance().getWorldDataSaver();
        if (saver != null) {
            saver.logError(message, err);
        }
    }
}
