package com.zhekasmirnov.innercore.modpack.installation;

import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileInstallationSource extends ModpackInstallationSource {
    protected ZipFile file;
    protected String manifestContent;

    public ZipFileInstallationSource(ZipFile file) {
        setFile(file);
    }

    public ZipFileInstallationSource() {
    }

    public void setFile(ZipFile file) {
        this.file = file;
    }

    public ZipFile getFile() {
        return this.file;
    }

    @Override
    public String getManifestContent() throws IOException {
        if (this.manifestContent == null) {
            ZipEntry entry = this.file.getEntry("modpack.json");
            if (entry == null) {
                throw new IOException("modpack zip file does not contain modpack.json");
            }
            this.manifestContent = FileUtils.convertStreamToString(this.file.getInputStream(entry));
        }
        return this.manifestContent;
    }

    @Override
    public int getEntryCount() {
        return this.file.size();
    }

    @Override
    public Enumeration<ModpackInstallationSource.Entry> entries() {
        final Enumeration<? extends ZipEntry> entries = this.file.entries();
        return new Enumeration<ModpackInstallationSource.Entry>() {
            @Override
            public boolean hasMoreElements() {
                return entries.hasMoreElements();
            }

            @Override
            public ModpackInstallationSource.Entry nextElement() {
                final ZipEntry entry = (ZipEntry) entries.nextElement();
                return new ModpackInstallationSource.Entry() {
                    @Override
                    public String getName() {
                        return entry.getName();
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return ZipFileInstallationSource.this.file.getInputStream(entry);
                    }
                };
            }
        };
    }
}
