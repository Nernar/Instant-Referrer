package com.zhekasmirnov.innercore.api.runtime.saver;

import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.mozilla.javascript.ScriptableObject;

public class GlobalSaves {
    public static final String SAVES_FILE_NAME = "moddata.json";
    public static final String SAVES_RESERVE_FILE_NAME = "moddata_backup.json";
    private static ScriptableObject globalScope = null;
    private static boolean isBeautified = true;
    private static ArrayList<LoggedSavesError> savesErrors = new ArrayList<>();
    private static HashMap<String, GlobalSavesScope> globalScopeMap = new HashMap<>();
    private static Thread currentThread = null;
    private static int currentThreadQueueSize = 0;
    private static boolean isReadComplete = false;
    private static boolean autoSaveEnabled = false;
    private static int autoSavePeriod = 30000;
    private static long lastAutoSave = 0;

    static int access$010() {
        int i = currentThreadQueueSize;
        currentThreadQueueSize = i - 1;
        return i;
    }

    private static void throwError(Throwable e) {
        RuntimeException r = new RuntimeException("error occurred in global saves");
        r.initCause(e);
        throw r;
    }

    private static void readScope(String fileName) {
        String dir = LevelInfo.getAbsoluteDir();
        if (dir == null) {
            return;
        }
        String jsonStr = null;
        globalScope = null;
        try {
            jsonStr = FileTools.readFileText(String.valueOf(dir) + fileName);
        } catch (IOException e) {
            throwError(e);
        }
        try {
            globalScope = (ScriptableObject) JsonHelper.parseJsonString(jsonStr);
        } catch (ClassCastException | JSONException e2) {
            throwError(e2);
        }
    }

    private static void writeScope() {
        String dir = LevelInfo.getAbsoluteDir();
        if (dir == null || globalScope == null) {
            return;
        }
        try {
            String text = JsonHelper.scriptableToJsonString(globalScope, isBeautified);
            FileTools.writeFileText(String.valueOf(dir) + "moddata.json", text);
            FileTools.writeFileText(String.valueOf(dir) + "moddata_backup.json", text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class LoggedSavesError {
        final Throwable error;
        final String message;

        LoggedSavesError(String message, Throwable error) {
            this.message = message;
            this.error = error;
        }
    }

    public static void logSavesError(String message, Throwable err) {
        savesErrors.add(new LoggedSavesError(message, err));
    }

    public static void clearSavesErrorLog() {
        savesErrors.clear();
    }

    public static ArrayList<LoggedSavesError> getSavesErrorLog() {
        return savesErrors;
    }

    public static void showSavesErrorsDialogIfRequired(boolean isReading) {
        if (!savesErrors.isEmpty()) {
            StringBuilder log = new StringBuilder();
            Iterator<LoggedSavesError> it = savesErrors.iterator();
            while (it.hasNext()) {
                LoggedSavesError err = it.next();
                log.append(err.message);
                log.append("\n");
                log.append(DialogHelper.getFormattedStackTrace(err.error));
                log.append("\n\n");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Errors occurred while ");
            sb.append(isReading ? "reading" : "saving");
            sb.append(" data:\n\n");
            sb.append((Object) log);
            String sb2 = sb.toString();
            StringBuilder sb3 = new StringBuilder();
            sb3.append("SOME ");
            sb3.append(isReading ? "READING" : "SAVING");
            sb3.append(" ERRORS OCCURRED");
            DialogHelper.openFormattedDialog(sb2, sb3.toString());
        }
    }

    public static void registerScope(String name, GlobalSavesScope scope) {
        while (globalScopeMap.containsKey(name)) {
            name = String.valueOf(name) + (name.hashCode() & 255);
        }
        scope.setName(name);
        globalScopeMap.put(name, scope);
    }

    public static synchronized void readSaves() {
        ICLog.d("SAVES", "reading saves...");
        updateAutoSaveTime();
        try {
            clearSavesErrorLog();
            readScope("moddata.json");
        } catch (RuntimeException e) {
            try {
                clearSavesErrorLog();
                readScope("moddata_backup.json");
            } catch (RuntimeException e2) {
                ICLog.e("SAVES", "failed to read saves", e2);
                globalScope = ScriptableObjectHelper.createEmpty();
            }
        }
        Object saverScope;
        for (String name : globalScopeMap.keySet()) {
            if (globalScope.has(name, globalScope)) {
                Object obj = globalScope.get(name);
                saverScope = obj;
            } else {
                Object createEmpty = ScriptableObjectHelper.createEmpty();
                saverScope = createEmpty;
            }
            try {
                GlobalSavesScope globalSavesScope = globalScopeMap.get(name);
                globalSavesScope.read(saverScope);
            } catch (Exception err) {
                logSavesError("error in reading scope " + name, err);
            }
        }
        showSavesErrorsDialogIfRequired(true);
        Callback.invokeAPICallback("ReadSaves", globalScope);
    }

    public static synchronized void writeSaves() {
        ICLog.d("SAVES", "writing saves...");
        updateAutoSaveTime();
        globalScope = ScriptableObjectHelper.createEmpty();
        clearSavesErrorLog();
        Iterator<String> it = globalScopeMap.keySet().iterator();
        while (it.hasNext()) {
            String str = it.next();
            try {
                globalScope.put(str, globalScope, globalScopeMap.get(str).save());
            } catch (Exception err) {
                logSavesError("error in writing saves scope " + str, err);
            }
        }
        Callback.invokeAPICallback("WriteSaves", globalScope);
        try {
            writeScope();
        } catch (RuntimeException e) {
            ICLog.e("SAVES", "failed to write saves", e);
        }
        showSavesErrorsDialogIfRequired(false);
    }

    public static boolean isReadComplete() {
        return isReadComplete;
    }

    public static void setIsReadComplete(boolean isReadComplete2) {
        isReadComplete = isReadComplete2;
    }

    public static void writeSavesInThread(final boolean saveMCPEWorld) {
        currentThreadQueueSize++;
        if (currentThread == null) {
            currentThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (GlobalSaves.currentThreadQueueSize > 0) {
                        ICLog.d("SAVES", "started saving world data");
                        ICLog.flush();
                        MainThreadQueue mainThreadQueue = MainThreadQueue.serverThread;
                        final boolean z = saveMCPEWorld;
                        mainThreadQueue.enqueue(new Runnable() {
                            @Override
                            public void run() {
                                if (z) {
                                    ICLog.d("SAVES", "saving minecraft world...");
                                    ICLog.flush();
                                    long start = System.currentTimeMillis();
                                    long end = System.currentTimeMillis();
                                    ICLog.d("SAVES", "saving minecraft world in thread took " + (end - start) + " ms");
                                }
                            }
                        });
                        long start = System.currentTimeMillis();
                        GlobalSaves.writeSaves();
                        long end = System.currentTimeMillis();
                        ICLog.d("SAVES", "saving mod data in thread took " + (end - start) + " ms");
                        ICLog.flush();
                        GlobalSaves.access$010();
                    }
                    GlobalSaves.currentThread = null;
                }
            });
            currentThread.start();
        }
    }

    public static void sleepUntilThreadEnd() {
        long start = System.currentTimeMillis();
        while (currentThread != null) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
            }
        }
        long end = System.currentTimeMillis();
        ICLog.d("SAVES", "delaying main thread while saving data took " + (end - start) + " ms");
    }

    public static void setAutoSaveParams(boolean enabled, int period) {
        autoSaveEnabled = enabled;
        autoSavePeriod = Math.max(5000, period);
        ICLog.d("SAVES", "auto-save params set enabled=" + enabled + " period=" + period);
    }

    private static void updateAutoSaveTime() {
        lastAutoSave = System.currentTimeMillis();
    }

    public static void startAutoSaveIfNeeded() {
        if (autoSaveEnabled) {
            long time = System.currentTimeMillis();
            if (time - lastAutoSave > autoSavePeriod) {
                updateAutoSaveTime();
                writeSavesInThread(true);
            }
        }
    }
}
