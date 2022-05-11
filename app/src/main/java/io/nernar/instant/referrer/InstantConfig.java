package io.nernar.instant.referrer;

import com.zhekasmirnov.innercore.mod.build.Config;
import java.io.File;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

public class InstantConfig {
	protected static final io.nernar.instant.storage.external.InstantConfig
		prototype = new io.nernar.instant.storage.external.InstantConfig();
	protected static final Config config = new Config(prototype.getOutput());
	
	static {
		checkAndRestore(prototype);
	}
	
	public static File getFile() {
		return prototype.getOutput();
	}
	
	public static Object get(String key) {
		return config.get(key);
	}
	
	public static Object get(String key, Object fallback) {
		Object value = get(key);
		return value == null ? fallback : value;
	}
	
	public static boolean get(String key, boolean fallback) {
		return get(key) == null ? fallback : config.getBool(key);
	}
	
	public static int get(String key, int fallback) {
		return get(key) == null ? fallback : config.getInteger(key);
	}
	
	public static float get(String key, float fallback) {
		return get(key) == null ? fallback : config.getFloat(key);
	}
	
	public static double get(String key, double fallback) {
		return get(key) == null ? fallback : config.getDouble(key);
	}
	
	public static Number get(String key, Number fallback) {
		return get(key) == null ? fallback : config.getNumber(key);
	}
	
	public static String get(String key, String fallback) {
		String value = config.getString(key);
		return value == null ? fallback : value;
	}
	
	public static Config.ConfigValue getValue(String key) {
		return config.getValue(key);
	}
	
	public static List<String> getNames() {
		return config.getNames();
	}
	
	public static boolean set(String key, Object value) {
		return config.set(key, value);
	}
	
	public static void setAndSave(String key, Object value) {
		if (set(key, value)) save();
	}
	
	public static void save() {
		config.save();
	}
	
	public static void checkAndRestore(JSONObject object) {
		config.checkAndRestore(object);
	}
	
	public static void checkAndRestore(ScriptableObject object) {
		config.checkAndRestore(object);
	}
	
	public static void checkAndRestore(String path) throws JSONException {
		config.checkAndRestore(path);
	}
}
