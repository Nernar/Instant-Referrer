package com.zhekasmirnov.horizon.util;

import java.util.Iterator;
import java.util.Locale;
import org.json.JSONObject;

public class LocaleUtils {
    public static final String LANG_EN = "en";
    public static final String LANG_RU = "ru";
    private static String defaultLanguage;

    public static Locale getLocale(Object resources) {
        return Locale.getDefault();
    }

    public static void updateDefaultLanguage(Object resources) {
        Locale locale = getLocale(resources);
        if (locale != null) {
            defaultLanguage = locale.getLanguage();
        }
    }

    public static String getLanguage(Object resources, String str) {
        if (resources != null) {
            Locale locale = getLocale(resources);
            return locale != null ? locale.getLanguage() : str;
        }
        String str2 = defaultLanguage;
        return str2 != null ? str2 : str;
    }

    public static String getLanguage(Object resources) {
        return getLanguage(resources, "en");
    }

    public static String getLanguageTag(Object resources) {
        if (resources != null) {
            Locale locale = getLocale(resources);
            return locale != null ? locale.toString().replace('_', '-') : "en";
        }
        String str = defaultLanguage;
        return str != null ? str : "en";
    }

    public static String resolveLocaleJsonProperty(Object resources, JSONObject jSONObject, String str) {
        Object opt = jSONObject.opt(str);
        if (opt instanceof JSONObject) {
            JSONObject jSONObject2 = (JSONObject) opt;
            String optString = jSONObject2.optString(getLanguage(resources));
            if (optString == null) {
                String optString2 = jSONObject2.optString("en");
                optString = optString2;
                if (optString2 == null) {
                    Iterator<Object> it = new JsonIterator<>(jSONObject2).iterator();
                    while (it.hasNext()) {
                        String optString3 = jSONObject2.optString((String) it.next());
                        optString = optString3;
                        if (optString3 != null) {
                            break;
                        }
                    }
                }
            }
            return optString;
        }
        return jSONObject.optString(str);
    }

    public static String resolveLocaleJsonProperty(JSONObject jSONObject, String str) {
        return resolveLocaleJsonProperty(null, jSONObject, str);
    }
}
