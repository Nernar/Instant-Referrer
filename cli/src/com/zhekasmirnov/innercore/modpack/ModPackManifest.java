package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.LocaleUtils;
import com.zhekasmirnov.innercore.modpack.strategy.extract.AllFilesDirectoryExtractStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.DefaultDirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.DirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.NoAccessDirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.CacheDirectoryUpdateStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.DirectoryUpdateStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.JsonMergeDirectoryUpdateStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.ResourceDirectoryUpdateStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.UserDataDirectoryUpdateStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModPackManifest {
    private String author;
    private final List<DeclaredDirectory> declaredDirectories = new ArrayList<>();
    private String description;
    private String displayedName;
    private String packName;
    private File source;
    private int versionCode;
    private String versionName;

    public enum DeclaredDirectoryType {
        RESOURCE,
        USER_DATA,
        CONFIG,
        CACHE,
        INVALID;
        
        private static volatile int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$modpack$ModPackManifest$DeclaredDirectoryType;

        public static DeclaredDirectoryType[] valuesCustom() {
            DeclaredDirectoryType[] valuesCustom = values();
            int length = valuesCustom.length;
            DeclaredDirectoryType[] declaredDirectoryTypeArr = new DeclaredDirectoryType[length];
            System.arraycopy(valuesCustom, 0, declaredDirectoryTypeArr, 0, length);
            return declaredDirectoryTypeArr;
        }

        static int[] $SWITCH_TABLE$com$zhekasmirnov$innercore$modpack$ModPackManifest$DeclaredDirectoryType() {
            int[] iArr = $SWITCH_TABLE$com$zhekasmirnov$innercore$modpack$ModPackManifest$DeclaredDirectoryType;
            if (iArr != null) {
                return iArr;
            }
            int[] iArr2 = new int[valuesCustom().length];
            try {
                iArr2[CACHE.ordinal()] = 4;
            } catch (NoSuchFieldError unused) {
            }
            try {
                iArr2[CONFIG.ordinal()] = 3;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                iArr2[INVALID.ordinal()] = 5;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                iArr2[RESOURCE.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                iArr2[USER_DATA.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
            $SWITCH_TABLE$com$zhekasmirnov$innercore$modpack$ModPackManifest$DeclaredDirectoryType = iArr2;
            return iArr2;
        }

        public static DirectoryRequestStrategy createDirectoryRequestStrategy(DeclaredDirectoryType type) {
            switch ($SWITCH_TABLE$com$zhekasmirnov$innercore$modpack$ModPackManifest$DeclaredDirectoryType()[type.ordinal()]) {
                case 1:
                case 2:
                case 3:
                case 4:
                    return new DefaultDirectoryRequestStrategy();
                default:
                    return new NoAccessDirectoryRequestStrategy();
            }
        }

        public static DirectoryUpdateStrategy createDirectoryUpdateStrategy(DeclaredDirectoryType type) {
            int i = type.ordinal();
            if (i == 1) {
                return new ResourceDirectoryUpdateStrategy();
            }
            switch (i) {
                case 3:
                    return new CacheDirectoryUpdateStrategy();
                case 4:
                    return new JsonMergeDirectoryUpdateStrategy();
                default:
                    return new UserDataDirectoryUpdateStrategy();
            }
        }
    }

    public class DeclaredDirectory {
        public final String path;
        public final DeclaredDirectoryType type;

        public DeclaredDirectory(DeclaredDirectoryType type, String path) {
            this.type = type;
            this.path = path;
        }

        public String getPath() {
            return this.path;
        }

        public DeclaredDirectoryType getType() {
            return this.type;
        }
    }

    public void loadJson(JSONObject json) {
        this.packName = json.optString("packName", json.optString("name"));
        this.displayedName = LocaleUtils.resolveLocaleJsonProperty(json, "displayedName");
        this.versionName = LocaleUtils.resolveLocaleJsonProperty(json, "versionName");
        this.versionCode = json.optInt("versionCode");
        this.author = LocaleUtils.resolveLocaleJsonProperty(json, "author");
        this.description = LocaleUtils.resolveLocaleJsonProperty(json, "description");
        this.declaredDirectories.clear();
        JSONArray directories = json.optJSONArray("directories");
        if (directories != null) {
            for (int i = 0; i < directories.length(); i++) {
                JSONObject directory = directories.optJSONObject(i);
                if (directory != null) {
                    String path = directory.optString("path");
                    String typeName = directory.optString("type");
                    if ((path == null || path.length() == 0) && typeName != null && typeName.length() != 0) {
                        DeclaredDirectoryType type = DeclaredDirectoryType.INVALID;
                        try {
                            type = DeclaredDirectoryType.valueOf(typeName);
                        } catch (IllegalArgumentException e) {
                        }
                        this.declaredDirectories.add(new DeclaredDirectory(type, path));
                    }
                }
            }
        }
    }

    public void loadInputStream(InputStream inputStream) throws IOException, JSONException {
        loadJson(new JSONObject(FileUtils.convertStreamToString(inputStream)));
    }

    public void loadFile(File file) throws IOException, JSONException {
        this.source = file;
        loadInputStream(new FileInputStream(file));
    }

    public String getPackName() {
        return this.packName;
    }

    public String getDisplayedName() {
        return (this.displayedName == null || this.displayedName.length() == 0) ? this.packName : this.displayedName;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public String getDescription() {
        return this.description;
    }

    public String getAuthor() {
        return this.author;
    }

    public List<DeclaredDirectory> getDeclaredDirectories() {
        return this.declaredDirectories;
    }

    static ModPackDirectory lambda$createDeclaredDirectoriesForModPack$0(ModPack pack, DeclaredDirectory declaredDirectory) {
        return new ModPackDirectory(ModPackDirectory.DirectoryType.CUSTOM, new File(pack.getRootDirectory(), declaredDirectory.path), declaredDirectory.path.trim(), DeclaredDirectoryType.createDirectoryRequestStrategy(declaredDirectory.type), DeclaredDirectoryType.createDirectoryUpdateStrategy(declaredDirectory.type), new AllFilesDirectoryExtractStrategy());
    }

    public List<ModPackDirectory> createDeclaredDirectoriesForModPack(final ModPack pack) {
        return Java8BackComp.stream(this.declaredDirectories).map(new Function<DeclaredDirectory, ModPackDirectory>() {
            @Override
            public final ModPackDirectory apply(DeclaredDirectory obj) {
                return ModPackManifest.lambda$createDeclaredDirectoriesForModPack$0(pack, obj);
            }
        }).collect(Collectors.toList());
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public void setDisplayedName(String displayedName) {
        this.displayedName = displayedName;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ModPackManifestEditor edit() throws IOException, JSONException {
        return new ModPackManifestEditor(this, this.source);
    }
}
