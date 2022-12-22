package com.zhekasmirnov.horizon.util;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class JSONUtils {
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(JSONArray jSONArray) {
        ArrayList<T> arrayList = new ArrayList<>();
        for (int i = 0; i < jSONArray.length(); i++) {
            arrayList.add((T) jSONArray.opt(i));
        }
        return arrayList;
    }
}
