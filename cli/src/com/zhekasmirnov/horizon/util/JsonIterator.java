package com.zhekasmirnov.horizon.util;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonIterator<T> implements Iterable<T> {
    private final JSONArray array;

    public JsonIterator(JSONArray jSONArray) {
        if (jSONArray != null) {
            this.array = jSONArray;
        } else {
            this.array = new JSONArray();
        }
    }

    public JsonIterator(JSONObject jSONObject) {
        this(jSONObject != null ? jSONObject.names() : null);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < JsonIterator.this.array.length();
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                try {
                    JSONArray jSONArray = JsonIterator.this.array;
                    int i = this.index;
                    this.index = i + 1;
                    return (T) jSONArray.opt(i);
                } catch (ClassCastException e) {
                    return null;
                }
            }
        };
    }

    public static JsonIterator<Object> iterate(JSONArray jSONArray) {
        return new JsonIterator<>(jSONArray);
    }

    public static JsonIterator<String> iterate(JSONObject jSONObject) {
        return new JsonIterator<>(jSONObject);
    }
}
