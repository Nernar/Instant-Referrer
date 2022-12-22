package com.zhekasmirnov.apparatus.mod;

import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import org.json.JSONException;
import org.json.JSONObject;

public class ContentIdSource {
    private static volatile ContentIdSource globalSingleton = null;
    private final File file;
    private final Map<String, ContentIdScope> scopes = new HashMap<>();

    public static ContentIdSource getGlobal() {
        ContentIdSource localInstance = globalSingleton;
        if (localInstance == null) {
            synchronized (ContentIdSource.class) {
                localInstance = globalSingleton;
                if (localInstance == null) {
                    ContentIdSource contentIdSource = new ContentIdSource(new File(FileTools.DIR_WORK, "mods/global-id-source.json"));
                    localInstance = contentIdSource;
                    globalSingleton = contentIdSource;
                    localInstance.read();
                }
            }
        }
        return localInstance;
    }

    public ContentIdSource(File file) {
        this.file = file;
    }

    public ContentIdScope getScope(String scopeName) {
        return this.scopes.get(scopeName);
    }

    static ContentIdScope lambda$getOrCreateScope$0(String scopeName, String key) {
        return new ContentIdScope(scopeName);
    }

    public ContentIdScope getOrCreateScope(final String scopeName) {
        return (ContentIdScope) Java8BackComp.computeIfAbsent(this.scopes, scopeName, new Function<String, ContentIdScope>() {
            @Override
            public final ContentIdScope apply(String obj) {
                return ContentIdSource.lambda$getOrCreateScope$0(scopeName, obj);
            }
        });
    }

    public void read() {
        if (this.file.isFile()) {
            try {
                JSONObject json = FileUtils.readJSON(this.file);
                Iterator<String> it = json.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    getOrCreateScope(key).fromJson(json.optJSONObject(key));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, ContentIdScope> keyAndScope : this.scopes.entrySet()) {
                json.put(keyAndScope.getKey(), keyAndScope.getValue().toJson());
            }
        } catch (JSONException e) {
        }
        try {
            FileUtils.writeJSON(this.file, json);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
