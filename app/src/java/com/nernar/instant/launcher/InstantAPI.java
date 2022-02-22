package com.nernar.instant.launcher;

import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.Executable;

public class InstantAPI extends API {
	
	@Override
	public int getLevel() {
		return 1;
	}
	
	@Override
	public String getName() {
		return "Instant";
	}
	
	@Override
	public void onCallback(String name, Object[] args) {}
	
	@Override
	public void onLoaded() {}
	
	@Override
	public void onModLoaded(Mod mod) {}
	
	@Override
	public void setupCallbacks(Executable executable) {}
}
