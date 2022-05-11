package com.zhekasmirnov.horizon;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import com.zheka.horizon.BuildConfig;
import com.zhekasmirnov.horizon.activity.main.StartupWrapperActivity;
import com.zhekasmirnov.horizon.runtime.logger.CoreLogger;
import com.zhekasmirnov.horizon.runtime.logger.LogFileHandler;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class HorizonApplication extends MultiDexApplication implements Thread.UncaughtExceptionHandler {
	private static final HashMap<String, ApplicationLock> applicationLocks = new HashMap<>();
	private static WeakReference<HorizonApplication> instance;
	private final HashSet<Activity> activitiesOnTop = new HashSet<>();
	private final List<Activity> activityStack = new ArrayList<>();
	private final List<Activity> runningActivities = new ArrayList<>();
	private Thread.UncaughtExceptionHandler systemExceptionHandler = null;
	
	public static HorizonApplication getInstance() {
		WeakReference<HorizonApplication> weakReference = instance;
		if (weakReference != null) {
			return (HorizonApplication) weakReference.get();
		}
		return null;
	}
	
	public HorizonApplication() {
		instance = new WeakReference<>(this);
		registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
			public void onActivityPaused(Activity activity) {}
			public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}
			
			public void onActivityCreated(Activity activity, Bundle bundle) {
				synchronized (HorizonApplication.this.activityStack) {
					HorizonApplication.this.activityStack.add(0, activity);
					HorizonApplication.this.runningActivities.add(activity);
				}
			}
			
			public void onActivityStarted(Activity activity) {
				synchronized (HorizonApplication.this.activityStack) {
					if (!HorizonApplication.this.runningActivities.contains(activity)) {
						HorizonApplication.this.runningActivities.add(activity);
					}
				}
				synchronized (HorizonApplication.this.activitiesOnTop) {
					HorizonApplication.this.activitiesOnTop.add(activity);
				}
			}
			
			public void onActivityResumed(Activity activity) {
				synchronized (HorizonApplication.this.activitiesOnTop) {
					HorizonApplication.this.activitiesOnTop.add(activity);
				}
			}
			
			public void onActivityStopped(Activity activity) {
				synchronized (HorizonApplication.this.activityStack) {
					HorizonApplication.this.runningActivities.remove(activity);
				}
				synchronized (HorizonApplication.this.activitiesOnTop) {
					HorizonApplication.this.activitiesOnTop.remove(activity);
				}
			}
			
			public void onActivityDestroyed(Activity activity) {
				synchronized (HorizonApplication.this.activityStack) {
					HorizonApplication.this.activityStack.remove(activity);
				}
			}
		});
	}
	
	public void uncaughtException(Thread thread, Throwable th) {
		StringWriter stringWriter = new StringWriter();
		th.printStackTrace(new PrintWriter(stringWriter));
		try {
			FileUtils.writeFileText(LogFileHandler.getInstance().getNewLogFile("crash.txt"), stringWriter.toString());
			CoreLogger.log("UncaughtException", stringWriter.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Thread.UncaughtExceptionHandler uncaughtExceptionHandler = this.systemExceptionHandler;
		if (uncaughtExceptionHandler != null) {
			uncaughtExceptionHandler.uncaughtException(thread, th);
		}
	}
	
	private Throwable getRootCause(Throwable th) {
		Throwable cause = th.getCause();
		if (cause == null) {
			return th;
		}
		return getRootCause(cause);
	}
	
	public void onCreate() {
		this.systemExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		super.onCreate();
	}
	
	public boolean isActivityRunning(Class cls) {
		synchronized (this.activityStack) {
			for (Activity next : this.activityStack) {
				if (cls.isInstance(next) && !next.isFinishing() && !this.runningActivities.contains(next)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public List<Class<? extends Activity>> getRunningActivityClasses() {
		ArrayList<Class<? extends Activity>> arrayList = new ArrayList<>();
		synchronized (this.activityStack) {
			for (Activity activity : this.activityStack) {
				arrayList.add(activity.getClass());
			}
		}
		return arrayList;
	}
	
	public Activity getTopRunningActivity0() {
		for (Activity next : this.activityStack) {
			if (!next.isFinishing() && this.runningActivities.contains(next)) {
				return next;
			}
		}
		return null;
	}
	
	public static Activity getTopRunningActivity() {
		Activity topRunningActivity0;
		for (int i = 0; i < 10; i++) {
			HorizonApplication instance2 = getInstance();
			if (instance2 != null && (topRunningActivity0 = instance2.getTopRunningActivity0()) != null) {
				return topRunningActivity0;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException unused) {
			}
		}
		return null;
	}
	
	public static Activity getTopActivity() {
		HorizonApplication instance2 = getInstance();
		if (instance2 == null || instance2.activityStack.size() <= 0) {
			return null;
		}
		return instance2.activityStack.get(0);
	}
	
	public static List<Activity> getActivityStack() {
		ArrayList<Activity> arrayList = null;
		HorizonApplication instance2 = getInstance();
		if (instance2 != null) {
			List<Activity> list = instance2.activityStack;
		} else {
			arrayList = new ArrayList<>();
		}
		return arrayList;
	}
	
	public static HashSet<Activity> getActivitiesOnTop() {
		HorizonApplication instance2 = getInstance();
		return instance2 != null ? instance2.activitiesOnTop : new HashSet<>();
	}
	
	public static boolean moveToBackgroundIfNotOnTop(Activity activity) {
		Iterator<Activity> it = getActivitiesOnTop().iterator();
		while (it.hasNext()) {
			if (it.next().getClass() != activity.getClass()) {
				return false;
			}
		}
		activity.moveTaskToBack(true);
		return true;
	}
	
	public static void terminate() {
		for (Activity finish : getActivityStack()) {
			finish.finish();
		}
		Runtime.getRuntime().exit(0);
	}
	
	public static void restart() {
		Activity topActivity = getTopActivity();
		topActivity.startActivity(Intent.makeRestartActivityTask(topActivity.getPackageManager().getLaunchIntentForPackage(topActivity.getPackageName()).getComponent()));
		Runtime.getRuntime().exit(0);
	}
	
	public static String getAppPackageName() {
		HorizonApplication instance2 = getInstance();
		return instance2 != null ? instance2.getApplicationInfo().packageName : BuildConfig.APPLICATION_ID;
	}
	
	public static File getExternalDataDir() {
		return new File(Environment.getExternalStorageDirectory(), new File("android/data", getAppPackageName()).getAbsolutePath());
	}
	
	public static class ApplicationLock {
		private boolean isLocked = false;
		
		public boolean tryLock() {
			synchronized (this) {
				if (this.isLocked) {
					return false;
				}
				this.isLocked = true;
				return true;
			}
		}
		
		public boolean lock() {
			while (this.isLocked) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException unused) {
					return false;
				}
			}
			return true;
		}
		
		public void unlock() {
			synchronized (this) {
				this.isLocked = false;
			}
		}
	}
	
	public static ApplicationLock getLock(String str) {
		ApplicationLock applicationLock;
		synchronized (applicationLocks) {
			applicationLock = applicationLocks.get(str);
			if (applicationLock == null) {
				applicationLock = new ApplicationLock();
				applicationLocks.put(str, applicationLock);
			}
		}
		return applicationLock;
	}
	
	private void initializeFirebase0(Context context) {}
	
	private void sendFirebaseEvent0(String str, Bundle bundle) {
		System.out.println("sending firebase event: " + str + " " + bundle);
	}
	
	public static void initializeFirebase(Context context) {
		HorizonApplication instance2 = getInstance();
		if (instance2 != null) {
			instance2.initializeFirebase0(context);
		}
	}
	
	public static void sendFirebaseEvent(String str, Bundle bundle) {
		HorizonApplication instance2 = getInstance();
		if (instance2 != null) {
			instance2.sendFirebaseEvent0(str, bundle);
		}
	}
}
