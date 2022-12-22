package com.zhekasmirnov.apparatus.minecraft.addon.recipe;

import android.util.Pair;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import java.util.List;
import org.json.JSONObject;

public abstract class AddonRecipeParser {
    public abstract List<ParsedRecipe> parse(JSONObject jSONObject);

    public static class ParsedRecipe {
        private final JSONObject contents;
        private final String identifier;
        private final List<String> tags;
        private final String type;

        public ParsedRecipe(String identifier, String type, List<String> tags, JSONObject contents) {
            this.identifier = identifier;
            this.type = type;
            this.tags = tags;
            this.contents = contents;
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public JSONObject getContents() {
            return this.contents;
        }

        public List<String> getTags() {
            return this.tags;
        }

        public String getType() {
            return this.type;
        }
    }

    public void prepare() {
    }

    public Pair<Integer, Integer> getIdAndDataFromItemString(String stringId, int defaultData) {
        String[] result = stringId.split(":");
        String name = result.length == 1 ? result[0] : result[1];
        int id = AdaptedScriptAPI.IDRegistry.getIDByName(name.toLowerCase());
        int data = defaultData;
        if (result.length == 3) {
            data = Integer.parseInt(result[2]);
        }
        if (id != 0) {
            return new Pair<>(Integer.valueOf(id), Integer.valueOf(data));
        }
        return null;
    }

    public Pair<Integer, Integer> getIdAndDataForItemJson(JSONObject json, int defaultData) {
        Pair<Integer, Integer> fromStringId;
        String stringId = json.optString("item");
        int data = defaultData;
        int id = 0;
        int dataFromJson = json.optInt("data", -1);
        if (dataFromJson != -1) {
            data = dataFromJson;
        }
        if (stringId != null && (fromStringId = getIdAndDataFromItemString(stringId, data)) != null) {
            id = fromStringId.first.intValue();
            data = fromStringId.second.intValue();
        }
        if (id == 0) {
            return null;
        }
        return new Pair<>(Integer.valueOf(id), Integer.valueOf(data));
    }
}
