package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import java.io.File;
import org.mozilla.javascript.NativeArray;

public class ModPackJsAdapter {
    private final ModPack modPack;

    public ModPackJsAdapter(ModPack modPack) {
        this.modPack = modPack;
    }

    public ModPack getModPack() {
        return this.modPack;
    }

    public File getRootDirectory() {
        return this.modPack.getRootDirectory();
    }

    public String getRootDirectoryPath() {
        return this.modPack.getRootDirectory().getAbsolutePath();
    }

    public String getModsDirectoryPath() {
        ModPackDirectory directory = this.modPack.getDirectoryOfType(ModPackDirectory.DirectoryType.MODS);
        if (directory != null) {
            return directory.getLocation().getAbsolutePath();
        }
        ICLog.i("ERROR", "Currently selected modpack has no mod directory, falling back to default one");
        return new File(this.modPack.getRootDirectory(), "mods").getAbsolutePath();
    }

    public ModPackManifest getManifest() {
        return this.modPack.getManifest();
    }

    public ModPackPreferences getPreferences() {
        return this.modPack.getPreferences();
    }

    public DirectorySetRequestHandler getRequestHandler(String type) {
        return this.modPack.getRequestHandler(ModPackDirectory.DirectoryType.valueOf(type.trim().toUpperCase()));
    }

    public NativeArray getAllDirectories() {
        return ScriptableObjectHelper.createArray(this.modPack.getAllDirectories());
    }

    public NativeArray getDirectoriesOfType(String type) {
        return ScriptableObjectHelper.createArray(this.modPack.getDirectoriesOfType(ModPackDirectory.DirectoryType.valueOf(type.trim().toUpperCase())));
    }

    public ModPackDirectory getDirectoryOfType(String type) {
        return this.modPack.getDirectoryOfType(ModPackDirectory.DirectoryType.valueOf(type.trim().toUpperCase()));
    }
}
