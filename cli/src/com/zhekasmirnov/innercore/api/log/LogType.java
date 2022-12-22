package com.zhekasmirnov.innercore.api.log;

public enum LogType {
    DEBUG(0),
    LOG(1),
    IMPORTANT(2),
    ERROR(3);
    
    public int level;

    public static LogType[] valuesCustom() {
        LogType[] valuesCustom = values();
        int length = valuesCustom.length;
        LogType[] logTypeArr = new LogType[length];
        System.arraycopy(valuesCustom, 0, logTypeArr, 0, length);
        return logTypeArr;
    }

    LogType(int level) {
        this.level = level;
    }
}
