package com.zhekasmirnov.innercore.api.runtime.saver;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JsonHelper {
    private static HashMap<Integer, ScriptableObject> scriptableByHashCode = new HashMap<>();

    private static class JsonToString {
        private static DecimalFormat format = new DecimalFormat("#");
        private boolean beautify;
        private int depth;
        private ArrayList<Boolean> isArrayStack = new ArrayList<>();
        private StringBuilder result = new StringBuilder();

        public JsonToString(boolean beautify) {
            this.beautify = false;
            this.depth = 0;
            this.beautify = beautify;
            this.depth = 0;
        }

        private boolean isArray() {
            if (this.isArrayStack.size() > 0) {
                return this.isArrayStack.get(0).booleanValue();
            }
            return false;
        }

        private void push(boolean isArray) {
            this.isArrayStack.add(0, Boolean.valueOf(isArray));
            this.depth++;
        }

        private void pop() {
            if (this.isArrayStack.size() == 0 || this.depth < 1) {
                throw new IllegalArgumentException("excess object or array end");
            }
            this.depth--;
            this.isArrayStack.remove(0);
        }

        private String getIntend() {
            String intend = "";
            if (!this.beautify) {
                return "";
            }
            for (int i = 0; i < this.depth; i++) {
                intend = String.valueOf(intend) + "  ";
            }
            return intend;
        }

        private void putCommaIfNeeded() {
            char last;
            char c;
            int index = this.result.length();
            while (true) {
                index--;
                if (index == -1 || ((c = this.result.charAt(index)) != ' ' && c != '\n')) {
                    break;
                }
            }
            if (index != -1 && (last = this.result.charAt(index)) != ',' && last != '{' && last != '[') {
                this.result.append(",");
            }
        }

        public void begin(boolean isArray) {
            StringBuilder sb = this.result;
            sb.append(isArray ? "[" : "{");
            sb.append(this.beautify ? "\n" : "");
            push(isArray);
        }

        public void end() {
            String str;
            boolean isArr = isArray();
            pop();
            StringBuilder sb = this.result;
            if (this.beautify) {
                str = "\n" + getIntend();
            } else {
                str = "";
            }
            sb.append(str);
            sb.append(isArr ? "]" : "}");
        }

        public void key(Object key) {
            putCommaIfNeeded();
            if (this.beautify && this.result.charAt(this.result.length() - 1) != '\n') {
                this.result.append("\n");
            }
            if (this.beautify) {
                this.result.append(getIntend());
            }
            if (!isArray()) {
                StringBuilder sb = this.result;
                sb.append("\"");
                sb.append(key);
                sb.append("\":");
            }
        }

        public void value(Object val) {
            String str;
            if (val == null) {
                throw new IllegalArgumentException("value cannot be null");
            }
            if (val instanceof CharSequence) {
                String str2 = val.toString();
                str = "\"" + str2.replace("\n", "\\n").replace("\"", "\\\"") + "\"";
            } else if ((val instanceof Number) && ((Number) val).doubleValue() == ((Number) val).longValue()) {
                str = format.format(val);
            } else {
                str = val.toString();
            }
            this.result.append(str);
        }

        public String getResult() {
            return this.result.toString();
        }
    }

    public static synchronized String scriptableToJsonString(ScriptableObject scriptableObject, boolean beautify) {
        if (scriptableObject == null) {
            return "{}";
        }
        scriptableByHashCode.clear();
        JsonToString jsonToString = new JsonToString(beautify);
        stringify(jsonToString, scriptableObject);
        return jsonToString.getResult();
    }

    private static void stringify(JsonToString jsonToString, ScriptableObject scriptableObject) {
        Object val;
        Object val2;
        scriptableByHashCode.put(Integer.valueOf(scriptableObject.hashCode()), scriptableObject);
        boolean isArray = scriptableObject instanceof NativeArray;
        jsonToString.begin(isArray);
        Object[] keys = scriptableObject.getAllIds();
        for (Object key : keys) {
            if ((!isArray || !(key instanceof CharSequence)) && (val = scriptableObject.get(key)) != null) {
                Object val3 = ObjectSaverRegistry.unwrapIfNeeded(val);
                if (val3 instanceof ScriptableObject) {
                    if (!scriptableByHashCode.containsKey(Integer.valueOf(val3.hashCode())) && (val2 = ObjectSaverRegistry.saveObjectAndCheckSaveIgnoring(val3)) != null) {
                        jsonToString.key(key);
                        stringify(jsonToString, (ScriptableObject) val2);
                    }
                } else if ((val3 instanceof CharSequence) || (val3 instanceof Number) || (val3 instanceof Boolean)) {
                    jsonToString.key(key);
                    jsonToString.value(val3);
                } else {
                    ScriptableObject saveResult = ObjectSaverRegistry.saveObject(val3);
                    if (saveResult != null) {
                        jsonToString.key(key);
                        stringify(jsonToString, saveResult);
                    }
                }
            }
        }
        jsonToString.end();
    }

    public static Scriptable parseJsonString(String string) throws JSONException {
        JSONObject json = new JSONObject(string);
        return jsonToScriptable(json);
    }

    public static Scriptable jsonToScriptable(Object object) {
        ScriptableObject scriptable;
        int i = 0;
        if (object instanceof JSONArray) {
            JSONArray arr = (JSONArray) object;
            List<Object> list = new ArrayList<>();
            while (i < arr.length()) {
                Object val = arr.opt(i);
                if ((val instanceof JSONObject) || (val instanceof JSONArray)) {
                    val = jsonToScriptable(val);
                }
                list.add(val);
                i++;
            }
            scriptable = ScriptableObjectHelper.createArray(list);
        } else if (object instanceof JSONObject) {
            scriptable = ScriptableObjectHelper.createEmpty();
            JSONObject json = (JSONObject) object;
            JSONArray arr2 = json.names();
            if (arr2 != null) {
                while (i < arr2.length()) {
                    String key = arr2.optString(i);
                    Object val2 = json.opt(key);
                    if ((val2 instanceof JSONObject) || (val2 instanceof JSONArray)) {
                        val2 = jsonToScriptable(val2);
                    }
                    scriptable.put(key, scriptable, val2);
                    i++;
                }
            }
        } else {
            throw new IllegalArgumentException("FAILED ASSERTION: JsonHelper.jsonToScriptable can get only JSONObject or JSONArray");
        }
        return ObjectSaverRegistry.readObject(scriptable);
    }
}
