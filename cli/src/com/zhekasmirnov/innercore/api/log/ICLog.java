package com.zhekasmirnov.innercore.api.log;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import org.mozilla.javascript.RhinoException;

public class ICLog {
    private static HashMap<Long, IEventHandler> eventHandlerForThread;
    private static LogFilter logFilter = new LogFilter();
    private static LogWriter logWriter = new LogWriter(new File(String.valueOf(FileTools.DIR_WORK) + "inner-core.log"));

    static {
        logWriter.clear();
        eventHandlerForThread = new HashMap<>();
    }

    public static LogFilter getLogFilter() {
        return logFilter;
    }

    public static LogWriter getLogWriter() {
        return logWriter;
    }

    private static long getThreadId() {
        return Thread.currentThread().getId();
    }

    public static void setupEventHandlerForCurrentThread(IEventHandler eventHandler) {
        eventHandlerForThread.put(Long.valueOf(getThreadId()), eventHandler);
    }

    public static IEventHandler getEventHandlerForCurrentThread() {
        return eventHandlerForThread.get(Long.valueOf(getThreadId()));
    }

    private static String removeFaultSymbolsFromString(String s) {
        return s.replaceAll("%", "%%");
    }

    private static void logMsg(LogType type, String prefix, String msg) {
        logFilter.log(type, prefix, msg);
        logWriter.logMsg(type, prefix, msg);
    }

    public static void l(String prefix, String message) {
        IEventHandler handler = getEventHandlerForCurrentThread();
        if (handler != null) {
            handler.onLogEvent(prefix, message);
        }
        logMsg(LogType.LOG, prefix, message);
        Logger.debug(prefix, removeFaultSymbolsFromString(message));
    }

    public static void d(String prefix, String message) {
        IEventHandler handler = getEventHandlerForCurrentThread();
        if (handler != null) {
            handler.onDebugEvent(prefix, message);
        }
        logMsg(LogType.DEBUG, prefix, message);
        Logger.debug(prefix, removeFaultSymbolsFromString(message));
    }

    public static void i(String prefix, String message) {
        IEventHandler handler = getEventHandlerForCurrentThread();
        if (handler != null) {
            handler.onImportantEvent(prefix, message);
        }
        logMsg(LogType.IMPORTANT, prefix, message);
        if (prefix.toUpperCase().equals("ERROR")) {
            Logger.error(prefix, removeFaultSymbolsFromString(message));
        } else {
            Logger.info(prefix, removeFaultSymbolsFromString(message));
        }
    }

    public static void e(String prefix, String message, Throwable err) {
        String str;
        String str2;
        String str3;
        IEventHandler handler = getEventHandlerForCurrentThread();
        if (handler != null) {
            handler.onErrorEvent(prefix, message, err);
        }
        LogType logType = LogType.ERROR;
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            str = "[" + prefix + "] ";
        } else {
            str = "";
        }
        sb.append(str);
        sb.append(message);
        if (err != null) {
            str2 = "\n" + getStackTrace(err);
        } else {
            str2 = "";
        }
        sb.append(str2);
        logMsg(logType, "ERROR", sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(message);
        if (err != null) {
            str3 = "\n" + getStackTrace(err);
        } else {
            str3 = "";
        }
        sb2.append(str3);
        Logger.error(prefix, removeFaultSymbolsFromString(sb2.toString()));
        if (err != null) {
            err.printStackTrace();
        }
    }

    public static String getStackTrace(Throwable err) {
        String jsStack = null;
        if (err instanceof RhinoException) {
            jsStack = ((RhinoException) err).getScriptStackTrace();
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        err.printStackTrace(pw);
        if (jsStack != null) {
            return "JS STACK TRACE:\n" + jsStack + "\n\nFULL STACK TRACE:\n" + sw.toString();
        }
        return sw.toString();
    }

    public static void flush() {
        logWriter.flush();
    }

    public static void showIfErrorsAreFound() {
        if (LogFilter.isContainingErrorTags()) {
            DialogHelper.reportStartupErrors("Some errors are occured during Inner Core startup and loading.");
        }
    }
}
