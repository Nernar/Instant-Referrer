package com.zhekasmirnov.horizon;

import android.content.res.AssetManager;
import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.compiler.packages.RepoConstants;
import com.zhekasmirnov.horizon.launcher.env.ClassLoaderPatch;
import com.zhekasmirnov.horizon.runtime.logger.CoreLogger;
import com.zhekasmirnov.horizon.runtime.logger.LogFileHandler;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.runtime.logger.Profiler;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;

public class HorizonLibrary {
	private static void builtinInitialize() {}
	public static void include() {}
	private static void nativeLinkSubstrate(String str) {}
	public static void nativeRunCoreTests() {}
	
	static {
		unpackAssets(false);
		initializeLog();
	}
	
	private static void loadLibrary(String str) {
		System.loadLibrary(str);
		CoreLogger.log("HorizonLibrary", "loaded shared object: " + str);
	}
	
	private static void unpackAssets(boolean z) {
		AssetManager assets = HorizonApplication.getInstance().getAssets();
		File applicationLibraryDirectory = Environment.getApplicationLibraryDirectory();
		boolean z2 = true;
		if (!z) {
			try {
				long currentTimeMillis = System.currentTimeMillis() - Long.valueOf(FileUtils.readFileText(new File(applicationLibraryDirectory, ".timestamp")).trim()).longValue();
				if (currentTimeMillis >= 0 && currentTimeMillis < 5000) {
					try {
						CoreLogger.log("HorizonLibrary", "recently initialized, unpack skipped");
					} catch (NullPointerException | NumberFormatException any) {}
					z2 = false;
				}
			} catch (IOException | NullPointerException | NumberFormatException any) {}
		}
		if (z2) {
			if (applicationLibraryDirectory.isDirectory()) {
				FileUtils.clearFileTree(applicationLibraryDirectory, false);
			} else if (applicationLibraryDirectory.isFile()) {
				applicationLibraryDirectory.delete();
			}
			applicationLibraryDirectory.mkdirs();
			try {
				FileUtils.writeFileText(new File(applicationLibraryDirectory, ".timestamp"), "" + System.currentTimeMillis());
				return;
			} catch (IOException any) {}
		}
		ClassLoaderPatch.addNativeLibraryPath(HorizonLibrary.class.getClassLoader(), Environment.getApplicationLibraryDirectory());
	}
	
	private static void initializeLog() {
		LogFileHandler instance = LogFileHandler.getInstance();
		File newLogFile = instance.getNewLogFile("log.txt");
		if (newLogFile != null) {
			Logger.setOutputFile(newLogFile);
		} else {
			Logger.error("Failed to get log file path!");
		}
		File newLogFile2 = instance.getNewLogFile("crash.txt");
		if (newLogFile2 != null) {
			Logger.setCrashFile(newLogFile2);
		} else {
			Logger.error("Failed to get crash file path!");
		}
	}
	
	public void setCallbackOptions(boolean z, boolean z2) {}
}
