package com.zhekasmirnov.horizon.modloader.java;

import android.content.Context;
import android.content.res.AssetManager;
import com.zhekasmirnov.horizon.compiler.packages.Environment;
import com.zhekasmirnov.horizon.modloader.ModContext;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.runtime.task.Task;
import com.zhekasmirnov.horizon.runtime.task.TaskManager;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public class JavaCompilerHolder {
	private static final String COMPONENT_PATH = "sdk/java/";
	private static final HashMap<Context, JavaCompilerHolder> instances = new HashMap<>();
	private final Component[] COMPONENTS = { new JarComponent(COMPONENT_PATH, "android.jar"),
			new JarComponent(COMPONENT_PATH, "android-support-multidex.jar"),
			new JarComponent(COMPONENT_PATH, "android-support-v4.jar"), new JarComponent(COMPONENT_PATH, "dx.jar"),
			new JarComponent(COMPONENT_PATH, "support-annotations-25.3.1.jar"),
			new JarComponent(COMPONENT_PATH, "gson-2.6.2.jar"),
			new JarComponent(COMPONENT_PATH, "horizon-classes.jar") };
	private final String COMPONENT_VERSION_UUID = "34b14f6e-d8d1-48af-86a7-8adcb41396ce";
	private boolean configured = false;
	private final ModContext context;
	private final File installationDir;
	private boolean isInitialized = false;
	private boolean isInitializing = false;
	private boolean isInstalled = false;
	private final Object main;
	
	private interface Component {
		List<File> getBootFiles();
		boolean install();
		boolean isInstalled();
	}
	
	private class JarComponent implements Component {
		private final String assetName;
		private final String assetPath;
		
		private JarComponent(String str, String str2) {
			this.assetPath = str;
			this.assetName = str2;
		}
		
		private File getJarFile() {
			return new File(Environment.getJavacDir(JavaCompilerHolder.this.context.context), this.assetName);
		}
		
		private String getLockFile() {
			try {
				String javacDir = Environment.getJavacDir(JavaCompilerHolder.this.context.context);
				return FileUtils.readFileText(new File(javacDir, this.assetName + ".uuid")).trim();
			} catch (IOException | NullPointerException unused) {
				return null;
			}
		}
		
		private void setLockFile() {
			try {
				String javacDir = Environment.getJavacDir(JavaCompilerHolder.this.context.context);
				FileUtils.writeFileText(new File(javacDir, this.assetName + ".uuid"),
						"34b14f6e-d8d1-48af-86a7-8adcb41396ce");
			} catch (IOException unused) {
				throw new RuntimeException("failed to write UUID lock for jar component: " + this.assetName);
			}
		}
		
		public boolean isInstalled() {
			return getJarFile().exists() && "34b14f6e-d8d1-48af-86a7-8adcb41396ce".equals(getLockFile());
		}
		
		public boolean install() {
			try {
				AssetManager assets = JavaCompilerHolder.this.context.context.getAssets();
				File jarFile = getJarFile();
				FileUtils.unpackAssetOrDirectory(assets, jarFile, this.assetPath + this.assetName);
				setLockFile();
				return true;
			} catch (IOException e) {
				Logger.debug("JavaCompiler", "skipped installing non-exiting " + assetName + " component");
				return false;
			} catch (Exception e) {
				throw new RuntimeException("failed to install jar component " + this.assetName, e);
			}
		}
		
		public List<File> getBootFiles() {
			ArrayList<File> arrayList = new ArrayList<>();
			if (isInstalled()) {
				arrayList.add(getJarFile());
			}
			return arrayList;
		}
		
		public String toString() {
			return "[jar component " + this.assetName + "]";
		}
	}
	
	public static JavaCompilerHolder getInstance(Context context2) {
		JavaCompilerHolder javaCompilerHolder;
		synchronized (instances) {
			javaCompilerHolder = instances.get(context2);
		}
		return javaCompilerHolder;
	}
	
	public static void initializeForContext(ModContext modContext, TaskManager taskManager) {
		if (getInstance(modContext.context) == null) {
			synchronized (instances) {
				JavaCompilerHolder javaCompilerHolder = new JavaCompilerHolder(modContext);
				instances.put(modContext.context, javaCompilerHolder);
				taskManager.addTask(javaCompilerHolder.getInitializationTask());
			}
		}
	}
	
	public JavaCompilerHolder(ModContext modContext) {
		this.context = modContext;
		File file = new File(Environment.getJavacDir(modContext.context));
		this.installationDir = file;
		if (!file.exists()) {
			this.installationDir.mkdirs();
		}
		try {
			Class.forName("org.eclipse.jdt.internal.compiler.batch.Main");
		} catch (ClassNotFoundException e) {
			Logger.debug("JavaCompiler", "skipped java ecj initialization because it not found");
			this.main = null;
			return;
		}
		if (this.installationDir.isDirectory()) {
			this.main = new Main(
					new PrintWriter(modContext.getEventLogger().getStream(EventLogger.MessageType.INFO, "BUILD")),
					new PrintWriter(modContext.getEventLogger().getStream(EventLogger.MessageType.FAULT, "BUILD")),
					false, (Map<String, String>) null, (CompilationProgress) null);
			return;
		}
		throw new RuntimeException("failed to allocate installation directory " + this.installationDir);
	}
	
	private void initialize() {
		this.isInstalled = true;
		for (Component component : this.COMPONENTS) {
			if (!component.isInstalled()) {
				Logger.debug("JavaCompiler", "installing or re-installing java component: " + component);
				if (!component.install()) {
					this.isInstalled = false;
				}
			}
		}
	}
	
	public Task getInitializationTask() {
		return new Task() {
			public String getDescription() {
				return "initializing javac";
			}
			
			public Object getLock() {
				return "initialize_javac";
			}
			
			public void run() {
				if (!JavaCompilerHolder.this.isInitializing) {
					boolean unused = JavaCompilerHolder.this.isInitializing = true;
					if (!JavaCompilerHolder.this.isInitialized) {
						JavaCompilerHolder.this.initialize();
						JavaCompilerHolder.this.isInitialized = true;
					}
					JavaCompilerHolder.this.isInitializing = false;
				}
			}
		};
	}
	
	private void awaitInitialization() {
		while (!this.isInitialized) {
			Thread.yield();
		}
	}
	
	public boolean compile(JavaCompilerArguments javaCompilerArguments) {
		if (this.main == null) {
			return false;
		}
		if (!this.configured) {
			((Main) (this.main)).configure(javaCompilerArguments.toArray());
			this.configured = true;
		}
		awaitInitialization();
		return ((Main) (this.main)).compile(new String[0]);
	}
	
	public List<File> getBootFiles() {
		ArrayList<File> arrayList = new ArrayList<>();
		for (Component bootFiles : this.COMPONENTS) {
			arrayList.addAll(bootFiles.getBootFiles());
		}
		return arrayList;
	}
	
	void installLibraries(List<File> list, File file) {
		for (File next : list) {
			String name = next.getName();
			if (name.endsWith(".jar") || name.endsWith("zip")) {
				installJarLibrary(next, file);
			} else if (name.endsWith(".dex")) {
				File file2 = new File(next.getAbsolutePath().replace(".dex", ".jar"));
				installJarLibrary(file2, file);
				file2.delete();
			} else {
				throw new RuntimeException("Unsupported file format:  " + next.getName());
			}
		}
	}
	
	private void installJarLibrary(File file, File file2) {
		try {
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			file2.mkdirs();
			while (entries.hasMoreElements()) {
				JarEntry nextElement = entries.nextElement();
				if (nextElement.getName().endsWith(SuffixConstants.SUFFIX_STRING_class)) {
					File file3 = new File(file2 + File.separator + nextElement.getName());
					if (nextElement.isDirectory()) {
						file3.mkdir();
					} else if (!file3.exists()) {
						file3.getParentFile().mkdirs();
						InputStream inputStream = jarFile.getInputStream(nextElement);
						FileOutputStream fileOutputStream = new FileOutputStream(file3);
						byte[] bArr = new byte[8192];
						while (true) {
							int read = inputStream.read(bArr);
							if (read <= 0) {
								break;
							}
							fileOutputStream.write(bArr, 0, read);
						}
						fileOutputStream.close();
						inputStream.close();
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to install jar library " + file.getName(), e);
		}
	}
}
