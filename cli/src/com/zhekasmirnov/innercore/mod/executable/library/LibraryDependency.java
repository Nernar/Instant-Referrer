package com.zhekasmirnov.innercore.mod.executable.library;

import com.zhekasmirnov.innercore.mod.build.Mod;

public class LibraryDependency {
    public final String libName;
    public final int minVersion;
    private Mod parentMod;

    public LibraryDependency(String libName, int minVersion) {
        this.libName = libName;
        this.minVersion = minVersion;
    }

    public LibraryDependency(String formattedString) {
        String[] parts = formattedString.split(":");
        if (parts.length == 1) {
            this.libName = formattedString;
            this.minVersion = -1;
        } else if (parts.length > 2) {
            throw new IllegalArgumentException("invalid library dependency " + formattedString + ", it should be formatted as <name>:<versionCode>");
        } else {
            try {
                this.libName = parts[0];
                this.minVersion = Integer.valueOf(parts[1]).intValue();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid library dependency " + formattedString + ", it should be formatted as <name>:<versionCode>");
            }
        }
    }

    public void setParentMod(Mod parentMod) {
        this.parentMod = parentMod;
    }

    public Mod getParentMod() {
        return this.parentMod;
    }

    public boolean isMatchesLib(Library lib) {
        return this.libName.equals(lib.getLibName()) && lib.getVersionCode() >= this.minVersion;
    }

    public boolean hasTargetVersion() {
        return this.minVersion != -1;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(this.libName);
        if (hasTargetVersion()) {
            str = ":" + this.minVersion;
        } else {
            str = "";
        }
        sb.append(str);
        return sb.toString();
    }

    public int hashCode() {
        return toString().hashCode();
    }
}
