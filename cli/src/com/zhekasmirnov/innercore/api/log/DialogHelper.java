package com.zhekasmirnov.innercore.api.log;

import com.zhekasmirnov.innercore.utils.UIUtils;

public class DialogHelper {
    private static int totalDialogCount = 0;
    private static ICustomErrorCallback customFatalErrorCallback = null;
    private static ICustomErrorCallback customNonFatalErrorCallback = null;
    private static ICustomErrorCallback customStartupErrorCallback = null;

    public interface ICustomErrorCallback {
        boolean show(String str, Throwable th, String str2, String str3);
    }

    static int access$008() {
        int i = totalDialogCount;
        totalDialogCount = i + 1;
        return i;
    }

    public static int access$010() {
        int i = totalDialogCount;
        totalDialogCount = i - 1;
        return i;
    }

    public static String getFormattedLog() {
        return ICLog.getLogFilter().buildFilteredLog(true);
    }

    public static String getFormattedStackTrace(Throwable error) {
        return "<font color='#FF0000'>" + ICLog.getStackTrace(error) + "</font>";
    }

    static class AnonymousClass1 implements Runnable {
        final Runnable val$dialogOverflowCallback;
        final int val$maxCount;
        final String val$text;
        final String val$title;

        AnonymousClass1(int i, String str, String str2, Runnable runnable) {
            this.val$maxCount = i;
            this.val$title = str;
            this.val$text = str2;
            this.val$dialogOverflowCallback = runnable;
        }

        @Override
        public void run() {
            if (this.val$maxCount < 0 || this.val$maxCount > DialogHelper.totalDialogCount) {
                DialogHelper.access$008();
            } else if (this.val$dialogOverflowCallback != null) {
                this.val$dialogOverflowCallback.run();
            }
        }
    }

    public static void openFormattedDialog(String text, String title, int maxCount, Runnable dialogOverflowCallback) {
        UIUtils.runOnUiThread(new AnonymousClass1(maxCount, title, text, dialogOverflowCallback));
    }

    public static void openFormattedDialog(String text, String title) {
        openFormattedDialog(text, title, -1, null);
    }

    public static int getTotalDialogCount() {
        return totalDialogCount;
    }

    public static void setCustomFatalErrorCallback(ICustomErrorCallback customFatalErrorCallback2) {
        customFatalErrorCallback = customFatalErrorCallback2;
    }

    public static void reportFatalError(String message, Throwable error) {
        String stacktrace = getFormattedStackTrace(error);
        String log = getFormattedLog();
        if (customFatalErrorCallback != null && customFatalErrorCallback.show(message, error, log, stacktrace)) {
            return;
        }
        String text = "<font color='#FFFFFF'>" + message + "\n\n\nSTACKTRACE:\n</font>" + stacktrace + "<font color='#FFFFFF'>\n\n\nLOG:\n</font>" + log;
        openFormattedDialog(text, "FATAL ERROR");
    }

    public static void setCustomNonFatalErrorCallback(ICustomErrorCallback customNonFatalErrorCallback2) {
        customNonFatalErrorCallback = customNonFatalErrorCallback2;
    }

    public static void reportNonFatalError(String message, Throwable error) {
        String stacktrace = getFormattedStackTrace(error);
        if (customNonFatalErrorCallback != null && customNonFatalErrorCallback.show(message, error, getFormattedLog(), stacktrace)) {
            return;
        }
        String text = "<font color='#FFFFFF'>" + message + "\n\n\nSTACKTRACE:\n</font>" + stacktrace;
        openFormattedDialog(text, "NON-FATAL ERROR");
    }

    public static void setCustomStartupErrorCallback(ICustomErrorCallback customStartupErrorCallback2) {
        customStartupErrorCallback = customStartupErrorCallback2;
    }

    public static void reportStartupErrors(String message) {
        String log = getFormattedLog();
        if (customStartupErrorCallback != null && customStartupErrorCallback.show(message, null, log, null)) {
            return;
        }
        String text = "<font color='#FFFFFF'>" + message + "\n\n\nLOG:\n</font>" + log;
        openFormattedDialog(text, "NON-FATAL ERROR");
    }
}
