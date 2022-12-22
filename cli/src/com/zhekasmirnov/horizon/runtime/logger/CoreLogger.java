package com.zhekasmirnov.horizon.runtime.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CoreLogger {
    private static File logFile;

    private static synchronized File getLogFile() {
        if (logFile == null) {
            try {
                logFile = LogFileHandler.getInstance().getNewLogFile("core-log.txt");
            } catch (Throwable th) {
            }
        }
        return logFile;
    }

    private static synchronized void writeToFile(String str) {
        System.out.print(str);
        File logFile2 = getLogFile();
        if (logFile2 == null) {
            return;
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(logFile2, true);
        } catch (IOException e) {
        } catch (Throwable th) {
        }
        try {
            fileWriter.write(str);
        } catch (IOException e2) {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Throwable th2) {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
            throw th2;
        }
        try {
            fileWriter.close();
        } catch (IOException e32) {
            e32.printStackTrace();
        }
    }

    public static void log(String str, Object obj) {
        writeToFile("[" + str + "] " + obj + "\n");
    }
}
