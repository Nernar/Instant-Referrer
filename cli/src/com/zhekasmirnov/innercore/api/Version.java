package com.zhekasmirnov.innercore.api;

public class Version {
    public static final Version INNER_CORE_VERSION = new Version("2.0.0.0", 10, true);
    public int build;
    public boolean isBeta;
    public int level;
    public String name;

    public Version(String name, int level, boolean beta) {
        this.isBeta = false;
        this.level = 0;
        this.name = "";
        this.build = -1;
        this.name = name;
        this.level = level;
        this.isBeta = beta;
    }

    public Version(String name, int level) {
        this(name, level, false);
    }

    public Version(String name) {
        this(name, 0, false);
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("v");
        sb.append(this.name);
        sb.append(this.isBeta ? " beta" : "");
        if (this.build > 0) {
            str = " build " + this.build;
        } else {
            str = "";
        }
        sb.append(str);
        return sb.toString();
    }

    public void setBuild(int build) {
        this.build = build;
    }
}
