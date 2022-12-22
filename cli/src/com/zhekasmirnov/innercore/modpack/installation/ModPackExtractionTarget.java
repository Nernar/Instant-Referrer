package com.zhekasmirnov.innercore.modpack.installation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public abstract class ModPackExtractionTarget {
    public abstract OutputStream write(String str) throws IOException;

    public void writeFile(String name, File file) throws IOException {
        OutputStream outputStream = write(name);
        try {
            InputStream inputStream = new FileInputStream(file);
            ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
            WritableByteChannel outputChannel = Channels.newChannel(outputStream);
            ByteBuffer buffer = ByteBuffer.allocateDirect(16384);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.compact();
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
            if (inputStream != null) {
                inputStream.close();
            }
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
}
