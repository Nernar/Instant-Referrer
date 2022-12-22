package com.zhekasmirnov.horizon.runtime.logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class EventLogger {
    private List<Message> messages = new ArrayList<>();
    private String currentSection = null;

    public interface Filter {
        boolean filter(Message message);
    }

    public enum MessageType {
        DEBUG,
        INFO,
        FAULT,
        EXCEPTION,
        STACKTRACE;

        public static MessageType[] valuesCustom() {
            MessageType[] valuesCustom = values();
            int length = valuesCustom.length;
            MessageType[] messageTypeArr = new MessageType[length];
            System.arraycopy(valuesCustom, 0, messageTypeArr, 0, length);
            return messageTypeArr;
        }
    }

    public class Message {
        public final String message;
        public final String section;
        public final String tag;
        public final MessageType type;

        private Message(MessageType messageType, String str, String str2) {
            this.type = messageType;
            this.tag = str;
            this.message = str2;
            this.section = EventLogger.this.currentSection;
        }

        Message(EventLogger eventLogger, MessageType messageType, String str, String str2, Message message) {
            this(messageType, str, str2);
        }
    }

    public synchronized List<Message> getMessages(Filter filter) {
        ArrayList<Message> arrayList = new ArrayList<>();
        for (Message message : this.messages) {
            if (filter.filter(message)) {
                arrayList.add(message);
            }
        }
        return arrayList;
    }

    public synchronized void addMessage(MessageType messageType, String str, String str2) {
        this.messages.add(new Message(this, messageType, str, str2, null));
    }

    public void section(String str) {
        this.currentSection = str;
    }

    public void debug(String str, String str2) {
        addMessage(MessageType.DEBUG, str, str2);
        Logger.debug(str, str2.replaceAll("%", "%%"));
    }

    public void info(String str, String str2) {
        addMessage(MessageType.INFO, str, str2);
        Logger.info(str, str2.replaceAll("%", "%%"));
    }

    private static String getStackTrace(Throwable th) {
        StringWriter stringWriter = new StringWriter();
        th.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    public void fault(String str, String str2, Throwable th) {
        Logger.error(str, str2.replaceAll("%", "%%"));
        addMessage(MessageType.FAULT, str, str2);
        if (th != null) {
            addMessage(MessageType.EXCEPTION, str, th.getMessage());
            String stackTrace = getStackTrace(th);
            Logger.error(str, stackTrace.replaceAll("%", "%%"));
            addMessage(MessageType.STACKTRACE, str, stackTrace);
        }
    }

    public void fault(String str, String str2) {
        fault(str, str2, null);
    }

    public OutputStream getStream(final MessageType messageType, final String str) {
        return new OutputStream() {
            StringBuffer buffer = new StringBuffer();

            @Override
            public void write(int i) throws IOException {
                this.buffer.append((char) i);
                if (i == 10) {
                    String stringBuffer = this.buffer.toString();
                    EventLogger.this.addMessage(messageType, str, stringBuffer);
                    Logger.info(messageType + "/BUILD", stringBuffer);
                    this.buffer = new StringBuffer();
                }
            }
        };
    }

    public void clear() {
        this.messages.clear();
    }
}
