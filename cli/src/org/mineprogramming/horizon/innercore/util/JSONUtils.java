package org.mineprogramming.horizon.innercore.util;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONUtils {
    public static Iterator<JSONObject> getJsonIterator(final JSONArray array) {
        return new Iterator<JSONObject>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < array.length();
            }

            @Override
            public JSONObject next() {
                JSONArray jSONArray = array;
                int i = this.i;
                this.i = i + 1;
                return jSONArray.optJSONObject(i);
            }
        };
    }

    public static Iterator<JSONObject> getJsonIterator(final JSONObject obejct) {
        final JSONArray names = obejct.names();
        return new Iterator<JSONObject>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < names.length();
            }

            @Override
            public JSONObject next() {
                JSONArray jSONArray = names;
                int i = this.i;
                this.i = i + 1;
                String name = jSONArray.optString(i);
                return obejct.optJSONObject(name);
            }
        };
    }
}
