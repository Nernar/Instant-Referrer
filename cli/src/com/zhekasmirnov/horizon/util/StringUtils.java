package com.zhekasmirnov.horizon.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StringUtils {
    public static Integer toIntegerOrNull(String str) {
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double toDoubleOrNull(String str) {
        try {
            return Double.valueOf(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getStackTrace(Throwable th) {
        StringWriter stringWriter = new StringWriter();
        th.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
