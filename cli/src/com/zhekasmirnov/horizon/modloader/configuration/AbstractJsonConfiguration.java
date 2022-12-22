package com.zhekasmirnov.horizon.modloader.configuration;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonConfiguration extends Configuration {
    protected abstract JSONObject getData();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String str, Class<T> cls) {
        try {
            return (T) get(getData(), str);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public Object get(String str) {
        return get(getData(), str);
    }

    private Object get(JSONObject jSONObject, String str) {
        int indexOf = str.indexOf(46);
        if (indexOf != -1) {
            Object opt = jSONObject.opt(str.substring(0, indexOf));
            if (opt instanceof JSONObject) {
                return get((JSONObject) opt, str.substring(indexOf + 1));
            }
            return null;
        }
        return jSONObject.opt(str);
    }

    private boolean set(JSONObject jSONObject, String str, Object obj) {
        if (jSONObject != null && !isReadOnly()) {
            int indexOf = str.indexOf(46);
            if (indexOf != -1) {
                String substring = str.substring(0, indexOf);
                Object opt = jSONObject.opt(substring);
                if (opt == null) {
                    opt = new JSONObject();
                    try {
                        jSONObject.put(substring, opt);
                    } catch (JSONException e) {
                    }
                }
                if (opt instanceof JSONObject) {
                    return set((JSONObject) opt, str.substring(indexOf + 1), obj);
                }
                return false;
            }
            try {
                jSONObject.put(str, obj);
                return true;
            } catch (JSONException e2) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean set(String str, Object obj) {
        return set(getData(), str, obj);
    }

    private Object delete(JSONObject jSONObject, String str) {
        if (jSONObject == null || isReadOnly()) {
            return null;
        }
        int indexOf = str.indexOf(46);
        if (indexOf != -1) {
            Object opt = jSONObject.opt(str.substring(0, indexOf));
            if (opt instanceof JSONObject) {
                return delete((JSONObject) opt, str.substring(indexOf + 1));
            }
            return null;
        }
        return jSONObject.remove(str);
    }

    @Override
    public boolean isContainer(String str) {
        return get(getData(), str) instanceof JSONObject;
    }

    @Override
    public Object delete(String str) {
        return delete(getData(), str);
    }

    @Override
    public Configuration getChild(String str) {
        Object obj = get(getData(), str);
        if (obj instanceof JSONObject) {
            return new ChildJsonConfiguration(this, (JSONObject) obj, null);
        }
        return null;
    }

    private static class ChildJsonConfiguration extends AbstractJsonConfiguration {
        final JSONObject data;
        final boolean isReadOnly;
        final AbstractJsonConfiguration parent;

        private ChildJsonConfiguration(AbstractJsonConfiguration abstractJsonConfiguration, JSONObject jSONObject) {
            this.parent = abstractJsonConfiguration;
            this.data = jSONObject;
            this.isReadOnly = abstractJsonConfiguration.isReadOnly();
        }

        ChildJsonConfiguration(AbstractJsonConfiguration abstractJsonConfiguration, JSONObject jSONObject, ChildJsonConfiguration childJsonConfiguration) {
            this(abstractJsonConfiguration, jSONObject);
        }

        @Override
        protected JSONObject getData() {
            return this.data;
        }

        @Override
        public void refresh() {
            this.parent.refresh();
        }

        @Override
        public boolean isReadOnly() {
            return this.isReadOnly;
        }

        @Override
        public void save() {
            this.parent.save();
        }

        @Override
        public void load() {
            this.parent.load();
        }
    }
}
