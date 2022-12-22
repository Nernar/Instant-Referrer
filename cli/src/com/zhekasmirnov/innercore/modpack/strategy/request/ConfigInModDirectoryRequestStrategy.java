package com.zhekasmirnov.innercore.modpack.strategy.request;

import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigInModDirectoryRequestStrategy extends DirectoryRequestStrategy {
    @Override
    public File get(String location, String name) {
        if (location.equalsIgnoreCase("innercore") && name.equalsIgnoreCase("ids.json")) {
            return new File(getAssignedDirectory().getLocation(), ".staticids");
        }
        return new File(get(location), name);
    }

    @Override
    public File get(String location) {
        File redirectDir = null;
        File dir = new File(getAssignedDirectory().getLocation(), location);
        File redirect = new File(dir, ".redirect");
        try {
            String redirectPath = FileUtils.readFileText(redirect).trim();
            redirectDir = new File(redirectPath);
        } catch (IOException e) {
        }
        if (redirectDir != null && redirectDir.isDirectory()) {
            return redirectDir;
        }
        return dir;
    }

    @Override
    public List<File> getAll(String location) {
        List<File> result = new ArrayList<>();
        File[] filesInDir = get(location).listFiles();
        if (filesInDir != null) {
            Collections.addAll(result, filesInDir);
        }
        if ("innercore".equalsIgnoreCase(location)) {
            result.add(get("innercore", "ids.json"));
        }
        return result;
    }

    @Override
    public List<String> getAllLocations() {
        Set<String> locationSet = new HashSet<>();
        locationSet.add("innercore");
        File[] filesInDir = getAssignedDirectory().getLocation().listFiles();
        if (filesInDir != null) {
            for (File file : filesInDir) {
                if (file.isDirectory()) {
                    locationSet.add(file.getName());
                }
            }
        }
        return new ArrayList<>(locationSet);
    }
}
