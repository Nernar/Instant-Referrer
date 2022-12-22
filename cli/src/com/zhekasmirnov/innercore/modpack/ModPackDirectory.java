package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.modpack.strategy.extract.DirectoryExtractStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.DirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.DirectoryUpdateStrategy;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModPackDirectory {
    private final DirectoryExtractStrategy extractStrategy;
    private final File location;
    private ModPack modPack;
    private final String pathPattern;
    private final Pattern pathPatternRegex;
    private final DirectoryRequestStrategy requestStrategy;
    private final DirectoryType type;
    private final DirectoryUpdateStrategy updateStrategy;

    public enum DirectoryType {
        MODS,
        MOD_ASSETS,
        ENGINE,
        CONFIG,
        CACHE,
        RESOURCE_PACKS,
        BEHAVIOR_PACKS,
        TEXTURE_PACKS,
        CUSTOM;

        public static DirectoryType[] valuesCustom() {
            DirectoryType[] valuesCustom = values();
            int length = valuesCustom.length;
            DirectoryType[] directoryTypeArr = new DirectoryType[length];
            System.arraycopy(valuesCustom, 0, directoryTypeArr, 0, length);
            return directoryTypeArr;
        }
    }

    public ModPackDirectory(DirectoryType type, File location, String pathPattern, DirectoryRequestStrategy requestStrategy, DirectoryUpdateStrategy updateStrategy, DirectoryExtractStrategy extractStrategy) {
        this.type = type;
        this.location = location;
        this.pathPattern = pathPattern;
        this.pathPatternRegex = Pattern.compile("[\\s/\\\\]*" + pathPattern + "[\\s/\\\\]*(.*)");
        this.requestStrategy = requestStrategy;
        requestStrategy.assignToDirectory(this);
        this.updateStrategy = updateStrategy;
        updateStrategy.assignToDirectory(this);
        this.extractStrategy = extractStrategy;
        extractStrategy.assignToDirectory(this);
    }

    public boolean assureDirectoryRoot() {
        if (this.location.isDirectory()) {
            return true;
        }
        if (this.location.isFile()) {
            this.location.delete();
        }
        return this.location.mkdirs();
    }

    public void assignToModPack(ModPack modPack) {
        if (this.modPack != null) {
            throw new IllegalStateException("directory " + this + " is already assigned to modpack");
        }
        this.modPack = modPack;
    }

    public DirectoryType getType() {
        return this.type;
    }

    public File getLocation() {
        return this.location;
    }

    public String getPathPattern() {
        return this.pathPattern;
    }

    public Pattern getPathPatternRegex() {
        return this.pathPatternRegex;
    }

    public String getLocalPathFromEntry(String entryName) {
        Matcher matcher = this.pathPatternRegex.matcher(entryName);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }

    public DirectoryRequestStrategy getRequestStrategy() {
        return this.requestStrategy;
    }

    public DirectoryUpdateStrategy getUpdateStrategy() {
        return this.updateStrategy;
    }

    public DirectoryExtractStrategy getExtractStrategy() {
        return this.extractStrategy;
    }
}
