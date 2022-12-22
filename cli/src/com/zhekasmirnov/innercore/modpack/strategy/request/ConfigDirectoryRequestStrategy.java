package com.zhekasmirnov.innercore.modpack.strategy.request;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ConfigDirectoryRequestStrategy extends DirectoryRequestStrategy {
    private static String normalizeFileName(String name) {
        return name.replaceAll("[\\\\/\\s]", "-");
    }

    @Override
    public File get(String location, String name) {
        String location2 = normalizeFileName(location);
        if (name.startsWith("config/")) {
            return new File(new File(getAssignedDirectory().getLocation(), location2), name.substring(7));
        }
        File location3 = getAssignedDirectory().getLocation();
        return new File(location3, String.valueOf(location2) + "-" + normalizeFileName(name));
    }

    @Override
    public File get(String location) {
        return getAssignedDirectory().getLocation();
    }

    protected void addAllRecursive(File file, List<File> files, Predicate<File> filter) {
        if (!file.isDirectory()) {
            if (file.isFile()) {
                if (filter == null || filter.test(file)) {
                    files.add(file);
                    return;
                }
                return;
            }
            return;
        }
        File[] filesInDir = file.listFiles();
        if (filesInDir != null) {
            for (File child : filesInDir) {
                addAllRecursive(child, files, filter);
            }
        }
    }

    @Override
    public List<File> getAll(String location) {
        List<File> result = new ArrayList<>();
        File[] allFiles = getAssignedDirectory().getLocation().listFiles();
        if (allFiles != null) {
            String prefix = String.valueOf(location.toLowerCase()) + "-";
            for (File file : allFiles) {
                if (file.isDirectory() && file.getName().equalsIgnoreCase(location)) {
                    addAllRecursive(file, result, null);
                } else if (file.isFile() && file.getName().toLowerCase().startsWith(prefix)) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getAllLocations() {
        int separator;
        Set<String> locationSet = new HashSet<>();
        locationSet.add("innercore");
        File[] allFiles = getAssignedDirectory().getLocation().listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                String name = file.getName().toLowerCase();
                if (file.isDirectory() && !name.equals(".keep-unchanged")) {
                    locationSet.add(name);
                } else if (file.isFile() && (separator = name.indexOf(45)) != -1) {
                    locationSet.add(name.substring(0, separator));
                }
            }
        }
        return new ArrayList<>(locationSet);
    }
}
