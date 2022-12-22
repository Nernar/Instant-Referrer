package com.zhekasmirnov.innercore.utils;

import com.cedarsoftware.util.io.JsonWriter;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.horizon.launcher.env.AssetPatch;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class FileTools {
    public static final String LOGGER_TAG = "INNERCORE-FILE";
    @SuppressWarnings("unused")
    private static Object mcTypeface;
    public static String DIR_PACK = null;
    public static String DIR_WORK = null;
    public static String DIR_ROOT = String.valueOf(System.getProperty("user.dir")) + "/";
    public static String DIR_HORIZON = String.valueOf(DIR_ROOT) + "games/horizon/";
    public static String DIR_MINECRAFT = String.valueOf(MinecraftVersions.getCurrent().getMinecraftExternalStoragePath().getAbsolutePath()) + "/";

    public static void initializeDirectories(File packPath) {
        DIR_PACK = String.valueOf(packPath.getAbsolutePath()) + "/";
        DIR_WORK = String.valueOf(DIR_PACK) + "innercore/";
        checkdirs();
    }

    public static String assureAndGetCrashDir() {
        String path = String.valueOf(DIR_WORK) + "crash-dump/";
        assureDir(path);
        return path;
    }

    public static File unpackInputStream(InputStream inputStream, String path) throws IOException {
        File outputFile = new File(path);
        outputFile.createNewFile();
        OutputStream outputStream = new FileOutputStream(outputFile);
        byte[] bytes = new byte[1024];
        while (true) {
            int read = inputStream.read(bytes);
            if (read != -1) {
                outputStream.write(bytes, 0, read);
            } else {
                outputStream.close();
                inputStream.close();
                return outputFile;
            }
        }
    }

    public static boolean assetExists(String name) throws IOException {
        InputStream is = getAssetInputStream(name);
        if (is == null) {
            return false;
        }
        try {
            is.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static InputStream getAssetInputStream(String name) throws IOException {
        return AssetPatch.getAssetInputStream(null, name);
    }

    public static byte[] getAssetBytes(String name) throws IOException {
        return AssetPatch.getAssetBytes(null, name);
    }

    public static byte[] getAssetBytes(String name, String[] searchPaths, boolean includeAbsPath) throws IOException {
        byte[] bytes;
        if (includeAbsPath && (bytes = AssetPatch.getAssetBytes(null, name)) != null) {
            return bytes;
        }
        for (String searchPath : searchPaths) {
            byte[] bytes2 = AssetPatch.getAssetBytes(null, String.valueOf(searchPath) + name);
            if (bytes2 != null) {
                return bytes2;
            }
        }
        return null;
    }

    public static Object bitmapFromBytes(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    public static Object getAssetAsBitmap(String name) {
        throw new UnsupportedOperationException();
    }

    public static String[] listAssets(String dir) {
        return AssetPatch.listAssets(null, dir);
    }

    public static String getAssetAsString(String name) throws IOException {
        return new String(getAssetBytes(name));
    }

    public static JSONObject getAssetAsJSON(String name) throws JSONException, IOException {
        return new JSONObject(getAssetAsString(name));
    }

    public static JSONArray getAssetAsJSONArray(String name) throws JSONException, IOException {
        return new JSONArray(getAssetAsString(name));
    }

    public static File unpackResource(int resource, String path) throws IOException {
        throw new UnsupportedOperationException();
    }

    public static File unpackAsset(String name, String path) throws IOException {
        InputStream inputStream = AssetPatch.getAssetInputStream(null, name);
        return unpackInputStream(inputStream, path);
    }

    public static void unpackAssetDir(String name, String path) {
    }

    public static void checkdirs() {
        File dir = new File(DIR_WORK);
        if (!dir.exists()) {
            boolean succeeded = dir.mkdirs();
            if (succeeded) {
                Logger.debug("INNERCORE-FILE", "created work directory: " + DIR_WORK);
                return;
            } else {
                Logger.debug("INNERCORE-FILE", "failed to create work directory: " + DIR_WORK);
                return;
            }
        }
        Logger.debug("INNERCORE-FILE", "work directory check successful");
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }

    public static boolean mkdirs(String path) {
        return new File(path).mkdirs();
    }

    public static boolean assureDir(String path) {
        if (!exists(path)) {
            return mkdirs(path);
        }
        return true;
    }

    public static boolean assureFileDir(File file) {
        String path = file.getAbsolutePath();
        String dir = path.substring(0, path.lastIndexOf("/"));
        return assureDir(dir);
    }

    public static Object getMcTypeface() {
        throw new UnsupportedOperationException();
    }

    public static String[] listDirectory(String path) {
        File directory = new File(path);
        return directory.list();
    }

    public static String readFileText(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
        String str = "";
        while (true) {
            String text = str;
            String line = reader.readLine();
            if (line != null) {
                str = String.valueOf(text) + line + "\n";
            } else {
                reader.close();
                return text;
            }
        }
    }

    public static void writeFileText(String path, String text) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
        writer.write(text);
        writer.close();
    }

    public static void addFileText(String path, String text) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
        writer.write(text);
        writer.close();
    }

    public static Object readFileAsBitmap(String path) {
        throw new UnsupportedOperationException();
    }

    public static void writeBitmap(String path, Object bmp) {
        throw new UnsupportedOperationException();
    }

    public static JSONObject readJSON(String path) throws IOException, JSONException {
        return new JSONObject(readFileText(path));
    }

    public static JSONArray readJSONArray(String path) throws IOException, JSONException {
        return new JSONArray(new JSONTokener(readFileText(path)));
    }

    public static void writeJSON(String path, JSONObject json) throws IOException {
        String result = json.toString();
        writeFileText(path, JsonWriter.formatJson(result));
    }

    public static void writeJSON(String path, JSONArray json) throws IOException {
        String result = json.toString();
        writeFileText(path, JsonWriter.formatJson(result));
    }

    public static String getPrettyPath(File dir, File fileInDir) {
        String dirPath = dir == null ? "" : dir.getAbsolutePath();
        String filePath = fileInDir == null ? "" : fileInDir.getAbsolutePath();
        return filePath.substring(dirPath.length() + 1);
    }

    public static void inStreamToOutStream(InputStream inputStream, OutputStream outputStream) throws IOException {
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
    }

    public static void copy(File src, File dst) throws IOException {
        FileInputStream inputStream = new FileInputStream(src);
        FileOutputStream outputStream = new FileOutputStream(dst);
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
    }

    public static byte[] convertStreamToBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int length = inputStream.read(buffer);
            if (length != -1) {
                result.write(buffer, 0, length);
            } else {
                return result.toByteArray();
            }
        }
    }

    public static void delete(String path) {
        deleteRecursive(new File(path));
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursive(child);
            }
        }
        file.delete();
    }
}
