package com.zhekasmirnov.horizon.launcher.pack;

import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class ExternalPackLocation implements IPackLocation {
    private PackManifest externalManifest;
    private final String graphicsUrl;
    private final String manifestUrl;
    private final String packageUrl;
    private String uuid;
    private String changelogUrl = null;
    private int installationPackageSize = -1;
    private String changelog = null;

    public ExternalPackLocation(String str, String str2, String str3) {
        this.packageUrl = str;
        this.manifestUrl = str2;
        this.graphicsUrl = str3;
    }

    private static InputStream openUrlStream(String str) {
        if (str == null) {
            return null;
        }
        try {
            return new URL(str).openStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public InputStream getInstallationPackageStream() {
        return openUrlStream(this.packageUrl);
    }

    @Override
    public int getInstallationPackageSize() {
        if (this.installationPackageSize == -1) {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.packageUrl).openConnection();
                httpURLConnection.connect();
                this.installationPackageSize = httpURLConnection.getContentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.installationPackageSize;
    }

    @Override
    public InputStream getVisualDataStream() {
        return openUrlStream(this.graphicsUrl);
    }

    @Override
    public PackManifest getManifest() {
        if (this.externalManifest == null) {
            try {
                this.externalManifest = new PackManifest(new JSONObject(FileUtils.convertStreamToString(openUrlStream(this.manifestUrl))));
            } catch (IOException e) {
                throw new RuntimeException("failed to read manifest: failed to download or string conversion failed", e);
            } catch (NullPointerException e2) {
            } catch (JSONException e22) {
                throw new RuntimeException("failed to read manifest: failed to read json", e22);
            }
        }
        return this.externalManifest;
    }

    @Override
    public int getNewestVersionCode() {
        getManifest();
        PackManifest packManifest = this.externalManifest;
        if (packManifest != null) {
            return packManifest.packVersionCode;
        }
        return -1;
    }

    @Override
    public String getChangelog() {
        String str = this.changelogUrl;
        if (str == null) {
            return null;
        }
        if (this.changelog == null) {
            try {
                this.changelog = FileUtils.convertStreamToString(openUrlStream(str));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e2) {
                e2.printStackTrace();
            }
        }
        return this.changelog;
    }

    public void setUUID(String str) {
        this.uuid = str;
    }

    @Override
    public String getUUID() {
        return this.uuid;
    }

    public void setChangelogUrl(String str) {
        this.changelogUrl = str;
    }
}
