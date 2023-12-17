package com.zhekasmirnov.horizon.launcher.pack;

import com.zhekasmirnov.horizon.launcher.ContextHolder;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PackStorage {
    public final ContextHolder contextHolder;
    public final File directory;
    private List<PackDirectory> packDirectories = new ArrayList<>();
    public List<PackHolder> packHolders = new ArrayList<>();

    public PackStorage(ContextHolder contextHolder, File file) {
        this.contextHolder = contextHolder;
        if (file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.isDirectory()) {
            throw new RuntimeException("Not directory passed to pack storage constructor: " + file);
        }
        this.directory = file;
        updateLocalDirectories();
    }

    private boolean addPackDirectory(PackDirectory packDirectory) {
        for (PackDirectory packDirectory2 : this.packDirectories) {
            if (packDirectory2.directory.equals(packDirectory.directory)) {
                return false;
            }
        }
        this.packDirectories.add(packDirectory);
        return true;
    }

    public void updateLocalDirectories() {
        File[] listFiles = this.directory.listFiles();
        if (listFiles == null) {
            return;
        }
        for (File file : listFiles) {
            if (file.isDirectory()) {
                addPackDirectory(new PackDirectory(file));
            }
        }
    }

    public PackDirectory makeNewPackDirectory(String str) {
        String str2;
        String replaceAll = ((str == null || str.length() == 0) ? "unnamed-pack" : str).replaceAll("[^a-zA-Z0-9_]", "_");
        int i = 0;
        while (true) {
            File file = this.directory;
            if (i > 0) {
                str2 = String.valueOf(replaceAll) + "_" + i;
            } else {
                str2 = replaceAll;
            }
            PackDirectory packDirectory = new PackDirectory(new File(file, str2));
            if (addPackDirectory(packDirectory)) {
                return packDirectory;
            }
            i++;
        }
    }

    public PackDirectory addPackLocation(IPackLocation iPackLocation) {
        PackManifest manifest = iPackLocation.getManifest();
        if (manifest == null) {
            return null;
        }
        PackDirectory makeNewPackDirectory = makeNewPackDirectory(manifest.pack);
        makeNewPackDirectory.setLocation(iPackLocation);
        return makeNewPackDirectory;
    }

    public PackHolder loadNewPackHolderFromLocation(IPackLocation iPackLocation) {
        PackDirectory addPackLocation = addPackLocation(iPackLocation);
        if (addPackLocation != null) {
            addPackLocation.nominateForInstallation();
            return loadPackHolderForDirectory(addPackLocation);
        }
        return null;
    }

    public PackHolder loadPackHolderFromDirectory(PackDirectory packDirectory) {
        if (!this.packDirectories.contains(packDirectory)) {
            throw new IllegalArgumentException("you must pass only directories from this pack storage");
        }
        return loadPackHolderForDirectory(packDirectory);
    }

    public ContextHolder getContextHolder() {
        return this.contextHolder;
    }

    public List<PackDirectory> getPackDirectories() {
        return this.packDirectories;
    }

    public synchronized void unloadAll() {
        for (PackHolder packHolder : this.packHolders) {
            packHolder.deselectAndUnload();
        }
        this.packHolders.clear();
    }

    private PackHolder loadPackHolderForDirectory(PackDirectory packDirectory) {
        PackHolder packHolder = new PackHolder(this, packDirectory);
        if (packHolder.getManifest() != null) {
            packHolder.initialize();
            this.packHolders.add(packHolder);
            return packHolder;
        }
        return null;
    }

    public synchronized List<PackHolder> reloadAll() {
        unloadAll();
        for (PackDirectory packDirectory : this.packDirectories) {
            loadPackHolderForDirectory(packDirectory);
        }
        return this.packHolders;
    }

    public synchronized void loadAll() {
        for (PackDirectory packDirectory : this.packDirectories) {
            boolean z = true;
            Iterator<PackHolder> it = this.packHolders.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (it.next().packDirectory == packDirectory) {
                        z = false;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (z) {
                loadPackHolderForDirectory(packDirectory);
            }
        }
    }

    public void fetchLocationsFromRepo(PackRepository packRepository) {
        for (PackDirectory packDirectory : this.packDirectories) {
            packDirectory.fetchFromRepo(packRepository);
        }
    }
}
