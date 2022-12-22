package com.zhekasmirnov.apparatus.mod;

import android.util.Pair;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class ContentIdScope {
    private final String name;
    private final Map<String, Pair<Integer, Boolean>> nameToId = new HashMap<>();
    private final Map<Integer, String> idToName = new HashMap<>();
    private int nextGeneratedId = 0;

    public ContentIdScope(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, Pair<Integer, Boolean>> nameAndId : this.nameToId.entrySet()) {
                json.put(nameAndId.getKey(), nameAndId.getValue().first);
            }
        } catch (JSONException e) {
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        if (json == null) {
            return;
        }
        Iterator<String> it = json.keys();
        while (it.hasNext()) {
            String key = it.next();
            int id = json.optInt(key);
            if (id != 0) {
                this.nameToId.put(key, new Pair<>(Integer.valueOf(id), false));
                this.idToName.put(Integer.valueOf(id), key);
            }
        }
    }

    private int generateNextId(int minValue, int maxValue) {
        this.nextGeneratedId = Math.max(minValue, this.nextGeneratedId);
        int i = minValue;
        while (this.idToName.containsKey(Integer.valueOf(this.nextGeneratedId))) {
            this.nextGeneratedId++;
            if (this.nextGeneratedId >= maxValue) {
                this.nextGeneratedId = minValue;
            }
            if (i < maxValue) {
                i++;
            } else {
                return 0;
            }
        }
        int i2 = this.nextGeneratedId;
        return i2;
    }

    public int getId(String nameId, boolean getUsedOnly) {
        Pair<Integer, Boolean> id = this.nameToId.get(nameId);
        if (id != null) {
            if (getUsedOnly && !id.second.booleanValue()) {
                return 0;
            }
            return id.first.intValue();
        }
        return 0;
    }

    public int getId(String nameId) {
        return getId(nameId, false);
    }

    public void removeId(String nameId) {
        int id = getId(nameId);
        if (id != 0) {
            this.nameToId.remove(nameId);
            this.idToName.remove(Integer.valueOf(id));
        }
    }

    public boolean setIdWasUsed(String nameId) {
        Pair<Integer, Boolean> id = this.nameToId.get(nameId);
        if (id != null) {
            this.nameToId.put(nameId, new Pair<>(id.first, true));
            return true;
        }
        return false;
    }

    public int getOrGenerateId(String nameId, int minValue, int maxValue, boolean isUsed) {
        int id = getId(nameId);
        if (id != 0) {
            if (id < minValue || id >= maxValue) {
                id = 0;
                removeId(nameId);
            } else if (isUsed) {
                setIdWasUsed(nameId);
            }
        }
        if (id == 0) {
            int generateNextId = generateNextId(minValue, maxValue);
            id = generateNextId;
            if (generateNextId != 0) {
                this.nameToId.put(nameId, new Pair<>(Integer.valueOf(id), Boolean.valueOf(isUsed)));
                this.idToName.put(Integer.valueOf(id), nameId);
            }
        }
        return id;
    }

    public boolean isNameIdUsed(String nameId) {
        Pair<Integer, Boolean> id = this.nameToId.get(nameId);
        return id != null && id.second.booleanValue();
    }

    public String getNameById(int id) {
        return this.idToName.get(Integer.valueOf(id));
    }
}
