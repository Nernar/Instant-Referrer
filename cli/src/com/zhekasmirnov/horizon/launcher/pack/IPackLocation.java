package com.zhekasmirnov.horizon.launcher.pack;

import java.io.InputStream;

public interface IPackLocation {
    String getChangelog();

    int getInstallationPackageSize();

    InputStream getInstallationPackageStream();

    PackManifest getManifest();

    int getNewestVersionCode();

    String getUUID();

    InputStream getVisualDataStream();
}
