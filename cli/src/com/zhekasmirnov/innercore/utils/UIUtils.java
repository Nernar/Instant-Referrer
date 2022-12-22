package com.zhekasmirnov.innercore.utils;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.mcpe161.EnvironmentSetup;

public class UIUtils {
    public static int screenHeight;
    public static int screenWidth;
    public static int xOffset = 0;
    public static int yOffset = 0;

    public static void getOffsets(int[] offsets) {
        offsets[0] = xOffset;
        offsets[1] = yOffset;
    }

    @SuppressWarnings("unused")
    private static void refreshScreenParams(Object ctx) {
    }

    public static void initialize(Object ctx) {
    }

    public static Object getContext() {
        return EnvironmentSetup.getCurrentActivity();
    }

    public static Object getDecorView() {
        return null;
    }

    public static void runOnUiThreadUnsafe(Runnable action) {
        new Thread(action).start();
    }

    public static void runOnUiThread(Runnable action) {
        runOnUiThreadUnsafe(new Runnable() {
            public void run() {
                try {
                    action.run();
                } catch (Exception e) {
                    UIUtils.processError(e);
                }
            }
        });
    }

    public static void processError(Exception e) {
        ICLog.e("INNERCORE-UI", "exception occured in UI engine:", e);
    }

    public static void log(String msg) {
        ICLog.d("INNERCORE-UI", msg);
    }

    public static Object getActivityOnTop() {
        return null;
    }

    public static boolean isInnerCoreActivityOpened() {
        return false;
    }
}
