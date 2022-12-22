package org.mineprogramming.horizon.innercore.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadHelper {

    public interface FileDownloadListener {
        void onDownloadProgress(long j, long j2);
    }

    public static String downloadString(String from) throws IOException {
        URL url = new URL(from);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        while (true) {
            String json = bufferedReader.readLine();
            if (json != null) {
                sb.append(String.valueOf(json) + "\n");
            } else {
                bufferedReader.close();
                con.disconnect();
                return sb.toString().trim();
            }
        }
    }

    public static void downloadFile(String from, File to, FileDownloadListener progressListener) throws IOException {
        URL url = new URL(from);
        URLConnection conection = url.openConnection();
        conection.connect();
        long total = conection.getContentLength();
        InputStream input = new BufferedInputStream(url.openStream(), 8192);
        OutputStream output = new FileOutputStream(to);
        byte[] data = new byte[1024];
        long progress = 0;
        while (true) {
            int count = input.read(data);
            if (count != -1) {
                URL url2 = url;
                long progress2 = progress + count;
                output.write(data, 0, count);
                if (progressListener != null) {
                    progressListener.onDownloadProgress(progress2, total);
                }
                progress = progress2;
                url = url2;
            } else {
                output.flush();
                output.close();
                input.close();
                return;
            }
        }
    }

    public static boolean isOnline(Object context) {
        return true;
    }
}
