package com.pdaxrom.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {
    private static final String TAG = "Utils";

    public static native int chmod(String str, int i);

    public static native void close(FileDescriptor fileDescriptor);

    public static native FileDescriptor createSubProcess(String str, String str2, String[] strArr, String[] strArr2, int[] iArr);

    public static native void hangupProcessGroup(int i);

    public static native int readByte(FileDescriptor fileDescriptor);

    public static native void setPtyUTF8Mode(FileDescriptor fileDescriptor, boolean z);

    public static native void setPtyWindowSize(FileDescriptor fileDescriptor, int i, int i2, int i3, int i4);

    public static native void symlink(String str, String str2);

    public static native int unzip(String str, String str2, String str3);

    public static native int unzippedSize(String str);

    public static native int waitFor(int i);

    public static native int writeByte(FileDescriptor fileDescriptor, int i);

    public static void copyDirectory(File file, File file2) throws IOException {
        if (file.isDirectory()) {
            if (!file2.exists()) {
                file2.mkdir();
            }
            String[] list = file.list();
            for (int i = 0; i < list.length; i++) {
                copyDirectory(new File(file, list[i]), new File(file2, list[i]));
            }
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        FileOutputStream fileOutputStream = new FileOutputStream(file2);
        byte[] bArr = new byte[1048576];
        while (true) {
            int read = fileInputStream.read(bArr);
            if (read > 0) {
                fileOutputStream.write(bArr, 0, read);
            } else {
                fileInputStream.close();
                fileOutputStream.close();
                return;
            }
        }
    }

    public static void deleteDirectory(File file) {
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                file.delete();
                return;
            }
            for (String str : file.list()) {
                deleteDirectory(new File(file, str));
            }
            if (file.list().length == 0) {
                file.delete();
                return;
            }
            return;
        }
        file.delete();
    }

    public static String humanReadableByteCount(long j, boolean z) {
        int i = z ? 1000 : 1024;
        if (j < i) {
            return String.valueOf(j) + " B";
        }
        double d = j;
        double d2 = i;
        int log = (int) (Math.log(d) / Math.log(d2));
        StringBuilder sb = new StringBuilder();
        sb.append((z ? "kMGTPE" : "KMGTPE").charAt(log - 1));
        sb.append(z ? "" : "i");
        String sb2 = sb.toString();
        double pow = Math.pow(d2, log);
        Double.isNaN(d);
        return String.format("%.1f %sB", Double.valueOf(d / pow), sb2);
    }

    public static void emptyDirectory(File file) {
        deleteDirectory(file);
        file.mkdirs();
    }
}
