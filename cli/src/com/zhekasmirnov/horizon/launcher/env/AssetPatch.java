package com.zhekasmirnov.horizon.launcher.env;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;

public class AssetPatch {
    private static String assetDirectory = null;
    private static HashMap<String, String> assetOverrides = new HashMap<>();

    private static native void nativeAddSingleOverride(String str, String str2);

    private static native void nativeSetAssetDirectory(String str);

    private static native void nativeSetFsRoot(String str);

    public static void setAssetDirectory(String str) {
        if (str != null) {
            while (true) {
                if (!str.endsWith("/") && !str.endsWith("\\")) {
                    break;
                }
                str = str.substring(0, str.length() - 1);
            }
            str = String.valueOf(str) + "/";
        }
        assetOverrides.clear();
        assetDirectory = str;
    }

    public static void setRootOverrideDirectory(String str) {
        if (str != null) {
            while (true) {
                if (!str.endsWith("/") && !str.endsWith("\\")) {
                    break;
                }
                str = str.substring(0, str.length() - 1);
            }
        }
        nativeSetFsRoot(str);
    }

    public static void addSingleOverride(String str, String str2) {
        assetOverrides.put(str, str2);
    }

    public static void removeSingleOverride(String str) {
        assetOverrides.remove(str);
    }

    public static String getSingleOverride(String str) {
        return assetOverrides.get(str);
    }

    public static String getRedirectedPath(String str) {
        if (assetDirectory != null) {
            String str2 = assetOverrides.get(str);
            if (str2 != null) {
                return str2;
            }
            return String.valueOf(assetDirectory) + str;
        }
        return null;
    }

    public static InputStream getAssetInputStream(Object assetManager, String str) throws IOException {
        InputStream stream = AssetPatch.class.getResourceAsStream(str);
        if (stream == null) {
            throw new IOException("Not found resource " + str + "!");
        }
        return stream;
    }

    public static byte[] getAssetBytes(Object assetManager, String str) throws IOException {
        int read;
        InputStream assetInputStream = getAssetInputStream(assetManager, str);
        if (assetInputStream != null) {
            byte[] bArr = new byte[65536];
            byte[] bArr2 = new byte[0];
            while (true) {
                try {
                    read = assetInputStream.read(bArr);
                    if (read <= 0) {
                        return bArr2;
                    }
                    byte[] bArr3 = new byte[bArr2.length + read];
                    System.arraycopy(bArr2, 0, bArr3, 0, bArr2.length);
                    System.arraycopy(bArr, 0, bArr3, bArr2.length, read);
                    bArr2 = bArr3;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            return null;
        }
    }

    public static String[] listAssets(Object assetManager, String str) {
        Path myPath;
        if (assetDirectory != null) {
            File file = new File(assetDirectory, str);
            if (file.isDirectory()) {
                return file.list();
            }
        }
        try {
            URI uri = AssetPatch.class.getResource("/" + str).toURI();
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath("/" + str, new String[0]);
            } else {
                myPath = Paths.get(uri);
            }
            Stream<Path> walk = Files.walk(myPath, 1, new FileVisitOption[0]);
            try {
                ArrayList<String> entries = new ArrayList<>();
                for (Object path : walk.toArray()) {
                    entries.add(((Path) path).getFileName().toString());
                }
                String[] strArr = (String[]) entries.toArray();
                if (walk != null) {
                    walk.close();
                }
                return strArr;
            } catch (Throwable th) {
                if (walk != null) {
                    walk.close();
                }
                throw th;
            }
        } catch (IOException | URISyntaxException | NullPointerException e) {
            return new String[0];
        }
    }

    public static InputStream open(String str) throws IOException {
        InputStream assetInputStream = getAssetInputStream(null, str);
        System.out.println("redirected asset manager: " + str + " -> " + assetInputStream);
        return assetInputStream;
    }
}
