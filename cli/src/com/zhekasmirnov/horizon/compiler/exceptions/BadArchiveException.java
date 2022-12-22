package com.zhekasmirnov.horizon.compiler.exceptions;

import java.io.IOException;

public class BadArchiveException extends IOException {
    private final String fileName;

    public BadArchiveException(String str) {
        this.fileName = str;
    }

    public String getFileName() {
        return this.fileName;
    }
}
