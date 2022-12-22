package org.mineprogramming.horizon.innercore.model;

import com.zhekasmirnov.innercore.modpack.DirectorySetRequestHandler;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModTracker {
    private static ModTracker currentTracker;
    private List<String> modLocations;
    private Map<Integer, String> modLocationsById;
    private final ModPack modPack;
    private Map<Integer, Integer> modVersions;
    private DirectorySetRequestHandler requestHandler;
    private boolean locationsDirty = true;
    private boolean versionsDirty = true;

    public static ModTracker getCurrent() {
        ModPack currentPack = ModPackContext.getInstance().getCurrentModPack();
        if (currentTracker == null || currentTracker.modPack != currentPack) {
            currentTracker = new ModTracker(currentPack);
        }
        return currentTracker;
    }

    public static ModTracker forPack(ModPack modPack) {
        if (currentTracker != null && currentTracker.modPack == modPack) {
            return currentTracker;
        }
        return new ModTracker(modPack);
    }

    private ModTracker(ModPack modPack) {
        this.modPack = modPack;
    }

    public List<String> getModLocations() {
        rebuildLocationsListIfRequired();
        return this.modLocations;
    }

    public int getModsCount() {
        rebuildLocationsListIfRequired();
        return this.modLocations.size();
    }

    public boolean isInstalled(int id) {
        rebuildVersionsListIfRequired();
        return this.modVersions.containsKey(Integer.valueOf(id));
    }

    public String getLocation(int id) {
        rebuildVersionsListIfRequired();
        return this.modLocationsById.get(Integer.valueOf(id));
    }

    public Map<Integer, Integer> getVersions() {
        rebuildVersionsListIfRequired();
        return this.modVersions;
    }

    public void onInstalled(File root, String location, int icmodsId, int icmodsVersion) {
        rebuildVersionsListIfRequired();
        this.modLocations.add(location);
        this.modVersions.put(Integer.valueOf(icmodsId), Integer.valueOf(icmodsVersion));
        this.modLocationsById.put(Integer.valueOf(icmodsId), location);
        ModPreferences modPreferences = new ModPreferences(root);
        modPreferences.setIcmodsData(icmodsId, icmodsVersion);
    }

    public void onDeleted(String location, int icmodsId) {
        rebuildVersionsListIfRequired();
        this.modLocations.remove(location);
        this.modVersions.remove(Integer.valueOf(icmodsId));
        this.modLocationsById.remove(Integer.valueOf(icmodsId));
    }

    private void rebuildLocationsList() {
        this.requestHandler = this.modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);
        this.modLocations = this.requestHandler.getAllLocations();
        this.locationsDirty = false;
    }

    public void rebuildLocationsListIfRequired() {
        if (this.modLocations == null || this.locationsDirty) {
            rebuildLocationsList();
            this.versionsDirty = true;
        }
    }

    private void rebuildVersionsList() {
        rebuildLocationsListIfRequired();
        this.modVersions = new HashMap<>();
        this.modLocationsById = new HashMap<>();
        for (String location : this.modLocations) {
            File root = this.requestHandler.get(location);
            ModPreferences preferences = new ModPreferences(root);
            int id = preferences.getIcmodsId();
            if (id != 0) {
                this.modVersions.put(Integer.valueOf(id), Integer.valueOf(preferences.getIcmodsVersion()));
                this.modLocationsById.put(Integer.valueOf(id), location);
            }
        }
        this.versionsDirty = false;
    }

    public void rebuildVersionsListIfRequired() {
        if (this.modVersions == null || this.versionsDirty) {
            rebuildVersionsList();
        }
    }

    public void invalidate() {
        this.locationsDirty = true;
        this.versionsDirty = true;
    }
}
