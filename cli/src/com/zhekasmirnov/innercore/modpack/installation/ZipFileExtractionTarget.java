package com.zhekasmirnov.innercore.modpack.installation;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileExtractionTarget extends ModPackExtractionTarget implements Closeable {
    @SuppressWarnings("unused")
    private final File file;
    private final ZipOutputStream outputStream;

    public ZipFileExtractionTarget(File file) throws FileNotFoundException {
        this.file = file;
        this.outputStream = new ZipOutputStream(new FileOutputStream(file));
    }

    @Override
    public OutputStream write(String name) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        this.outputStream.putNextEntry(entry);
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                ZipFileExtractionTarget.this.outputStream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                ZipFileExtractionTarget.this.outputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                ZipFileExtractionTarget.this.outputStream.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                ZipFileExtractionTarget.this.outputStream.closeEntry();
            }
        };
    }

    @Override
    public void close() throws IOException {
        this.outputStream.close();
    }
}
