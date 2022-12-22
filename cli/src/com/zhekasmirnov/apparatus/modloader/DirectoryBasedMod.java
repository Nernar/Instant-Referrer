package com.zhekasmirnov.apparatus.modloader;

import java.io.File;

public abstract class DirectoryBasedMod extends ApparatusMod {
    private final File directory;

    public DirectoryBasedMod(File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return this.directory;
    }
}
