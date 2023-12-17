package com.zhekasmirnov.horizon.compiler.packages;

import com.pdaxrom.utils.Utils;
import com.zhekasmirnov.horizon.runtime.logger.CoreLogger;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Environment {
    public static final String APPLICATION_DIR_NAME = "CCPlusPlusNIDE";
    @SuppressWarnings("unused")
    private static final String TAG = "Environment";
    @SuppressWarnings("unused")
    private static String[] abiListOverride;
    private static ExternalDirectoryManager externalDirectoryManager;

    public static String getShell(Object context) {
        return "/system/bin/sh";
    }

    public static String[] getSupportedABIs() {
        return (String[]) getSupportedAbiList().toArray(new String[0]);
    }

    public static List<String> getSupportedAbiList() {
        return new ArrayList<>();
    }

    public static ExternalDirectoryManager getExternalDirectoryManager() {
        if (externalDirectoryManager == null) {
            externalDirectoryManager = new ExternalDirectoryManager(null);
        }
        return externalDirectoryManager;
    }

    public static void setAbiListOverride(String[] strArr) {
        abiListOverride = strArr;
        CoreLogger.log("Environment", "setting ABI list override: " + getSupportedAbiList());
    }

    public static File getExternalHorizonDirectory() {
        return getExternalHorizonDirectoryByLocation(getExternalDirectoryManager().getLocation());
    }

    public static File getExternalHorizonDirectoryByLocation(ExternalDirectoryManager.Location location) {
        if (location == ExternalDirectoryManager.Location.INTERNAL) {
            return new File(getDataDirFile(), "horizon");
        }
        return new File(System.getProperty("user.dir"), "games/horizon");
    }

    public static File getDataDirFile(Object context) {
        return new File(mkdirIfNotExist(new File(System.getProperty("user.dir"), "files")));
    }

    public static File getDataDirFile() {
        return getDataDirFile(null);
    }

    public static File getPackExecutionDir(Object context) {
        return new File(getDataDirFile(), "soexec_pack");
    }

    public static File getApplicationLibraryDirectory() {
        return new File(getDataDirFile(), "soexec_app");
    }

    public static String getToolchainsDir(Object context) {
        return mkdirIfNotExist(new File(getDataDirFile(), "root"));
    }

    public static String getJavacDir(Object context) {
        return mkdirIfNotExist(new File(getDataDirFile(), "javac"));
    }

    public static String getCCtoolsDir(Object context) {
        return mkdirIfNotExist(new File(getToolchainsDir(context), "cctools"));
    }

    public static String getServiceDir(Object context) {
        return mkdirIfNotExist(new File(getToolchainsDir(context), "/cctools/services").getAbsolutePath());
    }

    public static String getHomeDir(Object context) {
        return mkdirIfNotExist(new File(getToolchainsDir(context), "/cctools/home").getAbsolutePath());
    }

    public static String getInstalledPackageDir(Object context) {
        return mkdirIfNotExist(new File(getToolchainsDir(context), "installed").getAbsolutePath());
    }

    public static String getDalvikCacheDir(Object context) {
        return mkdirIfNotExist(new File(getToolchainsDir(context), "cctools/var/dalvik/dalvik-cache"));
    }

    public static String getTmpExeDir(Object context) {
        return mkdirIfNotExist(new File(getToolchainsDir(context), "tmpdir"));
    }

    public static String getSdCardHomeDir() {
        return mkdirIfNotExist(new File(getDataDirFile(), APPLICATION_DIR_NAME));
    }

    public static String getSdCardBackupDir() {
        return mkdirIfNotExist(String.valueOf(getSdCardHomeDir()) + "/backup");
    }

    public static String getSdCardTmpDir() {
        return mkdirIfNotExist(String.valueOf(getSdCardHomeDir()) + "/tmp");
    }

    public static String getSdCardSourceDir() {
        return mkdirIfNotExist(new File(getSdCardHomeDir(), "src"));
    }

    private static String mkdirIfNotExist(String str) {
        return mkdirIfNotExist(new File(str));
    }

    private static String mkdirIfNotExist(File file) {
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    public static String[] buildDefaultEnv(Object context) {
        String cCtoolsDir = getCCtoolsDir(context);
        String tmpExeDir = getTmpExeDir(context);
        return new String[]{"TMPDIR=" + tmpExeDir, "TMPEXEDIR=" + tmpExeDir, "TEMPDIR=" + tmpExeDir, "TEMP=" + tmpExeDir, "PATH=" + joinPath(String.valueOf(cCtoolsDir) + "/bin", String.valueOf(cCtoolsDir) + "/sbin", System.getenv("PATH")), "HOME=" + getHomeDir(context), "ANDROID_ASSETS=" + getEnv("ANDROID_ASSETS", "/system/app"), "ANDROID_BOOTLOGO=" + getEnv("ANDROID_BOOTLOGO", "1"), "ANDROID_DATA=" + joinPath(String.valueOf(cCtoolsDir) + "/var/dalvik", getEnv("ANDROID_DATA", (String) null)), "ANDROID_ROOT=" + getEnv("ANDROID_ROOT", "/system"), "ANDROID_PROPERTY_WORKSPACE=" + getEnv(context, "ANDROID_PROPERTY_WORKSPACE"), "BOOTCLASSPATH=" + getBootClassPath(), "EXTERNAL_STORAGE=" + System.getProperty("user.dir"), "LD_LIBRARY_PATH=" + joinPath(String.valueOf(cCtoolsDir) + "/lib", getEnv("LD_LIBRARY_PATH", (String) null)), "CCTOOLSDIR=" + cCtoolsDir, "CFGDIR=" + getShareDir(context), "SHELL=" + getShell(context), "TERM=", "PS1=$ ", "SDDIR=" + getSdCardHomeDir()};
    }

    private static String getShareDir(Object context) {
        return mkdirIfNotExist(new File(getCCtoolsDir(context), "share"));
    }

    private static String getBootClassPath() {
        String env = getEnv("BOOTCLASSPATH", (String) null);
        if (env == null) {
            env = findBootClassPath();
        }
        return (env == null || env.isEmpty()) ? "/system/framework/core.jar:/system/framework/ext.jar:/system/framework/framework.jar:/system/framework/android.policy.jar:/system/framework/services.jar" : env;
    }

    private static String findBootClassPath() {
        File file = new File("/system/framework");
        String str = null;
        if (file.exists() && file.isDirectory()) {
            String[] list = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File file2, String str2) {
                    return str2.toLowerCase().endsWith(".jar");
                }
            });
            for (int i = 0; i < list.length; i++) {
                String str2 = list[i];
                if (i != 0) {
                    str = String.valueOf(str) + ":";
                }
                str = String.valueOf(str) + "/system/framework/" + str2;
            }
        }
        return str;
    }

    private static String getEnv(String str, String str2) {
        String str3 = System.getenv(str);
        return str3 != null ? str3 : str2;
    }

    private static String joinPath(String... strArr) {
        StringBuilder sb = new StringBuilder();
        for (String str : strArr) {
            if (str != null && !str.isEmpty()) {
                if (sb.length() != 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(str);
            }
        }
        return sb.toString();
    }

    protected static String getEnv(Object context, String str) {
        String readLine;
        String cCtoolsDir = getCCtoolsDir(context);
        StringBuilder sb = new StringBuilder();
        sb.append("ANDROID_DATA=");
        String str2 = null;
        sb.append(joinPath(String.valueOf(cCtoolsDir) + "/var/dalvik", getEnv("ANDROID_DATA", (String) null)));
        String[] strArr = {"TMPDIR=" + getSdCardTmpDir(), "PATH=" + joinPath(String.valueOf(cCtoolsDir) + "/bin", String.valueOf(cCtoolsDir) + "/sbin", System.getenv("PATH")), "ANDROID_ASSETS=" + getEnv("ANDROID_ASSETS", "/system/app"), "ANDROID_BOOTLOGO=" + getEnv("ANDROID_BOOTLOGO", "1"), sb.toString(), "ANDROID_ROOT=" + getEnv("ANDROID_ROOT", "/system"), "CCTOOLSDIR=" + cCtoolsDir, "LD_LIBRARY_PATH=" + joinPath(String.valueOf(cCtoolsDir) + "/lib", getEnv("LD_LIBRARY_PATH", (String) null)), "HOME=" + getHomeDir(context), "SHELL=" + getShell(context), "TERM=xterm", "PS1=$ ", "SDDIR=" + getSdCardHomeDir(), "EXTERNAL_STORAGE=" + System.getProperty("user.dir")};
        String[] strArr2 = {"/system/bin/sh", "-c", "set"};
        int[] iArr = new int[1];
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(Utils.createSubProcess(cCtoolsDir, strArr2[0], strArr2, strArr, iArr)));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
        while (true) {
            try {
                readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                } else if (readLine.startsWith(String.valueOf(str) + "=") && readLine.contains("=")) {
                    str2 = readLine.substring(readLine.indexOf("=") + 1);
                }
            } catch (Exception e) {
                break;
            }
        }
        try {
            dataInputStream.close();
        } catch (IOException e2) {
        }
        Utils.waitFor(iArr[0]);
        return str2;
    }

    public static String[] join(String[] strArr, String[] strArr2) {
        String[] strArr3 = new String[strArr.length + strArr2.length];
        System.arraycopy(strArr, 0, strArr3, 0, strArr.length);
        System.arraycopy(strArr2, 0, strArr3, strArr.length, strArr2.length);
        return strArr3;
    }

    public static void mkdirs(Object context) {
        getServiceDir(context);
        getHomeDir(context);
        getToolchainsDir(context);
        getSdCardHomeDir();
        getSdCardBackupDir();
        getSdCardTmpDir();
    }

    public static String getSdCardDir() {
        return new File(System.getProperty("user.dir")).getAbsolutePath();
    }

    public static String getSdCardExampleDir() {
        return mkdirIfNotExist(new File(getSdCardHomeDir(), "Examples"));
    }
}
