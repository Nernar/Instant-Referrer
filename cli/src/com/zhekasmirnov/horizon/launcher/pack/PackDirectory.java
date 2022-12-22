package com.zhekasmirnov.horizon.launcher.pack;

import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

public class PackDirectory {
    public final File directory;
    private IPackLocation location = null;
    private PackManifest localManifest = null;

    public PackDirectory(File file) {
        this.directory = file;
    }

    public void fetchFromRepo(PackRepository packRepository) {
        IPackLocation locationForUUID;
        String uuid = getUUID();
        if (uuid == null || (locationForUUID = packRepository.getLocationForUUID(uuid)) == null) {
            return;
        }
        setLocation(locationForUUID);
    }

    public IPackLocation getLocation() {
        return this.location;
    }

    public void setLocation(IPackLocation iPackLocation) {
        this.location = iPackLocation;
    }

    public String getExternalUUID() {
        IPackLocation iPackLocation = this.location;
        if (iPackLocation != null) {
            return iPackLocation.getUUID();
        }
        return null;
    }

    public String getLocalUUID() {
        return getInstallationInfo().getValue("uuid");
    }

    public String getUUID() {
        String externalUUID = getExternalUUID();
        return externalUUID != null ? externalUUID : getLocalUUID();
    }

    public String getInternalID() {
        PackInstallationInfo installationInfo = getInstallationInfo();
        String value = installationInfo.getValue("internalId");
        if (value == null) {
            String uuid = UUID.randomUUID().toString();
            installationInfo.setValue("internalId", uuid);
            return uuid;
        }
        return value;
    }

    public void generateNewInternalID() {
        getInstallationInfo().setValue("internalId", UUID.randomUUID().toString());
    }

    public void updateLocalUUID() {
        String externalUUID = getExternalUUID();
        if (externalUUID != null) {
            getInstallationInfo().setValue("uuid", externalUUID);
        }
    }

    public PackInstallationInfo getInstallationInfo() {
        return new PackInstallationInfo(new File(this.directory, ".installation_info"));
    }

    public int getInstallationPackageSize() {
        IPackLocation iPackLocation = this.location;
        if (iPackLocation != null) {
            return iPackLocation.getInstallationPackageSize();
        }
        return -1;
    }

    public InputStream getInstallationPackageStream() {
        IPackLocation iPackLocation = this.location;
        if (iPackLocation != null) {
            return iPackLocation.getInstallationPackageStream();
        }
        return null;
    }

    public InputStream getVisualDataStream() {
        IPackLocation iPackLocation = this.location;
        if (iPackLocation != null) {
            return iPackLocation.getVisualDataStream();
        }
        return null;
    }

    public PackManifest getExternalManifest() {
        IPackLocation iPackLocation = this.location;
        if (iPackLocation != null) {
            return iPackLocation.getManifest();
        }
        return null;
    }

    public PackManifest getLocalManifest() {
        if (this.localManifest == null) {
            File file = new File(this.directory, "manifest.json");
            if (file.exists()) {
                try {
                    this.localManifest = new PackManifest(new JSONObject(FileUtils.readFileText(file)));
                } catch (IOException e) {
                    throw new RuntimeException("failed to read manifest: failed to download or string conversion failed", e);
                } catch (NullPointerException e2) {
                } catch (JSONException e22) {
                    throw new RuntimeException("failed to read manifest: failed to read json", e22);
                }
            }
        }
        return this.localManifest;
    }

    public void reloadLocalManifest() {
        this.localManifest = null;
        getLocalManifest();
    }

    public PackManifest getManifest() {
        PackManifest localManifest = getLocalManifest();
        return localManifest != null ? localManifest : getExternalManifest();
    }

    public int getNewestVersionCode() {
        IPackLocation iPackLocation = this.location;
        if (iPackLocation != null) {
            return iPackLocation.getNewestVersionCode();
        }
        return -1;
    }

    public boolean isUpdateAvailable() {
        PackManifest localManifest;
        int newestVersionCode;
        return (this.location == null || (localManifest = getLocalManifest()) == null || (newestVersionCode = getNewestVersionCode()) == -1 || newestVersionCode <= localManifest.packVersionCode) ? false : true;
    }

    public void nominateForInstallation() {
        this.directory.mkdirs();
        updateLocalUUID();
        updateTimestamp();
        PackManifest externalManifest = getExternalManifest();
        if (externalManifest != null) {
            try {
                FileUtils.writeJSON(new File(this.directory, "manifest.json"), externalManifest.getContent());
            } catch (IOException e) {
            }
        }
    }

    public void updateTimestamp() {
        PackInstallationInfo installationInfo = getInstallationInfo();
        installationInfo.setValue("timestamp", new StringBuilder().append(System.currentTimeMillis()).toString());
    }

    public long getTimestamp() {
        try {
            return Long.valueOf(getInstallationInfo().getValue("timestamp")).longValue();
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public void setCustomName(String str) {
        getInstallationInfo().setValue("customName", str);
    }

    public String getCustomName() {
        return getInstallationInfo().getValue("customName");
    }
}
