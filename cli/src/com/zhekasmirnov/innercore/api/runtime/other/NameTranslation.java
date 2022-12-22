package com.zhekasmirnov.innercore.api.runtime.other;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

public class NameTranslation {
    private static HashMap<String, HashMap<Integer, String>> allLanguageTranslations = new HashMap<>();
    private static HashMap<Integer, String> currentLanguageTranslations = new HashMap<>();
    private static HashMap<Integer, String> defaultLanguageTranslations = new HashMap<>();
    private static String language = "en";
    private static final HashMap<Integer, String> namesToGenerateCache = new HashMap<>();

    private static HashMap<Integer, String> getTranslationMap(String language2) {
        if (allLanguageTranslations.containsKey(language2)) {
            return allLanguageTranslations.get(language2);
        }
        HashMap<Integer, String> map = new HashMap<>();
        allLanguageTranslations.put(language2, map);
        return map;
    }

    private static String toShortName(String lang) {
        int index = lang.indexOf(95);
        if (index == -1) {
            return lang;
        }
        return lang.substring(0, index);
    }

    public static void loadBuiltinTranslations() {
        try {
            JSONObject translations = FileUtils.readJSONFromAssets(null, "innercore/builtin_translations.json");
            Iterator<String> it = translations.keys();
            while (it.hasNext()) {
                String name = it.next();
                JSONObject languages = translations.getJSONObject(name);
                Iterator<String> it2 = languages.keys();
                while (it2.hasNext()) {
                    String lang = it2.next();
                    String translation = languages.getString(lang);
                    addSingleTranslation(lang, name, translation);
                }
            }
        } catch (IOException | JSONException e) {
            ICLog.e("TRANSLATION", "failed to load builtin translations", e);
        }
    }

    public static void setLanguage(String lang) {
        language = toShortName(lang);
        currentLanguageTranslations = getTranslationMap(language);
        defaultLanguageTranslations = getTranslationMap("en");
        ICLog.d("TRANSLATION", "set game language to " + language + " (full name is " + lang + ")");
    }

    public static String getLanguage() {
        return language;
    }

    public static void addSingleTranslation(String lang, String origin, String translation) {
        HashMap<Integer, String> map = getTranslationMap(toShortName(lang));
        map.put(Integer.valueOf(origin.hashCode()), translation);
    }

    public static void addTranslation(String origin, HashMap<String, String> translations) {
        Set<String> langs = translations.keySet();
        for (String lang : langs) {
            addSingleTranslation(lang, origin, translations.get(lang));
        }
    }

    public static void addTranslation(String origin, ScriptableObject translations) {
        Object[] keys = translations.getAllIds();
        HashMap<String, String> map = new HashMap<>();
        for (Object key : keys) {
            if (key instanceof String) {
                map.put((String) key, new StringBuilder().append(translations.get(key)).toString());
            }
        }
        addTranslation(origin, map);
    }

    public static String translate(String str) {
        if (str == null) {
            return null;
        }
        String result = currentLanguageTranslations.get(Integer.valueOf(str.hashCode()));
        if (result != null) {
            return result;
        }
        String result2 = defaultLanguageTranslations.get(Integer.valueOf(str.hashCode()));
        if (result2 != null) {
            return result2;
        }
        return str;
    }

    public static void refresh(boolean sendOverrideCache) {
        File file = new File(String.valueOf(FileTools.DIR_MINECRAFT) + "minecraftpe/", "options.txt");
        try {
            String content = FileTools.readFileText(file.getAbsolutePath());
            String[] lines = content.split("\n");
            for (String line : lines) {
                String[] opts = line.split(":");
                if (opts[0].equals("game_language")) {
                    if (opts.length == 2) {
                        setLanguage(opts[1]);
                        return;
                    } else {
                        refreshFromNative();
                        return;
                    }
                }
            }
            refreshFromNative();
        } catch (Throwable th) {
            refreshFromNative();
        }
    }

    private static void refreshFromNative() {
        ICLog.d("TRANSLATION", "failed to get language settings");
        setLanguage("en_US");
    }

    public static void sendNameToGenerateCache(int id, int data, String name) {
        synchronized (namesToGenerateCache) {
            namesToGenerateCache.put(Integer.valueOf((id * 16) + Math.min(15, Math.max(0, data))), name);
        }
    }

    public static boolean isAscii(String str) {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(str);
    }

    public static String fixUnicodeIfRequired(String nameId, String name) {
        if (name.length() == 0) {
            return "error.blank_name";
        }
        if (isAscii(name)) {
            return name;
        }
        String alias = String.valueOf(nameId) + ".name";
        addSingleTranslation("en", alias, name);
        return alias;
    }
}
