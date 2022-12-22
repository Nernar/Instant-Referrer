package com.zhekasmirnov.horizon.compiler.packages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RepoUtils {
    public static String CPU_API;
    private static String NDK_ARCH;
    private static int NDK_VERSION;

    public static void setVersion() {
        CPU_API = "x86";
        NDK_ARCH = "i686";
        NDK_VERSION = 14;
    }

    public static boolean isContainsPackage(List<PackageInfo> list, String str) {
        for (PackageInfo packageInfo : list) {
            if (packageInfo.getName().equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isContainsPackage(List<PackageInfo> list, String str, String str2) {
        for (PackageInfo packageInfo : list) {
            if (packageInfo.getName().equals(str) && packageInfo.getVersion().equals(str2)) {
                return true;
            }
        }
        return false;
    }

    public static PackageInfo getPackageByName(List<PackageInfo> list, String str) {
        for (PackageInfo packageInfo : list) {
            if (packageInfo.getName().equals(str)) {
                return packageInfo;
            }
        }
        return null;
    }

    public static String replaceMacro(String str) {
        return str != null ? str.replaceAll("\\$\\{HOSTARCH\\}", NDK_ARCH).replaceAll("\\$\\{HOSTNDKARCH\\}", NDK_ARCH).replaceAll("\\$\\{HOSTNDKVERSION\\}", String.valueOf(NDK_VERSION)) : str;
    }

    public static List<PackageInfo> checkingForUpdates(List<PackageInfo> list, List<PackageInfo> list2) {
        ArrayList<PackageInfo> arrayList = new ArrayList<>();
        Iterator<PackageInfo> it = list2.iterator();
        if (it.hasNext()) {
            PackageInfo packageInfo = it.next();
            Iterator<PackageInfo> it2 = list.iterator();
            while (true) {
                if (it2.hasNext()) {
                    PackageInfo next = it2.next();
                    if (packageInfo.getName().equals(next.getName()) && !packageInfo.getVersion().equals(next.getVersion())) {
                        arrayList.add(next);
                    }
                }
            }
        } else {
            return arrayList;
        }
    }
}
