package com.zhekasmirnov.horizon.modloader.library;

import java.io.File;
import java.util.List;

public class LibraryMakeFile {
    @SuppressWarnings("unused")
    private File file;
    private String cppFlags = null;
    private List<String> files = null;

    public LibraryMakeFile(File file) {
        this.file = file;
    }

    public String getCppFlags() {
        return this.cppFlags;
    }

    public List<String> getFiles() {
        return this.files;
    }
}
