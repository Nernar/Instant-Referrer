package com.zhekasmirnov.innercore.api.log;

import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;

public class LogWriter {
    private String buffer = "";
    private final File file;

    public LogWriter(File file) {
        this.file = file;
    }

    public void clear() {
        try {
            FileTools.writeFileText(this.file.getAbsolutePath(), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logMsg(LogType type, String prefix, String message) {
        this.buffer = String.valueOf(this.buffer) + "[" + prefix + "] " + message + "\n";
        if (this.buffer.length() > 2048) {
            flush();
        }
    }

    public void flush() {
        try {
            FileTools.addFileText(this.file.getAbsolutePath(), this.buffer);
            this.buffer = "";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
