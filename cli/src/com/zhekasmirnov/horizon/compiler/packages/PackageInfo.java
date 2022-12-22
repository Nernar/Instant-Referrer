package com.zhekasmirnov.horizon.compiler.packages;

public class PackageInfo {
    private String arch;
    private String depends;
    private String description;
    private String fileName;
    private int filesize;
    private String name;
    private String replaces;
    private int size;
    private String version;

    public PackageInfo(String str, String str2, int i, int i2, String str3, String str4, String str5, String str6, String str7) {
        this.name = str;
        this.fileName = str2;
        this.size = i;
        this.filesize = i2;
        this.version = str3;
        this.description = str4;
        this.depends = str5;
        this.arch = str6;
        this.replaces = str7;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "PackageInfo{name='" + this.name + "', file='" + this.fileName + "', size=" + this.size + ", filesize=" + this.filesize + ", version='" + this.version + "', description='" + this.description + "', depends='" + this.depends + "', arch='" + this.arch + "', replaces='" + this.replaces + "'}";
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getSize() {
        return this.size;
    }

    public int getFileSize() {
        return this.filesize;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDepends() {
        return this.depends;
    }

    public String getArch() {
        return this.arch;
    }

    public String getReplaces() {
        return this.replaces;
    }
}
