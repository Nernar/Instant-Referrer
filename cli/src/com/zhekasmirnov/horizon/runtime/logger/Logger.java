package com.zhekasmirnov.horizon.runtime.logger;

import java.io.File;

public class Logger {
    public static void debug(String str, String str2) {
        CoreLogger.log(String.valueOf(str) + "/D", str2);
    }

    public static void error(String str, String str2) {
        CoreLogger.log(String.valueOf(str) + "/E", str2);
    }

    public static void info(String str, String str2) {
        CoreLogger.log(String.valueOf(str) + "/I", str2);
    }

    public static void message(String str, String str2) {
        CoreLogger.log(str, str2);
    }

    public static void setCrashFile(String str) {
    }

    public static void setOutputFile(String str) {
    }

    public static void setOutputFile(File file) {
        setOutputFile(file.getAbsolutePath());
    }

    public static void setCrashFile(File file) {
        setCrashFile(file.getAbsolutePath());
    }

    public static void debug(String str) {
        debug("DEBUG", str);
    }

    public static void message(String str) {
        message("MESSAGE", str);
    }

    public static void info(String str) {
        info("INFO", str);
    }

    public static void warning(String str) {
        info("WARNING", str);
    }

    public static void error(String str) {
        info("ERROR", str);
    }
}
