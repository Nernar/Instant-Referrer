package com.zhekasmirnov.innercore.mod.resource.pack;

import com.zhekasmirnov.innercore.mod.resource.types.ResourceFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ResourcePack implements IResourcePack {
    private String dir;
    public ArrayList<ResourceFile> resourceFiles = new ArrayList<>();
    public boolean isLoaded = false;

    public ResourcePack(String dir) {
        this.dir = dir;
    }

    @Override
    public String getAbsolutePath() {
        return this.dir;
    }

    @Override
    public String getPackName() {
        return this.dir.substring(this.dir.lastIndexOf("/") + 1);
    }

    public void readAllFiles() {
        this.resourceFiles.clear();
        try {
            findFilesInDir(new File(this.dir), this.resourceFiles);
            this.isLoaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findFilesInDir(File dir, ArrayList<ResourceFile> files) throws IOException {
        File[] filesInDir = dir.listFiles();
        for (File file : filesInDir) {
            if (file.isDirectory()) {
                findFilesInDir(file, files);
            } else {
                files.add(new ResourceFile(this, file));
            }
        }
    }
}
