package com.zhekasmirnov.innercore.modpack.installation;

import com.zhekasmirnov.innercore.modpack.ModPackManifest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class ModpackInstallationSource {

    public interface Entry {
        InputStream getInputStream() throws IOException;

        String getName();
    }

    public abstract Enumeration<Entry> entries();

    public abstract int getEntryCount();

    public abstract String getManifestContent() throws IOException;

    public ModPackManifest getTempManifest() throws IOException, JSONException {
        ModPackManifest manifest = new ModPackManifest();
        manifest.loadJson(new JSONObject(getManifestContent()));
        return manifest;
    }
}
