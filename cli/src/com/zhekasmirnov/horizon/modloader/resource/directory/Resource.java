package com.zhekasmirnov.horizon.modloader.resource.directory;

import com.zhekasmirnov.horizon.modloader.resource.ResourceOverride;
import com.zhekasmirnov.horizon.util.StringUtils;
import java.io.File;
import java.util.List;

public class Resource {
    public static final String DEFAULT_RESOURCE_PACK = "resource_packs/vanilla/";
    public static final String RESOURCE_INDEX_SEPARATOR = "_";
    public final ResourceDirectory directory;
    private String extension;
    public final File file;
    private boolean hasIndex;
    private int index;
    private ResourceMeta meta;
    private String name;
    private String path;

    public Resource(ResourceDirectory resourceDirectory, File file, String str) {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("directory file is passed to Resource constructor (" + file + ")");
        }
        this.directory = resourceDirectory;
        this.file = file;
        this.path = str;
        initPath();
    }

    public Resource(ResourceDirectory resourceDirectory, File file) {
        this(resourceDirectory, file, resourceDirectory.getResourceName(file));
    }

    private void initPath() {
        Integer integerOrNull;
        int lastIndexOf = this.path.lastIndexOf(47);
        int lastIndexOf2 = this.path.lastIndexOf(46);
        this.hasIndex = false;
        this.index = 0;
        if (lastIndexOf2 != -1 && lastIndexOf2 > lastIndexOf) {
            this.extension = this.path.substring(lastIndexOf2 + 1);
            this.name = this.path.substring(lastIndexOf + 1, lastIndexOf2);
            this.path = this.path.substring(0, lastIndexOf2);
        }
        int lastIndexOf3 = this.name.lastIndexOf("_");
        if (lastIndexOf3 != -1 && (integerOrNull = StringUtils.toIntegerOrNull(this.name.substring(lastIndexOf3 + 1))) != null) {
            this.index = Math.max(0, integerOrNull.intValue());
            this.hasIndex = true;
            this.name = this.name.substring(0, lastIndexOf3);
            String str = this.path;
            this.path = str.substring(0, (str.length() - this.name.length()) + lastIndexOf3);
        }
        File file = new File(String.valueOf(this.file.getAbsolutePath()) + ".meta");
        if (!file.exists()) {
            File file2 = this.directory.directory;
            file = new File(file2, String.valueOf(this.path) + ".meta");
        }
        this.meta = file.exists() ? new ResourceMeta(file) : null;
    }

    public String getPath() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(this.path);
        if (this.extension.length() > 0) {
            str = "." + this.extension;
        } else {
            str = "";
        }
        sb.append(str);
        return sb.toString();
    }

    public String getPathWithIndex() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(this.path);
        sb.append("_");
        sb.append(this.index);
        if (this.extension.length() > 0) {
            str = "." + this.extension;
        } else {
            str = "";
        }
        sb.append(str);
        return sb.toString();
    }

    public String getRealPath() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(this.path);
        String str2 = "";
        if (this.hasIndex) {
            str = "_" + this.index;
        } else {
            str = "";
        }
        sb.append(str);
        if (this.extension.length() > 0) {
            str2 = "." + this.extension;
        }
        sb.append(str2);
        return sb.toString();
    }

    public String getPathWithoutExtension() {
        return this.path;
    }

    public String getAtlasPath() {
        return String.valueOf(this.path) + "_" + this.index;
    }

    public String getName() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        if (this.extension.length() > 0) {
            str = "." + this.extension;
        } else {
            str = "";
        }
        sb.append(str);
        return sb.toString();
    }

    public String getNameWithoutExtension() {
        return this.name;
    }

    public String getNameWithIndex() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append("_");
        sb.append(this.index);
        if (this.extension.length() > 0) {
            str = "." + this.extension;
        } else {
            str = "";
        }
        sb.append(str);
        return sb.toString();
    }

    public String getRealName() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        String str2 = "";
        if (this.hasIndex) {
            str = "_" + this.index;
        } else {
            str = "";
        }
        sb.append(str);
        if (this.extension.length() > 0) {
            str2 = "." + this.extension;
        }
        sb.append(str2);
        return sb.toString();
    }

    public boolean hasIndex() {
        return this.hasIndex;
    }

    public int getIndex() {
        return this.index;
    }

    public String getExtension() {
        return this.extension;
    }

    public ResourceMeta getMeta() {
        return this.meta;
    }

    public Resource getLink(String str) {
        return new Resource(this.directory, this.file, str);
    }

    public void addOverrides(List<ResourceOverride> list) {
        list.add(new ResourceOverride(getPath(), this.file));
        list.add(new ResourceOverride(getPathWithIndex(), this.file));
    }
}
