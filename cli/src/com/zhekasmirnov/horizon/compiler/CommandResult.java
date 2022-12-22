package com.zhekasmirnov.horizon.compiler;

public class CommandResult {
    private String message;
    private int resultCode;
    private long time;

    public CommandResult(int i) {
        this.time = 0L;
        this.resultCode = i;
    }

    public CommandResult(int i, String str) {
        this.time = 0L;
        this.resultCode = i;
        this.message = str;
    }

    public CommandResult(CommandResult commandResult) {
        this(commandResult.getResultCode(), commandResult.getMessage());
    }

    public String toString() {
        return "CommandResult{message='" + this.message + "', resultCode=" + this.resultCode + ", time=" + this.time + '}';
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long j) {
        this.time = j;
    }

    public int getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(int i) {
        this.resultCode = i;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String str) {
        this.message = str;
    }
}
