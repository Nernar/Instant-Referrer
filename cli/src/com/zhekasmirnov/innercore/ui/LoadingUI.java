package com.zhekasmirnov.innercore.ui;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import java.lang.ref.WeakReference;

public class LoadingUI {
    @SuppressWarnings("unused")
    private static Object backgroundView;
    @SuppressWarnings("unused")
    private static Object backgroundViewDrawable;
    private static WeakReference<Object> context;
    @SuppressWarnings("unused")
    private static Object customUiThreadHandler;
    @SuppressWarnings("unused")
    private static Object customUiThreadLooper;
    @SuppressWarnings("unused")
    private static Object handler;
    private static Object windowManager;
    @SuppressWarnings("unused")
    private static Object windowParams;
    private static boolean isShowed = false;
    private static boolean isOpened = false;

    public static void initializeFor(Object ctx) {
        context = new WeakReference<>(ctx);
        initViews();
    }

    public static void initViews() {
    }

    public static boolean isShowed() {
        return isShowed;
    }

    public static void show() {
        if (context != null && windowManager != null && !isShowed) {
            try {
                Logger.debug("INNERCORE-LOADING-UI", "showing...");
                isShowed = true;
            } catch (Exception e) {
                isShowed = false;
                Logger.error("INNERCORE-LOADING-UI", "failed to display loading overlay, try to allow system overlays in settings.");
            }
        }
    }

    public static void hide() {
        if (context != null && windowManager != null && isShowed) {
            Logger.debug("INNERCORE-LOADING-UI", "hiding...");
            isShowed = false;
        }
    }

    private static void runCustomUiThread() {
        stopCustomUiThread();
    }

    public static void postOnCustomUiThread(Runnable r) {
        if (r != null) {
            r.run();
        }
    }

    public static void stopCustomUiThread() {
        customUiThreadHandler = null;
        customUiThreadLooper = null;
    }

    public static void open() {
        if (!isOpened) {
            isOpened = true;
            runCustomUiThread();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LoadingUI.postOnCustomUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoadingUI.show();
                            Logger.debug("LOADING-UI", "opened");
                        }
                    });
                    while (LoadingUI.isOpened) {
                        try {
                            Thread.sleep(75L);
                        } catch (InterruptedException e) {
                        }
                    }
                    LoadingUI.postOnCustomUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoadingUI.hide();
                            LoadingUI.stopCustomUiThread();
                        }
                    });
                }
            }).start();
        }
    }

    public static void close() {
        if (isOpened) {
            isOpened = false;
        }
    }

    public static void setTextAndProgressBar(String text, float progressBar) {
        setText(text);
        setProgress(progressBar);
    }

    public static void setText(String text) {
        Logger.debug("INNERCORE", "updated loading ui text: " + text);
    }

    public static void setProgress(float progress) {
        Logger.debug("INNERCORE", "updated loading ui progress: " + progress);
    }

    public static void setTip(String text) {
        Logger.debug("INNERCORE", "updated loading ui tip: " + text);
    }
}
