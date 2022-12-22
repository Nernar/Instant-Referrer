package com.zhekasmirnov.apparatus.minecraft.addon.recipe;

import android.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class AddonRecipeParser16 extends AddonRecipeParser {
    private final Map<Integer, String> dyeConversion = new HashMap<>();

    public AddonRecipeParser16() {
        this.dyeConversion.put(0, "black_dye");
        this.dyeConversion.put(1, "red_dye");
        this.dyeConversion.put(2, "green_dye");
        this.dyeConversion.put(3, "cocoa_beans");
        this.dyeConversion.put(4, "lapis_lazuli");
        this.dyeConversion.put(5, "purple_dye");
        this.dyeConversion.put(6, "cyan_dye");
        this.dyeConversion.put(7, "light_gray_dye");
        this.dyeConversion.put(8, "gray_dye");
        this.dyeConversion.put(9, "pink_dye");
        this.dyeConversion.put(10, "lime_dye");
        this.dyeConversion.put(11, "yellow_dye");
        this.dyeConversion.put(12, "light_blue_dye");
        this.dyeConversion.put(13, "magenta_dye");
        this.dyeConversion.put(14, "orange_dye");
        this.dyeConversion.put(15, "bone_meal");
        this.dyeConversion.put(16, "black_dye");
        this.dyeConversion.put(17, "brown_dye");
        this.dyeConversion.put(18, "blue_dye");
        this.dyeConversion.put(19, "white_dye");
    }

    @Override
    public List<AddonRecipeParser.ParsedRecipe> parse(JSONObject recipeJson) {
        List<AddonRecipeParser.ParsedRecipe> result = new ArrayList<>();
        Iterator<String> it = recipeJson.keys();
        while (it.hasNext()) {
            String key = it.next();
            JSONObject contents = recipeJson.optJSONObject(key);
            if (contents != null) {
                List<String> tags = new ArrayList<>();
                String identifier = null;
                JSONObject description = contents.optJSONObject("description");
                if (description != null) {
                    identifier = description.optString("identifier");
                }
                JSONArray tagsJson = contents.optJSONArray("tags");
                if (tagsJson != null) {
                    for (int i = 0; i < tagsJson.length(); i++) {
                        tags.add(tagsJson.optString(i));
                    }
                }
                result.add(new AddonRecipeParser.ParsedRecipe(identifier, key, tags, contents));
            }
        }
        return result;
    }

    @Override
    public Pair<Integer, Integer> getIdAndDataFromItemString(String stringId, int defaultData) {
        if ("dye".equalsIgnoreCase(stringId) || "minecraft:dye".equalsIgnoreCase(stringId)) {
            stringId = this.dyeConversion.get(Integer.valueOf(defaultData));
            if (stringId == null) {
                return new Pair<>(0, 0);
            }
            defaultData = 0;
        }
        return super.getIdAndDataFromItemString(stringId, defaultData);
    }
}
