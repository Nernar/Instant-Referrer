package com.zhekasmirnov.innercore.modpack.installation;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class ExternalZipFileInstallationSource extends ZipFileInstallationSource implements Closeable {
    public ExternalZipFileInstallationSource(InputStream inputStream) throws IOException {
        File tmpFile = new File(FileTools.DIR_WORK, "temp/modpack_tmp");
        tmpFile.getParentFile().mkdirs();
        tmpFile.delete();
        FileUtils.unpackInputStream(inputStream, tmpFile);
        setFile(new ZipFile(tmpFile));
    }

    @Override
    public void close() throws IOException {
    }
}
