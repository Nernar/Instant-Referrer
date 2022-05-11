package io.nernar.instant.environment;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.UriPermission;
import android.os.Environment;
import android.os.Handler;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.os.Build;
import android.transition.TransitionManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.transition.ChangeScroll;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.widget.ScrollView;
import com.zhekasmirnov.horizon.HorizonApplication;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.StringUtils;
import io.nernar.instant.R;
import io.nernar.instant.referrer.InstantReferrer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import com.zhekasmirnov.mcpe161.EnvironmentSetup;
import io.nernar.instant.launcher.InstantInnerCore;
import java.io.File;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.launcher.ContextHolder;
import android.widget.TextView;
import android.app.Activity;
import android.os.Bundle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LauncherActivity extends Activity {
	private static final String[] REQUIRED_PERMISSIONS = { "android.permission.WRITE_EXTERNAL_STORAGE" };
	private final HashMap<String, PermissionResult> permissionResults = new HashMap<>();

	private enum PermissionResult {
		GRANTED, DENIED, REJECTED
	}

	private class DecisionResult {
		String result;
	}

	private final Runnable LAUNCH_RUNNABLE = new Runnable() {
		public void run() {
			requestPermissionsIfNeeded();
			ContextHolder holder = new ContextHolder(LauncherActivity.this);
			printlnCopyright();
			Pack pack = getValidSelectedPack(holder);
			try {
				Method reflectionUnderModding = pack.getClass().getDeclaredMethod("initializeModContext");
				reflectionUnderModding.setAccessible(true);
				reflectionUnderModding.invoke(pack);
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new UnsupportedOperationException(e);
			}
			try {
				Method reflectionUnderModding = pack.getClass().getDeclaredMethod("initializeBootJavaDirs");
				reflectionUnderModding.setAccessible(true);
				reflectionUnderModding.invoke(pack);
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new UnsupportedOperationException(e);
			}
			try {
				Method reflectionUnderModding = pack.getClass().getDeclaredMethod("loadBootJavaLibraries");
				reflectionUnderModding.setAccessible(true);
				reflectionUnderModding.invoke(pack);
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new UnsupportedOperationException(e);
			}
			InstantInnerCore innerCore = new InstantInnerCore(LauncherActivity.this, pack);
			EnvironmentSetup.loadInstantInnerCore(pack, new ArrayList<>());
			EnvironmentSetup.initializeAndBuildInstant(pack);
			EnvironmentSetup.initiateInstantLaunch(LauncherActivity.this, innerCore);
			LauncherActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					View view = LauncherActivity.this.findViewById(R.id.logTextView);
					if (view != null)
						initiateDesiredAnimation(view);
				}
			});
		}
	};

	private void initPermissionResults() {
		synchronized (this.permissionResults) {
			for (String str : REQUIRED_PERMISSIONS) {
				this.permissionResults.put(str,
						ContextCompat.checkSelfPermission(this, str) == 0 ? PermissionResult.GRANTED
								: PermissionResult.DENIED);
			}
		}
	}

	private PermissionResult getAllPermissionsResult() {
		synchronized (this.permissionResults) {
			for (PermissionResult next : this.permissionResults.values()) {
				if (next == PermissionResult.REJECTED) {
					return next;
				}
				if (next == PermissionResult.DENIED) {
					return next;
				}
			}
			return PermissionResult.GRANTED;
		}
	}

	private void requestDeniedPermissions() {
		ArrayList<String> arrayList = new ArrayList<>();
		synchronized (this.permissionResults) {
			for (String next : this.permissionResults.keySet()) {
				if (this.permissionResults.get(next) != PermissionResult.GRANTED) {
					arrayList.add(next);
				}
			}
			for (String put : arrayList) {
				this.permissionResults.put(put, PermissionResult.DENIED);
			}
		}
		ActivityCompat.requestPermissions(this, arrayList.toArray(new String[arrayList.size()]), 0);
	}

	private void requestPermissionsIfNeeded() {
		initPermissionResults();
		if (getAllPermissionsResult() != PermissionResult.GRANTED) {
			requestDeniedPermissions();
			while (true) {
				PermissionResult allPermissionsResult = getAllPermissionsResult();
				if (allPermissionsResult != PermissionResult.GRANTED) {
					if (allPermissionsResult == PermissionResult.REJECTED) {
						requestDeniedPermissions();
					}
					Thread.yield();
				} else {
					return;
				}
			}
		}
	}

	public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
		super.onRequestPermissionsResult(i, strArr, iArr);
		synchronized (this.permissionResults) {
			for (int i2 = 0; i2 < strArr.length; i2++) {
				this.permissionResults.put(strArr[i2],
						iArr[i2] == 0 ? PermissionResult.GRANTED : PermissionResult.REJECTED);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		TextView logView = findViewById(R.id.logTextView);
		ScrollView logScroll = findViewById(R.id.logScrollView);
		System.setOut(new DebuggerStream(logView, logScroll));
		new Thread(LAUNCH_RUNNABLE).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Runtime.getRuntime().exit(0);
	}

	private void printlnCopyright() {
		try {
			Class.forName("org.eclipse.jdt.internal.compiler.batch.Main");
			System.out.println("Copyright, 2015 IBM Corp (Eclipse Compiler for Java(TM) 3.12.1)");
		} catch (ClassNotFoundException e) {
		}
		try {
			Class.forName("org.mozilla.javascript.Context");
			System.out.println("Copyright, 2022 Mozilla Foundation (Mozilla Rhino 1.7.14)");
		} catch (ClassNotFoundException e) {
			System.out.println("Copyright, 2015 Mozilla Foundation (Mozilla Rhino 1.7.7)");
		}
		System.out.println("Copyright, 2022 Horizon Team (Horizon 1.2, Inner Core)");
		System.out.println("Copyright, 2022 Nernar (Instant Referrer " + InstantReferrer.getVersionName() + ")");
		System.out.println("Project uses platform licensed libraries. All rights reserved.");
		System.out.println();
	}

	private void initiateDesiredAnimation(View view) {
		AlphaAnimation animation = new AlphaAnimation(1f, 0.4f);
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		animation.setDuration(1000);
		animation.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationEnd(Animation who) {
				view.setAlpha(0.4f);
			}

			public void onAnimationStart(Animation who) {}
			public void onAnimationRepeat(Animation who) {}
		});
		view.startAnimation(animation);
	}

	private String requestPackByUserSelection(List<String> packs) {
		DecisionResult selection = new DecisionResult();
		runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
				builder.setItems(packs.toArray(new String[packs.size()]), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface di, int index) {
						selection.result = packs.get(index);
					}
				});
				builder.setCancelable(false);
				AlertDialog dialog = builder.create();
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface di) {
						// In case if user just leave app when startup
						if (selection.result == null) {
							LauncherActivity.this.finish();
						}
					}
				});
				dialog.show();
			}
		});
		while (selection.result == null) {
			Thread.yield();
		}
		return selection.result;
	}

	private Pack getValidSelectedPack(ContextHolder holder) {
		String selectedFolder = getSharedPreferences("selected", 0).getString("pack_folder", null);
		File packsFolder = new File(Environment.getExternalStorageDirectory(), "games/horizon/packs");
		File pendingPackFolder = null;
		while (true) {
			if (selectedFolder != null) {
				pendingPackFolder = new File(packsFolder, selectedFolder);
			}
			if (pendingPackFolder != null) {
				try {
					Pack pack = new Pack(holder, pendingPackFolder);
					getSharedPreferences("selected", 0).edit().putString("pack_folder", selectedFolder).commit();
					pendingPackFolder = null;
					return pack;
				} catch (RuntimeException any) {
					System.out.println(StringUtils.getStackTrace(any));
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
					}
				}
			}
			ArrayList<String> availabledPacks = new ArrayList<>();
			for (String folder : packsFolder.list()) {
				if (new File(packsFolder, folder).isDirectory()) {
					availabledPacks.add(folder);
				}
			}
			selectedFolder = requestPackByUserSelection(availabledPacks);
		}
	}
}