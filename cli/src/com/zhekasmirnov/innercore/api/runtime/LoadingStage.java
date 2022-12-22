package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import java.util.HashMap;

public class LoadingStage {
    public static final int STAGE_COMPLETE = 7;
    public static final int STAGE_COUNT = 8;
    public static final int STAGE_FINAL_LOADING = 6;
    public static final int STAGE_IDLE = 0;
    public static final int STAGE_MCPE_INITIALIZING = 5;
    public static final int STAGE_MCPE_STARTING = 4;
    public static final int STAGE_MODS_PRELOAD = 3;
    public static final int STAGE_RESOURCES_LOADING = 2;
    public static final int STAGE_START = 1;
    private static int stage = 0;
    private static HashMap<Integer, Long> timeMap = new HashMap<>();
    private static final long startupTime = System.currentTimeMillis();

    public static int getStage() {
        return stage;
    }

    public static void setStage(int stage2) {
        stage = stage2;
        timeMap.put(Integer.valueOf(stage2), Long.valueOf(System.currentTimeMillis()));
        ICLog.i("PROFILING", "switched into new loading stage: stage=" + stageToString(stage2));
    }

    public static boolean isPassed(int stage2) {
        return stage > stage2;
    }

    public static boolean isInOrPassed(int stage2) {
        return stage >= stage2;
    }

    public static String stageToString(int stage2) {
        switch (stage2) {
            case 0:
                return "STAGE_IDLE";
            case 1:
                return "STAGE_START";
            case 2:
                return "STAGE_RESOURCES_LOADING";
            case 3:
                return "STAGE_MODS_PRELOAD";
            case 4:
                return "STAGE_MCPE_STARTING";
            case 5:
                return "STAGE_MCPE_INITIALIZING";
            case 6:
                return "STAGE_FINAL_LOADING";
            case 7:
                return "STAGE_COMPLETE";
            default:
                return "STAGE_UNKNOWN";
        }
    }

    public static long getStageBeginTime(int stage2) {
        if (timeMap.containsKey(Integer.valueOf(stage2))) {
            return timeMap.get(Integer.valueOf(stage2)).longValue();
        }
        return -1L;
    }

    public static void outputTimeMap() {
        ICLog.d("PROFILING", "showing startup time map");
        for (int stage2 = 0; stage2 < 8; stage2++) {
            long time = getStageBeginTime(stage2);
            if (time != -1) {
                Logger.debug("PROFILING", "stage " + stageToString(stage2) + " started at " + ((time - startupTime) * 0.001d) + "s");
            } else {
                Logger.debug("PROFILING", "stage " + stageToString(stage2) + " was ignored");
            }
        }
    }
}
