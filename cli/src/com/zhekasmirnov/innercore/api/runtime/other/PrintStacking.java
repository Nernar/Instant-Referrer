package com.zhekasmirnov.innercore.api.runtime.other;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

public class PrintStacking {
    @SuppressWarnings("unused")
    private static final int LONG_DELAY = 1500;
    @SuppressWarnings("unused")
    private static final int MAX_LENGTH = 4096;
    @SuppressWarnings("unused")
    private static final int SHORT_DELAY = 100;
    @SuppressWarnings("unused")
    private static Object handler;
    private static final Object lock = new Object();
    private static boolean isShowPosted = false;
    private static long last = -1;
    private static String text = "";
    private static final Runnable showAndClearRunnable = new Runnable() {
        @Override
        public void run() {
            PrintStacking.showAndClear();
        }
    };

    public static void prepare() {
        handler = new Object();
    }

    public static void showAndClear() {
        if (text.length() > 0) {
            synchronized (lock) {
                String _text = text;
                text = "";
                String text0 = _text.length() > 4096 ? _text.substring(0, 4096) : _text;
                Logger.info("PRINT", text0);
            }
        }
        isShowPosted = false;
        last = System.currentTimeMillis();
    }

    private static void post(Runnable action, int delay) {
        action.run();
    }

    private static void postShowAndClear(int delay) {
        isShowPosted = true;
        post(showAndClearRunnable, delay);
    }

    public static void print(String message) {
        synchronized (lock) {
            if (text.length() > 0) {
                text = String.valueOf(text) + "\n";
            }
            text = String.valueOf(text) + message;
            if (!isShowPosted) {
                long time = System.currentTimeMillis();
                if (time - last > 1500) {
                    postShowAndClear(100);
                } else {
                    postShowAndClear((int) (1500 - (time - last)));
                }
            }
        }
    }
}
