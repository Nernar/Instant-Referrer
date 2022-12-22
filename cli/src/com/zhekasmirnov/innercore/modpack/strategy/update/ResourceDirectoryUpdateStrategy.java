package com.zhekasmirnov.innercore.modpack.strategy.update;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceDirectoryUpdateStrategy extends DirectoryUpdateStrategy {
    @Override
    public void beginUpdate() throws IOException {
        FileUtils.clearFileTree(getAssignedDirectory().getLocation(), true);
        getAssignedDirectory().getLocation().mkdirs();
    }

    @Override
    public void updateFile(String path, InputStream stream) throws IOException {
        File file = new File(getAssignedDirectory().getLocation(), path);
        file.getParentFile().mkdirs();
        OutputStream outputStream = new FileOutputStream(file);
        try {
            FileTools.inStreamToOutStream(stream, outputStream);
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (outputStream != null && th != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable th3) {
                        outputStream.close();
                    }
                }
                throw th2;
            }
        }
    }

    @Override
    public void finishUpdate() throws IOException {
    }
}
