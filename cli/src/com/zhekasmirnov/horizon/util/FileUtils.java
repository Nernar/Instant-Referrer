package com.zhekasmirnov.horizon.util;

import com.zhekasmirnov.horizon.activity.util.DialogHelper;
import com.zhekasmirnov.horizon.launcher.env.AssetPatch;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class FileUtils {
    public static String readFileText(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine != null) {
                sb.append(readLine);
                sb.append("\n");
            } else {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
                break;
            }
        }
        return sb.toString();
    }

    public static void writeFileText(File file, String str) throws IOException {
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
        printWriter.write(str);
        printWriter.close();
    }

    public static void addFileText(File file, String str) throws IOException {
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
        printWriter.write(str);
        printWriter.close();
    }

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[1024];
        while (true) {
            int read = inputStream.read(bArr);
            if (read != -1) {
                byteArrayOutputStream.write(bArr, 0, read);
            } else {
                return byteArrayOutputStream.toString("UTF-8");
            }
        }
    }

    public static Object readFileAsBitmap(String str) {
        throw new UnsupportedOperationException();
    }

    public static void writeBitmap(String str, Object bitmap) {
        throw new UnsupportedOperationException();
    }

    public static JSONObject readJSON(File file) throws IOException, JSONException {
        return new JSONObject(readFileText(file));
    }

    public static JSONArray readJSONArray(File file) throws IOException, JSONException {
        return new JSONArray(new JSONTokener(readFileText(file)));
    }

    public static void writeJSON(File file, JSONObject jSONObject) throws IOException {
        writeFileText(file, jSONObject.toString());
    }

    public static void writeJSON(File file, JSONArray jSONArray) throws IOException {
        writeFileText(file, jSONArray.toString());
    }

    public static void copy(File file, File file2) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileOutputStream fileOutputStream = new FileOutputStream(file2);
        ReadableByteChannel newChannel = Channels.newChannel(fileInputStream);
        WritableByteChannel newChannel2 = Channels.newChannel(fileOutputStream);
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(16384);
        while (newChannel.read(allocateDirect) != -1) {
            allocateDirect.flip();
            newChannel2.write(allocateDirect);
            allocateDirect.compact();
        }
        allocateDirect.flip();
        while (allocateDirect.hasRemaining()) {
            newChannel2.write(allocateDirect);
        }
    }

    public static File unpackInputStream(InputStream inputStream, File file, boolean z) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] bArr = new byte[1024];
        while (true) {
            int read = inputStream.read(bArr);
            if (read <= 0) {
                break;
            }
            fileOutputStream.write(bArr, 0, read);
        }
        fileOutputStream.close();
        if (z) {
            inputStream.close();
        }
        return file;
    }

    public static File unpackInputStream(InputStream inputStream, File file) throws IOException {
        return unpackInputStream(inputStream, file, true);
    }

    public static void unpackAssetOrDirectory(Object assetManager, File file, String str) throws IOException {
        String[] list = AssetPatch.listAssets(assetManager, str);
        if (list != null && list.length > 0) {
            file.mkdirs();
            for (String str2 : list) {
                unpackAssetOrDirectory(assetManager, new File(file, str2), new File(str, str2).getPath());
            }
            return;
        }
        unpackInputStream(AssetPatch.getAssetInputStream(assetManager, str), file);
    }

    public static String readStringFromAsset(Object assetManager, String str) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream assetInputStream = AssetPatch.getAssetInputStream(assetManager, str);
        if (assetInputStream == null) {
            throw new IOException("null");
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assetInputStream, "UTF-8"));
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine != null) {
                sb.append(readLine);
                sb.append("\n");
            } else {
                bufferedReader.close();
                return sb.toString();
            }
        }
    }

    public static JSONObject readJSONFromAssets(Object assetManager, String str) throws IOException, JSONException {
        return new JSONObject(readStringFromAsset(assetManager, str));
    }

    public static void clearFileTree(File file, boolean z) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File file2 : file.listFiles()) {
                    clearFileTree(file2, true);
                }
            }
            if (z) {
                file.delete();
            }
        }
    }

    public static void copyFileTree(File file, File file2, DialogHelper.ProgressDialogHolder progressDialogHolder, String str) throws IOException {
        if (!file2.isDirectory() && !file2.mkdirs()) {
            throw new IOException("mkdirs failed: " + file2);
        }
        for (String str2 : file.list()) {
            if (progressDialogHolder != null && progressDialogHolder.isTerminated()) {
                return;
            }
            File file3 = new File(file, str2);
            File file4 = new File(file2, str2);
            if (file3.isDirectory()) {
                copyFileTree(file3, file4, progressDialogHolder, String.valueOf(str) + str2 + "/");
            } else {
                if (progressDialogHolder != null) {
                    progressDialogHolder.onDownloadMessage(str2);
                }
                copy(file3, file4);
            }
        }
    }

    private static void debugFileTree(File file, String str) {
        if (file.exists()) {
            System.out.println(String.valueOf(str) + " |-" + file.getName());
            if (file.isDirectory()) {
                for (File file2 : file.listFiles()) {
                    debugFileTree(file2, String.valueOf(str) + "  ");
                }
                return;
            }
            return;
        }
        System.out.println(String.valueOf(str) + "| file does not exist: " + file);
    }

    public static void debugFileTree(File file) {
        debugFileTree(file, "");
    }

    public static void debugFileTreeViaLogger(File file, String str) {
        if (file.exists()) {
            Logger.debug("FILE-TREE", String.valueOf(str) + " |-" + file.getName());
            if (file.isDirectory()) {
                for (File file2 : file.listFiles()) {
                    debugFileTreeViaLogger(file2, String.valueOf(str) + "  ");
                }
                return;
            }
            return;
        }
        Logger.debug("FILE-TREE", String.valueOf(str) + "| file does not exist: " + file);
    }

    public static String cleanupPath(String str) {
        String replaceAll = str.replaceAll("\\\\", "/");
        while (true) {
            String replaceAll2 = replaceAll;
            if (replaceAll2.startsWith("/")) {
                replaceAll = replaceAll2.substring(1);
            } else {
                return replaceAll2;
            }
        }
    }

    @SuppressWarnings("unused")
    private static void deleteFileTree(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File file2 : file.listFiles()) {
                    deleteFileTree(file2);
                }
            }
            file.delete();
        }
    }

    private static void getAllRelativePaths(File file, File file2, List<String> list, boolean z) {
        if (z || file.isFile()) {
            list.add(cleanupPath(file.getAbsolutePath().substring(file2.getAbsolutePath().length())));
        }
        if (file.isDirectory()) {
            for (File file3 : file.listFiles()) {
                getAllRelativePaths(file3, file2, list, z);
            }
        }
    }

    public static List<String> getAllRelativePaths(File file, boolean z) {
        ArrayList<String> arrayList = new ArrayList<>();
        getAllRelativePaths(file, file, arrayList, z);
        return arrayList;
    }

    public static boolean zipDirectory(File file, File file2) throws IOException {
        List<String> allRelativePaths = getAllRelativePaths(file, false);
        if (allRelativePaths.size() == 0) {
            return false;
        }
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file2)));
        byte[] bArr = new byte[4096];
        for (String str : allRelativePaths) {
            File file3 = new File(file, str);
            Logger.debug("Compress", "Adding: " + file3);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file3), 4096);
            zipOutputStream.putNextEntry(new ZipEntry(str));
            while (true) {
                int read = bufferedInputStream.read(bArr, 0, 4096);
                if (read != -1) {
                    zipOutputStream.write(bArr, 0, read);
                } else {
                    break;
                }
            }
            bufferedInputStream.close();
        }
        zipOutputStream.close();
        return true;
    }

    public static boolean getFileFlag(File file, String str) {
        return new File(file, "." + str).exists();
    }

    public static void setFileFlag(File file, String str, boolean z) {
        File file2 = new File(file, "." + str);
        if (z) {
            if (file2.exists()) {
                return;
            }
            try {
                file2.createNewFile();
                return;
            } catch (IOException e) {
                throw new RuntimeException("failed to set flag: " + file2, e);
            }
        }
        file2.delete();
    }
}
