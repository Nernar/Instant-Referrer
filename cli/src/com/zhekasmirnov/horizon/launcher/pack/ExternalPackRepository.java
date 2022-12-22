package com.zhekasmirnov.horizon.launcher.pack;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JsonIterator;
import com.zhekasmirnov.horizon.util.LocaleUtils;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExternalPackRepository extends PackRepository {
    private final String manifestUrl;
    private final List<String> uuids = new ArrayList<>();
    private final List<String> suggestions = new ArrayList<>();
    private final HashMap<String, IPackLocation> locationMap = new HashMap<>();

    public ExternalPackRepository(String str) {
        this.manifestUrl = str;
    }

    @Override
    public synchronized void fetch() {
        try {
            JSONObject jSONObject = new JSONObject(FileUtils.convertStreamToString(new URL(this.manifestUrl).openStream()));
            JSONArray jSONArray = jSONObject.getJSONArray("packs");
            this.locationMap.clear();
            this.uuids.clear();
            if (jSONArray != null) {
                Iterator<Object> it = new JsonIterator<>(jSONArray).iterator();
                while (it.hasNext()) {
                    JSONObject jSONObject2 = (JSONObject) it.next();
                    String optString = jSONObject2.optString("uuid");
                    String optString2 = jSONObject2.optString("package");
                    String optString3 = jSONObject2.optString("manifest");
                    String optString4 = jSONObject2.optString("graphics");
                    String resolveLocaleJsonProperty = LocaleUtils.resolveLocaleJsonProperty(jSONObject2, "changelog");
                    if (optString != null && optString2 != null && optString3 != null) {
                        ExternalPackLocation externalPackLocation = new ExternalPackLocation(optString2, optString3, optString4);
                        externalPackLocation.setUUID(optString);
                        externalPackLocation.setChangelogUrl(resolveLocaleJsonProperty);
                        this.locationMap.put(optString, externalPackLocation);
                        this.uuids.add(optString);
                        this.suggestions.add(optString);
                    } else {
                        Logger.error("ExternalPackRepository", "failed to read pack description json: " + jSONObject2);
                    }
                }
            }
            this.suggestions.clear();
            JSONArray optJSONArray = jSONObject.optJSONArray("suggestions");
            if (optJSONArray != null) {
                Iterator<Object> it2 = new JsonIterator<>(optJSONArray).iterator();
                while (it2.hasNext()) {
                    String str = (String) it2.next();
                    if (str != null) {
                        this.suggestions.add(str);
                    }
                }
            }
        } catch (IOException e) {
            Logger.error("ExternalPackRepository", "failed to read external packs manifest from " + this.manifestUrl + " error: " + e);
        } catch (JSONException e2) {
            Logger.error("ExternalPackRepository", "failed to read external packs manifest json from " + this.manifestUrl + " error:  " + e2);
        }
    }

    @Override
    public List<String> getAllPacksUUIDs() {
        return this.uuids;
    }

    @Override
    public IPackLocation getLocationForUUID(String str) {
        return this.locationMap.get(str);
    }

    @Override
    public List<String> getPackSuggestions() {
        return this.suggestions;
    }
}
