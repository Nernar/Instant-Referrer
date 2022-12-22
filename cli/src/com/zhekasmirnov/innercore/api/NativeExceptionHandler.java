package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.innercore.api.log.ICLog;
import java.util.ArrayList;

public class NativeExceptionHandler {

    public static class ICNativeException extends RuntimeException {
        public ICNativeException(String msg) {
            super(msg);
        }
    }

    public static ICNativeException buildException(String info, String backtrace) {
        ArrayList<StackTraceElement> stacktrace = new ArrayList<>();
        if (info != null) {
            stacktrace.add(new StackTraceElement("[signal info: " + info + "]", "", "", -2));
        }
        if (backtrace != null) {
            String[] lines = backtrace.split("\n");
            for (String line : lines) {
                String method = line.substring(line.indexOf(95));
                stacktrace.add(new StackTraceElement("", method, "", -2));
            }
        }
        ICNativeException exception = new ICNativeException("exception was caught in native code");
        exception.setStackTrace((StackTraceElement[]) stacktrace.toArray());
        return exception;
    }

    public static void handle(String info, String backtrace) {
        final ICNativeException exception = buildException(info, backtrace);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ICLog.e("ERROR", "native crash handled: ", exception);
                throw exception;
            }
        }).start();
    }
}
