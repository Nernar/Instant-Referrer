package com.zhekasmirnov.innercore.api.log;

import java.util.ArrayList;

public class LogFilter {
    private static boolean isContainingErrorTags = false;
    ArrayList<LogMessage> logMessages = new ArrayList<>();

    public static boolean isContainingErrorTags() {
        return isContainingErrorTags;
    }

    public void log(LogType type, String prefix, String line) {
        if (type == LogType.ERROR || prefix.equals("ERROR")) {
            isContainingErrorTags = true;
        }
        this.logMessages.add(new LogMessage(type, prefix, line));
    }

    private LogMessage getMessageByIndex(int index) {
        if (index < 0 || index >= this.logMessages.size()) {
            return null;
        }
        return this.logMessages.get(index);
    }

    public String buildFilteredLog(boolean format) {
        String build = "";
        for (int i = 0; i < this.logMessages.size(); i++) {
            LogMessage msg = getMessageByIndex(i);
            StringBuilder sb = new StringBuilder();
            sb.append(build);
            sb.append(msg.format(format));
            sb.append(format ? "<br>" : "\n");
            build = sb.toString();
        }
        return build;
    }

    public static class LogMessage {
        public final String message;
        public final LogPrefix prefix;
        public final String strPrefix;
        public final LogType type;

        public LogMessage(LogType type, String prefix, String message) {
            this.type = type;
            this.strPrefix = prefix;
            this.prefix = LogPrefix.fromString(prefix);
            this.message = message;
        }

        public String toString() {
            return "[" + this.strPrefix + "] " + this.message;
        }

        public String toHtml() {
            return "<font color='" + this.prefix.toFontColor() + "'><b>[" + this.strPrefix + "]</b> " + this.message + "</font>";
        }

        public String format(boolean format) {
            return format ? toHtml() : toString();
        }
    }
}
