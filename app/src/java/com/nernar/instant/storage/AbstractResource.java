package com.nernar.instant.storage;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import java.io.File;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public abstract class AbstractResource extends JSONObject implements Resource {
	
    public AbstractResource() {
		super();
	}
	
    public AbstractResource(Map copyFrom) {
		super(copyFrom);
	}
	
    public AbstractResource(JSONTokener readFrom) throws JSONException {
		super(readFrom);
	}
	
    public AbstractResource(String json) throws JSONException {
		super(json);
	}
	
    public AbstractResource(JSONObject copyFrom, String[] names) throws JSONException {
		super(copyFrom, names);
	}
	
	@Override
	public abstract String getId();
	
	@Override
	public String getKey() {
		String id = getId();
		if (id == null) return null;
		int index = id.lastIndexOf(":");
		if (index == -1) return id;
		return id.substring(index + 1);
	}
	
	@Override
	public File getOutput() {
		return null;
	}
	
	public JSONObject put(Resource value) {
		try {
			return put(value.getKey(), value);
		} catch (NullPointerException e) {
			Logger.debug("AbstractResource", "Passed empty resource instance");
			return this;
		}
	}
	
	@Override
	public JSONObject put(String name, boolean value) {
		try {
			return super.put(name, value);
		} catch (JSONException e) {
			Logger.debug("AbstractResource", name + " already accumulated");
			return this;
		}
	}
	
	@Override
	public JSONObject put(String name, double value) {
		try {
			return super.put(name, value);
		} catch (JSONException e) {
			Logger.debug("AbstractResource", name + " already accumulated");
			return this;
		}
	}
	
	@Override
	public JSONObject put(String name, int value) {
		try {
			return super.put(name, value);
		} catch (JSONException e) {
			Logger.debug("AbstractResource", name + " already accumulated");
			return this;
		}
	}
	
	@Override
	public JSONObject put(String name, long value) {
		try {
			return super.put(name, value);
		} catch (JSONException e) {
			Logger.debug("AbstractResource", name + " already accumulated");
			return this;
		}
	}
	
	@Override
	public JSONObject put(String name, Object value) {
		try {
			return super.put(name, value);
		} catch (JSONException e) {
			Logger.debug("AbstractResource", name + " already accumulated");
			return this;
		}
	}
	
	@Override
	public JSONObject putOpt(String name, Object value) {
		try {
			return super.putOpt(name, value);
		} catch (JSONException e) {
			Logger.debug("AbstractResource", name + " already accumulated");
			return this;
		}
	}
	
	@Override
	public String toString() {
		return getId() + super.toString();
	}
}
