package com.zhekasmirnov.innercore.api.mod.util;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.mod.build.Config;
import java.util.ArrayList;
import java.util.Iterator;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ConfigVisualizer {
    private static int uniqueIndex = 0;
    private Config config;
    private float currentYOffset;
    private String prefix;

    public ConfigVisualizer(Config config, String prefix) {
        this.currentYOffset = 0.0f;
        this.config = config;
        this.prefix = prefix;
    }

    public ConfigVisualizer(Config config) {
        this(config, "config_vis");
    }

    private String getElementName() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.prefix);
        sb.append("_");
        int i = uniqueIndex;
        uniqueIndex = i + 1;
        sb.append(i);
        return sb.toString();
    }

    private void visualizeName(float x, float y, float z, ScriptableObject elements, String name) {
        ScriptableObject nameElement = ScriptableObjectHelper.createEmpty();
        nameElement.put("type", nameElement, "text");
        nameElement.put("x", nameElement, Float.valueOf(x));
        nameElement.put("y", nameElement, Double.valueOf(y + 22.5d));
        nameElement.put("z", nameElement, Float.valueOf(z));
        nameElement.put("text", nameElement, name);
        elements.put(getElementName(), elements, nameElement);
    }

    private void visualizeElement(float x, float y, float z, ScriptableObject elements, Config unit, String name) {
        visualizeName(x, y, z, elements, name);
        Object val = unit.get(name);
        if (val instanceof Boolean) {
            ScriptableObject valElement = ScriptableObjectHelper.createEmpty();
            valElement.put("type", valElement, "switch");
            valElement.put("x", (Scriptable) valElement, (Object) 860);
            valElement.put("y", valElement, Double.valueOf(y + 4.5d));
            valElement.put("z", valElement, Float.valueOf(z));
            valElement.put("state", valElement, val);
            valElement.put("configValue", valElement, unit.getValue(name));
            elements.put(getElementName(), elements, valElement);
        } else {
            String text = new StringBuilder().append(val).toString();
            if (val instanceof CharSequence) {
                text = "\"" + text + "\"";
            }
            ScriptableObject valElement2 = ScriptableObjectHelper.createEmpty();
            valElement2.put("type", valElement2, "text");
            valElement2.put("x", (Scriptable) valElement2, (Object) 960);
            valElement2.put("y", valElement2, Double.valueOf(y + 22.5d));
            valElement2.put("z", valElement2, Float.valueOf(z));
            valElement2.put("text", valElement2, text);
            elements.put(getElementName(), elements, valElement2);
        }
        ScriptableObject lineElement = ScriptableObjectHelper.createEmpty();
        lineElement.put("type", lineElement, "image");
        lineElement.put("bitmap", lineElement, "default_horizontal_line_template");
        lineElement.put("y", lineElement, Double.valueOf(y + 32.5d));
        lineElement.put("z", lineElement, Float.valueOf(z));
        lineElement.put("height", (Scriptable) lineElement, (Object) 10);
        elements.put(getElementName(), elements, lineElement);
    }

    public void clearVisualContent(ScriptableObject elements) {
        Object[] keys = elements.getAllIds();
        for (Object key : keys) {
            if (new StringBuilder().append(key).toString().contains(String.valueOf(this.prefix) + "_")) {
                elements.put(new StringBuilder().append(key).toString(), elements, (Object) null);
            }
        }
    }

    public void createVisualContent(ScriptableObject elements, ScriptableObject prefs) {
        if (prefs == null) {
            prefs = ScriptableObjectHelper.createEmpty();
        }
        float offX = ScriptableObjectHelper.getFloatProperty(prefs, "x", 0.0f);
        float offY = ScriptableObjectHelper.getFloatProperty(prefs, "y", 0.0f);
        float offZ = ScriptableObjectHelper.getFloatProperty(prefs, "z", 0.0f);
        this.currentYOffset = 0.0f;
        createVisualContent(elements, this.config, offX, offY, offZ);
    }

    private void createVisualContent(ScriptableObject elements, Config cfg, float x, float y, float z) {
        ArrayList<String> names = cfg.getNames();
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String name = it.next();
            Object val = cfg.get(name);
            if (!(val instanceof Config)) {
                visualizeElement(x, y + this.currentYOffset, z, elements, cfg, name);
                this.currentYOffset += 75.0f;
            } else {
                visualizeName(x, y + this.currentYOffset, z, elements, String.valueOf(name) + ": ");
                this.currentYOffset += 75.0f;
                createVisualContent(elements, (Config) val, x + 75.0f, y, z);
            }
        }
    }
}
