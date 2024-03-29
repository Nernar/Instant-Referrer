package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.ScriptableObject;

public class Config {
    private JSONObject data;
    private File file;
    private Config parent;

    public Config(File file) {
        this.file = file;
        try {
            this.data = FileTools.readJSON(file.getAbsolutePath());
        } catch (IOException | JSONException e) {
            this.data = new JSONObject();
        }
    }

    public Config(String string) {
        this(new File(string));
    }

    private Config(Config parent, JSONObject json) {
        this.parent = parent;
        this.data = json;
    }

    public void reload() {
        try {
            this.data = FileTools.readJSON(this.file.getAbsolutePath());
        } catch (IOException | JSONException e) {
            ICLog.e("CONFIG", "cannot reload config " + getPath(), e);
        }
    }

    public String getPath() {
        return this.file.getAbsolutePath();
    }

    public void save() {
        if (this.parent != null) {
            this.parent.save();
            return;
        }
        try {
            if (this.data == null) {
                this.data = new JSONObject();
            }
            FileTools.writeJSON(this.file.getAbsolutePath(), this.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getNames() {
        ArrayList<String> nameArr = new ArrayList<>();
        JSONArray names = this.data.names();
        if (names != null) {
            for (int i = 0; i < names.length(); i++) {
                nameArr.add(names.optString(i));
            }
        }
        return nameArr;
    }

    public Object get(String name) {
        String firstName;
        int dotIndex = name.indexOf(46);
        String lastName = null;
        if (dotIndex != -1) {
            firstName = name.substring(0, dotIndex);
            lastName = name.substring(dotIndex + 1);
        } else {
            firstName = name;
        }
        Object val = this.data.opt(firstName);
        if (val == null) {
            return null;
        }
        if (val instanceof JSONObject) {
            Config cfg = new Config(this, (JSONObject) val);
            if (lastName != null) {
                return cfg.get(lastName);
            }
            return cfg;
        } else if (lastName != null) {
            return null;
        } else {
            return val;
        }
    }

    public Object access(String name) {
        return get(name);
    }

    public boolean getBool(String name) {
        Object val = get(name);
        if (val instanceof Boolean) {
            return ((Boolean) val).booleanValue();
        }
        return false;
    }

    public Number getNumber(String name) {
        Object val = get(name);
        if (val instanceof Number) {
            return (Number) val;
        }
        return 0;
    }

    public int getInteger(String name) {
        return getNumber(name).intValue();
    }

    public float getFloat(String name) {
        return getNumber(name).floatValue();
    }

    public double getDouble(String name) {
        return getNumber(name).doubleValue();
    }

    public String getString(String name) {
        Object val = get(name);
        if (val instanceof CharSequence) {
            return new StringBuilder().append(val).toString();
        }
        return null;
    }

    public boolean set(String name, Object val) {
        String firstName;
        int dotIndex = name.indexOf(46);
        String lastName = null;
        if (dotIndex != -1) {
            firstName = name.substring(0, dotIndex);
            lastName = name.substring(dotIndex + 1);
        } else {
            firstName = name;
        }
        if (lastName == null) {
            try {
                this.data.put(firstName, val);
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        Object unit = this.data.opt(firstName);
        if (unit == null || !(unit instanceof JSONObject)) {
            return false;
        }
        return new Config(this, (JSONObject) unit).set(lastName, val);
    }

    public ConfigValue getValue(String path) {
        ConfigValue val = new ConfigValue(this, path, null);
        if (val.isValid) {
            return val;
        }
        return null;
    }

    public void checkAndRestore(String str) throws JSONException {
        checkAndRestore(new JSONObject(str));
    }

    public void checkAndRestore(ScriptableObject obj) {
        try {
            checkAndRestore(NativeJSON.stringify(Context.enter(), ScriptableObjectHelper.getDefaultScope(), obj, null, null).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void checkAndRestore(JSONObject json) {
        try {
            this.data = checkAndRestoreRecursive(json, this.data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        save();
    }

    private JSONObject checkAndRestoreRecursive(JSONObject json, JSONObject cur) throws JSONException {
        if (cur == null) {
            cur = new JSONObject();
        }
        JSONArray names = json.names();
        int len = names.length();
        for (int i = 0; i < len; i++) {
            String key = names.optString(i);
            if (key != null) {
                Object defVal = json.opt(key);
                Object curVal = cur.opt(key);
                if (defVal != null) {
                    if (defVal instanceof JSONObject) {
                        cur.put(key, checkAndRestoreRecursive((JSONObject) defVal, cur.optJSONObject(key)));
                    } else if (curVal != null) {
                        if (curVal.getClass() != defVal.getClass()) {
                            cur.put(key, defVal);
                        }
                    } else {
                        cur.put(key, defVal);
                    }
                }
            }
        }
        return cur;
    }

    public static class ConfigValue {
        private boolean isValid;
        private Config parent;
        private String path;

        private ConfigValue(Config parent, String path) {
            this.parent = parent;
            this.path = path;
            this.isValid = parent.get(path) != null;
        }

        ConfigValue(Config config, String str, ConfigValue configValue) {
            this(config, str);
        }

        public void set(Object val) {
            if (!this.isValid) {
                return;
            }
            this.parent.set(this.path, val);
            this.parent.save();
        }

        public Object get() {
            if (!this.isValid) {
                return null;
            }
            return this.parent.get(this.path);
        }

        public String toString() {
            return "[ConfigValue name=" + this.path + "]";
        }
    }
}
