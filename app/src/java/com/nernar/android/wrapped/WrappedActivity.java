package com.nernar.android.wrapped;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Dialog;
import android.app.DirectAction;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.SharedElementCallback;
import android.app.TaskStackBuilder;
import android.app.VoiceInteractor;
import android.app.assist.AssistContent;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.LocusId;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.PersistableBundle;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toolbar;
import com.zhekasmirnov.horizon.HorizonApplication;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

public class WrappedActivity extends ContextThemeWrapper implements LayoutInflater.Factory2,
		Window.Callback, KeyEvent.Callback, View.OnCreateContextMenuListener, ComponentCallbacks2 {
	protected WeakReference<Activity> wrapped;
	
	public static final int DEFAULT_KEYS_DISABLE = 0;
	public static final int DEFAULT_KEYS_DIALER = 1;
	public static final int DEFAULT_KEYS_SHORTCUT = 2;
	public static final int DEFAULT_KEYS_SEARCH_LOCAL = 3;
	public static final int DEFAULT_KEYS_SEARCH_GLOBAL = 4;
	
	protected static final int[] FOCUSED_STATE_SET;
	
	static {
		Class cls = Activity.class;
		int[] request = null;
		try {
			Field field = cls.getDeclaredField("FOCUSED_STATE_SET");
			field.setAccessible(true);
			request = (int[]) field.get(null);
		} catch (NoSuchFieldException e) {
			// Impossible to there's nothing
		} catch (IllegalAccessException e) {}
		FOCUSED_STATE_SET = request;
	}
	
	public static final int RESULT_OK = -1;
	public static final int RESULT_CANCELED = 0;
	public static final int RESULT_FIRST_USER = 1;
	
	public WrappedActivity() {
		super();
	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(newBase);
		onPrepare(newBase);
	}
	
	public void wrapAsActivity(Activity activity) {
		if (isWrapped()) unwrap();
		attachBaseContext(activity);
		wrapped = new WeakReference<Activity>(activity);
		registerComponentCallbacks(this);
		if (activity != null) {
			Window window = activity.getWindow();
			if (window != null) {
				window.setCallback(this);
			}
			List<Activity> stack = HorizonApplication.getActivityStack();
			if (stack != null && stack.contains(activity)) {
				onStart();
				onCreate(null);
			}
		}
	}
	
	public void unwrap(boolean forceRelease) {
		unregisterComponentCallbacks(this);
		if (wrapped != null) {
			Activity activity = wrapped.get();
			if (activity != null) {
				getWindow().setCallback(activity);
				if (forceRelease) {
					releaseInstance();
				}
			}
			wrapped.clear();
			wrapped = null;
		}
	}
	
	public void unwrap() {
		unwrap(false);
	}
	
	public final Activity getWrapped() {
		if (wrapped != null) {
			return wrapped.get();
		}
		return null;
	}
	
	public final boolean isWrapped() {
		return getWrapped() != null;
	}
	
	public Intent getIntent() {
		return wrapped.get().getIntent();
	}
	
	public void setIntent(Intent newIntent) {
		wrapped.get().setIntent(newIntent);
	}
	
	public void setLocusContext(LocusId locusId, Bundle bundle) {
		wrapped.get().setLocusContext(locusId, bundle);
	}
	
	public final Application getApplication() {
		return wrapped.get().getApplication();
	}
	
	public final boolean isChild() {
		return wrapped.get().isChild();
	}
	
	public final Activity getParent() {
		return wrapped.get().getParent();
	}
	
	public WindowManager getWindowManager() {
		return wrapped.get().getWindowManager();
	}
	
	public Window getWindow() {
		return wrapped.get().getWindow();
	}
	
	@Deprecated()
	public LoaderManager getLoaderManager() {
		return wrapped.get().getLoaderManager();
	}
	
	public View getCurrentFocus() {
		return wrapped.get().getCurrentFocus();
	}
	
	public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
		wrapped.get().registerActivityLifecycleCallbacks(callback);
	}
	
	public void unregisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
		wrapped.get().unregisterActivityLifecycleCallbacks(callback);
	}
	
	public boolean isVoiceInteraction() {
		return wrapped.get().isVoiceInteraction();
	}
	
	public boolean isVoiceInteractionRoot() {
		return wrapped.get().isVoiceInteractionRoot();
	}
	
	public VoiceInteractor getVoiceInteractor() {
		return wrapped.get().getVoiceInteractor();
	}
	
	public boolean isLocalVoiceInteractionSupported() {
		return wrapped.get().isLocalVoiceInteractionSupported();
	}
	
	public void startLocalVoiceInteraction(Bundle privateOptions) {
		wrapped.get().startLocalVoiceInteraction(privateOptions);
	}
	
	public void stopLocalVoiceInteraction() {
		wrapped.get().stopLocalVoiceInteraction();
	}
	
	public final void requestShowKeyboardShortcuts() {
		wrapped.get().requestShowKeyboardShortcuts();
	}
	
	public final void dismissKeyboardShortcutsHelper() {
		wrapped.get().dismissKeyboardShortcutsHelper();
	}
	
	public boolean showAssist(Bundle args) {
		return wrapped.get().showAssist(args);
	}
	
	public void reportFullyDrawn() {
		wrapped.get().reportFullyDrawn();
	}
	
	public boolean isInMultiWindowMode() {
		return wrapped.get().isInMultiWindowMode();
	}
	
	public boolean isInPictureInPictureMode() {
		return wrapped.get().isInPictureInPictureMode();
	}
	
	@Deprecated()
	public void enterPictureInPictureMode() {
		wrapped.get().enterPictureInPictureMode();
	}
	
	public boolean enterPictureInPictureMode(PictureInPictureParams params) {
		return wrapped.get().enterPictureInPictureMode(params);
	}
	
	public void setPictureInPictureParams(PictureInPictureParams params) {
		wrapped.get().setPictureInPictureParams(params);
	}
	
	public int getMaxNumPictureInPictureActions() {
		return wrapped.get().getMaxNumPictureInPictureActions();
	}
	
	public int getChangingConfigurations() {
		return wrapped.get().getChangingConfigurations();
	}
	
	public Object getLastNonConfigurationInstance() {
		return wrapped.get().getLastNonConfigurationInstance();
	}
	
	@Deprecated()
	public FragmentManager getFragmentManager() {
		return wrapped.get().getFragmentManager();
	}
	
	@Deprecated()
	public final Cursor managedQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return wrapped.get().managedQuery(uri, projection, selection, selectionArgs, sortOrder);
	}
	
	@Deprecated()
	public void startManagingCursor(Cursor c) {
		wrapped.get().startManagingCursor(c);
	}
	
	@Deprecated()
	public void stopManagingCursor(Cursor c) {
		wrapped.get().stopManagingCursor(c);
	}
	
	public <T extends View> T findViewById(int id) {
		return wrapped.get().findViewById(id);
	}
	
	public final <T extends View> T requireViewById(int id) {
		return wrapped.get().requireViewById(id);
	}
	
	public ActionBar getActionBar() {
		return wrapped.get().getActionBar();
	}
	
	public void setActionBar(Toolbar toolbar) {
		wrapped.get().setActionBar(toolbar);
	}
	
	public void setContentView(int layoutResID) {
		wrapped.get().setContentView(layoutResID);
	}
	
	public void setContentView(View view) {
		wrapped.get().setContentView(view);
	}
	
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		wrapped.get().setContentView(view, params);
	}
	
	public void addContentView(View view, ViewGroup.LayoutParams params) {
		wrapped.get().addContentView(view, params);
	}
	
	public TransitionManager getContentTransitionManager() {
		return wrapped.get().getContentTransitionManager();
	}
	
	public void setContentTransitionManager(TransitionManager tm) {
		wrapped.get().setContentTransitionManager(tm);
	}
	
	public Scene getContentScene() {
		return wrapped.get().getContentScene();
	}
	
	public void setFinishOnTouchOutside(boolean finish) {
		wrapped.get().setFinishOnTouchOutside(finish);
	}
	
	public final void setDefaultKeyMode(int mode) {
		wrapped.get().setDefaultKeyMode(mode);
	}
	
	public boolean hasWindowFocus() {
		return wrapped.get().hasWindowFocus();
	}
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		return wrapped.get().dispatchKeyEvent(event);
	}
	
	public boolean dispatchKeyShortcutEvent(KeyEvent event) {
		return wrapped.get().dispatchKeyShortcutEvent(event);
	}
	
	public boolean dispatchTouchEvent(MotionEvent event) {
		return wrapped.get().dispatchTouchEvent(event);
	}
	
	public boolean dispatchTrackballEvent(MotionEvent event) {
		return wrapped.get().dispatchTrackballEvent(event);
	}
	
	public boolean dispatchGenericMotionEvent(MotionEvent event) {
		return wrapped.get().dispatchGenericMotionEvent(event);
	}
	
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		return wrapped.get().dispatchPopulateAccessibilityEvent(event);
	}
	
	public void invalidateOptionsMenu() {
		wrapped.get().invalidateOptionsMenu();
	}
	
	public void openOptionsMenu() {
		wrapped.get().openOptionsMenu();
	}
	
	public void closeOptionsMenu() {
		wrapped.get().closeOptionsMenu();
	}
	
	public void registerForContextMenu(View view) {
		wrapped.get().registerForContextMenu(view);
	}
	
	public void unregisterForContextMenu(View view) {
		wrapped.get().unregisterForContextMenu(view);
	}
	
	public void openContextMenu(View view) {
		wrapped.get().openContextMenu(view);
	}
	
	public void closeContextMenu() {
		wrapped.get().closeContextMenu();
	}
	
	@Deprecated()
	public final void showDialog(int id) {
		Dialog dialog = onCreateDialog(id);
		if (dialog != null) {
			onPrepareDialog(id, dialog);
		}
		wrapped.get().showDialog(id);
	}
	
	@Deprecated()
	public final boolean showDialog(int id, Bundle args) {
		Dialog dialog = onCreateDialog(id, args);
		if (dialog != null) {
			onPrepareDialog(id, dialog, args);
		}
		return wrapped.get().showDialog(id, args);
	}
	
	@Deprecated()
	public final void dismissDialog(int id) {
		wrapped.get().dismissDialog(id);
	}
	
	@Deprecated()
	public final void removeDialog(int id) {
		wrapped.get().removeDialog(id);
	}
	
	public final SearchEvent getSearchEvent() {
		return wrapped.get().getSearchEvent();
	}
	
	public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, boolean globalSearch) {
		wrapped.get().startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
	}
	
	public void triggerSearch(String query, Bundle appSearchData) {
		wrapped.get().triggerSearch(query, appSearchData);
	}
	
	public void takeKeyEvents(boolean get) {
		wrapped.get().takeKeyEvents(get);
	}
	
	public final boolean requestWindowFeature(int featureId) {
		return wrapped.get().requestWindowFeature(featureId);
	}
	
	public final void setFeatureDrawableResource(int featureId, int resId) {
		wrapped.get().setFeatureDrawableResource(featureId, resId);
	}
	
	public final void setFeatureDrawableUri(int featureId, Uri uri) {
		wrapped.get().setFeatureDrawableUri(featureId, uri);
	}
	
	public final void setFeatureDrawable(int featureId, Drawable drawable) {
		wrapped.get().setFeatureDrawable(featureId, drawable);
	}
	
	public final void setFeatureDrawableAlpha(int featureId, int alpha) {
		wrapped.get().setFeatureDrawableAlpha(featureId, alpha);
	}
	
	public LayoutInflater getLayoutInflater() {
		return wrapped.get().getLayoutInflater();
	}
	
	public MenuInflater getMenuInflater() {
		return wrapped.get().getMenuInflater();
	}
	
	public void setTheme(int resid) {
		wrapped.get().setTheme(resid);
	}
	
	public final void requestPermissions(String[] permissions, int requestCode) {
		wrapped.get().requestPermissions(permissions, requestCode);
	}
	
	public boolean shouldShowRequestPermissionRationale(String permission) {
		return wrapped.get().shouldShowRequestPermissionRationale(permission);
	}
	
	public void startActivityForResult(Intent intent, int requestCode) {
		wrapped.get().startActivityForResult(intent, requestCode);
	}
	
	public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
		wrapped.get().startActivityForResult(intent, requestCode, options);
	}
	
	public boolean isActivityTransitionRunning() {
		return wrapped.get().isActivityTransitionRunning();
	}
	
	public void startIntentSenderForResult(IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
		wrapped.get().startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags);
	}
	
	public void startIntentSenderForResult(IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
		wrapped.get().startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
	}
	
	public void startActivity(Intent intent) {
		wrapped.get().startActivity(intent);
	}
	
	public void startActivity(Intent intent, Bundle options) {
		wrapped.get().startActivity(intent, options);
	}
	
	public void startActivities(Intent[] intents) {
		wrapped.get().startActivities(intents);
	}
	
	public void startActivities(Intent[] intents, Bundle options) {
		wrapped.get().startActivities(intents, options);
	}
	
	public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
		wrapped.get().startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
	}
	
	public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
		wrapped.get().startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, options);
	}
	
	public boolean startActivityIfNeeded(Intent intent, int requestCode) {
		return wrapped.get().startActivityIfNeeded(intent, requestCode);
	}
	
	public boolean startActivityIfNeeded(Intent intent, int requestCode, Bundle options) {
		return wrapped.get().startActivityIfNeeded(intent, requestCode, options);
	}
	
	public boolean startNextMatchingActivity(Intent intent) {
		return wrapped.get().startNextMatchingActivity(intent);
	}
	
	public boolean startNextMatchingActivity(Intent intent, Bundle options) {
		return wrapped.get().startNextMatchingActivity(intent, options);
	}
	
	@Deprecated()
	public void startActivityFromChild(Activity child, Intent intent, int requestCode) {
		wrapped.get().startActivityFromChild(child, intent, requestCode);
	}
	
	@Deprecated()
	public void startActivityFromChild(Activity child, Intent intent, int requestCode, Bundle options) {
		wrapped.get().startActivityFromChild(child, intent, requestCode, options);
	}
	
	@Deprecated()
	public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
		wrapped.get().startActivityFromFragment(fragment, intent, requestCode);
	}
	
	@Deprecated()
	public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options) {
		wrapped.get().startActivityFromFragment(fragment, intent, requestCode, options);
	}
	
	@Deprecated()
	public void startIntentSenderFromChild(Activity child, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
		wrapped.get().startIntentSenderFromChild(child, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags);
	}
	
	@Deprecated()
	public void startIntentSenderFromChild(Activity child, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
		wrapped.get().startIntentSenderFromChild(child, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
	}
	
	public void overridePendingTransition(int enterAnim, int exitAnim) {
		wrapped.get().overridePendingTransition(enterAnim, exitAnim);
	}
	
	public final void setResult(int resultCode) {
		wrapped.get().setResult(resultCode);
	}
	
	public final void setResult(int resultCode, Intent data) {
		wrapped.get().setResult(resultCode, data);
	}
	
	public Uri getReferrer() {
		return wrapped.get().getReferrer();
	}
	
	public String getCallingPackage() {
		return wrapped.get().getCallingPackage();
	}
	
	public ComponentName getCallingActivity() {
		return wrapped.get().getCallingActivity();
	}
	
	public void setVisible(boolean visible) {
		wrapped.get().setVisible(visible);
	}
	
	public boolean isFinishing() {
		return wrapped.get().isFinishing();
	}
	
	public boolean isDestroyed() {
		return wrapped.get().isDestroyed();
	}
	
	public boolean isChangingConfigurations() {
		return wrapped.get().isChangingConfigurations();
	}
	
	public void recreate() {
		wrapped.get().recreate();
	}
	
	public void finish() {
		wrapped.get().finish();
	}
	
	public void finishAffinity() {
		wrapped.get().finishAffinity();
	}
	
	@Deprecated()
	public void finishFromChild(Activity child) {
		wrapped.get().finishFromChild(child);
	}
	
	public void finishAfterTransition() {
		wrapped.get().finishAfterTransition();
	}
	
	public void finishActivity(int requestCode) {
		wrapped.get().finishActivity(requestCode);
	}
	
	@Deprecated()
	public void finishActivityFromChild(Activity child, int requestCode) {
		wrapped.get().finishActivityFromChild(child, requestCode);
	}
	
	public void finishAndRemoveTask() {
		wrapped.get().finishAndRemoveTask();
	}
	
	public boolean releaseInstance() {
		return wrapped.get().releaseInstance();
	}
	
	public PendingIntent createPendingResult(int requestCode, Intent data, int flags) {
		return wrapped.get().createPendingResult(requestCode, data, flags);
	}
	
	public void setRequestedOrientation(int requestedOrientation) {
		wrapped.get().setRequestedOrientation(requestedOrientation);
	}
	
	public int getRequestedOrientation() {
		return wrapped.get().getRequestedOrientation();
	}
	
	public int getTaskId() {
		return wrapped.get().getTaskId();
	}
	
	public boolean isTaskRoot() {
		return wrapped.get().isTaskRoot();
	}
	
	public boolean moveTaskToBack(boolean nonRoot) {
		return wrapped.get().moveTaskToBack(nonRoot);
	}
	
	public String getLocalClassName() {
		return wrapped.get().getLocalClassName();
	}
	
	public ComponentName getComponentName() {
		return wrapped.get().getComponentName();
	}
	
	public SharedPreferences getPreferences(int mode) {
		return wrapped.get().getPreferences(mode);
	}
	
	public Object getSystemService(String name) {
		return wrapped.get().getSystemService(name);
	}
	
	public void setTitle(CharSequence title) {
		wrapped.get().setTitle(title);
	}
	
	public void setTitle(int titleId) {
		wrapped.get().setTitle(titleId);
	}
	
	@Deprecated()
	public void setTitleColor(int textColor) {
		wrapped.get().setTitleColor(textColor);
	}
	
	public final CharSequence getTitle() {
		return wrapped.get().getTitle();
	}
	
	public final int getTitleColor() {
		return wrapped.get().getTitleColor();
	}
	
	public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
		wrapped.get().setTaskDescription(taskDescription);
	}
	
	@Deprecated()
	public final void setProgressBarVisibility(boolean visible) {
		wrapped.get().setProgressBarVisibility(visible);
	}
	
	@Deprecated()
	public final void setProgressBarIndeterminateVisibility(boolean visible) {
		wrapped.get().setProgressBarIndeterminateVisibility(visible);
	}
	
	@Deprecated()
	public final void setProgressBarIndeterminate(boolean indeterminate) {
		wrapped.get().setProgressBarIndeterminate(indeterminate);
	}
	
	@Deprecated()
	public final void setProgress(int progress) {
		wrapped.get().setProgress(progress);
	}
	
	@Deprecated()
	public final void setSecondaryProgress(int secondaryProgress) {
		wrapped.get().setSecondaryProgress(secondaryProgress);
	}
	
	public final void setVolumeControlStream(int streamType) {
		wrapped.get().setVolumeControlStream(streamType);
	}
	
	public final int getVolumeControlStream() {
		return wrapped.get().getVolumeControlStream();
	}
	
	public final void setMediaController(MediaController controller) {
		wrapped.get().setMediaController(controller);
	}
	
	public final MediaController getMediaController() {
		return wrapped.get().getMediaController();
	}
	
	public final void runOnUiThread(Runnable action) {
		wrapped.get().runOnUiThread(action);
	}
	
	public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
		wrapped.get().dump(prefix, fd, writer, args);
	}
	
	public boolean isImmersive() {
		return wrapped.get().isImmersive();
	}
	
	public boolean setTranslucent(boolean translucent) {
		return wrapped.get().setTranslucent(translucent);
	}
	
	@Deprecated()
	public boolean requestVisibleBehind(boolean visible) {
		return wrapped.get().requestVisibleBehind(visible);
	}
	
	public void setImmersive(boolean i) {
		wrapped.get().setImmersive(i);
	}
	
	public void setVrModeEnabled(boolean enabled, ComponentName requestedComponent) throws PackageManager.NameNotFoundException {
		wrapped.get().setVrModeEnabled(enabled, requestedComponent);
	}
	
	public ActionMode startActionMode(ActionMode.Callback callback) {
		return wrapped.get().startActionMode(callback);
	}
	
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		return wrapped.get().startActionMode(callback, type);
	}
	
	public boolean shouldUpRecreateTask(Intent targetIntent) {
		return wrapped.get().shouldUpRecreateTask(targetIntent);
	}
	
	public boolean navigateUpTo(Intent upIntent) {
		return wrapped.get().navigateUpTo(upIntent);
	}
	
	@Deprecated()
	public boolean navigateUpToFromChild(Activity child, Intent upIntent) {
		return wrapped.get().navigateUpToFromChild(child, upIntent);
	}
	
	public Intent getParentActivityIntent() {
		return wrapped.get().getParentActivityIntent();
	}
	
	public void setEnterSharedElementCallback(SharedElementCallback callback) {
		wrapped.get().setEnterSharedElementCallback(callback);
	}
	
	public void setExitSharedElementCallback(SharedElementCallback callback) {
		wrapped.get().setExitSharedElementCallback(callback);
	}
	
	public void postponeEnterTransition() {
		wrapped.get().postponeEnterTransition();
	}
	
	public void startPostponedEnterTransition() {
		wrapped.get().startPostponedEnterTransition();
	}
	
	public DragAndDropPermissions requestDragAndDropPermissions(DragEvent event) {
		return wrapped.get().requestDragAndDropPermissions(event);
	}
	
	public void startLockTask() {
		wrapped.get().startLockTask();
	}
	
	public void stopLockTask() {
		wrapped.get().stopLockTask();
	}
	
	public void showLockTaskEscapeMessage() {
		wrapped.get().showLockTaskEscapeMessage();
	}
	
	public void setShowWhenLocked(boolean showWhenLocked) {
		wrapped.get().setShowWhenLocked(showWhenLocked);
	}
	
	public void setInheritShowWhenLocked(boolean inheritShowWhenLocked) {
		wrapped.get().setInheritShowWhenLocked(inheritShowWhenLocked);
	}
	
	public void setTurnScreenOn(boolean turnScreenOn) {
		wrapped.get().setTurnScreenOn(turnScreenOn);
	}
	
	protected void onCreate(Bundle savedInstanceState) {}
	public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {}
	protected void onRestoreInstanceState(Bundle savedInstanceState) {}
	public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {}
	protected void onPostCreate(Bundle savedInstanceState) {}
	public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {}
	protected void onStart() {}
	protected void onRestart() {}
	@Deprecated()
	public void onStateNotSaved() {}
	protected void onResume() {}
	protected void onPostResume() {}
	public void onTopResumedActivityChanged(boolean isTopResumedActivity) {}
	protected void onNewIntent(Intent intent) {}
	protected void onSaveInstanceState(Bundle outState) {}
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {}
	protected void onPause() {}
	protected void onUserLeaveHint() {}
	protected void onStop() {}
	
	protected void onDestroy() {
		unwrap();
	}
	
	protected void onPrepare(Context packageContext) {}
	
	public void onLocalVoiceInteractionStarted() {}
	public void onLocalVoiceInteractionStopped() {}
	
	public void onProvideAssistData(Bundle data) {}
	public void onProvideAssistContent(AssistContent outContent) {}
	public void onGetDirectActions(CancellationSignal cancellationSignal, Consumer<List<DirectAction>> callback) {}
	public void onPerformDirectAction(String actionId, Bundle arguments, CancellationSignal cancellationSignal, Consumer<Bundle> resultListener) {}
	public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {}
	
	public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {}
	@Deprecated()
	public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {}
	public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {}
	@Deprecated()
	public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {}
	
	public void onConfigurationChanged(Configuration newConfig) {}
	public void onLowMemory() {}
	public void onTrimMemory(int level) {}
	
	@Deprecated()
	public void onAttachFragment(Fragment fragment) {}
	
	public void onBackPressed() {}
	
	public void onUserInteraction() {}
	
	public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
		wrapped.get().onWindowAttributesChanged(params);
	}
	
	public void onContentChanged() {
		wrapped.get().onContentChanged();
	}
	
	public void onWindowFocusChanged(boolean hasFocus) {
		wrapped.get().onWindowFocusChanged(hasFocus);
	}
	
	public void onAttachedToWindow() {
		wrapped.get().onAttachedToWindow();
	}
	
	public void onDetachedFromWindow() {
		wrapped.get().onDetachedFromWindow();
	}
	
	public void onPanelClosed(int featureId, Menu menu) {
		wrapped.get().onPanelClosed(featureId, menu);
	}
	
	public void onCreateNavigateUpTaskStack(TaskStackBuilder builder) {}
	public void onPrepareNavigateUpTaskStack(TaskStackBuilder builder) {}
	public void onOptionsMenuClosed(Menu menu) {}
	public void onContextMenuClosed(Menu menu) {}
	
	@Deprecated()
	protected void onPrepareDialog(int id, Dialog dialog) {
		onPrepareDialog(id, dialog, null);
	}
	
	@Deprecated()
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {}
	
	protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {}
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {}
	public void onActivityReenter(int resultCode, Intent data) {}
	protected void onTitleChanged(CharSequence title, int color) {}
	protected void onChildTitleChanged(Activity childActivity, CharSequence title) {}
	
	@Deprecated()
	public void onVisibleBehindCanceled() {}
	public void onEnterAnimationComplete() {}
	
	public void onActionModeStarted(ActionMode mode) {
		wrapped.get().onActionModeStarted(mode);
	}
	
	public void onActionModeFinished(ActionMode mode) {
		wrapped.get().onActionModeFinished(mode);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {}
	
	@Deprecated()
	public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
		return false;
	}
	
	public CharSequence onCreateDescription() {
		return null;
	}
	
	public boolean onPictureInPictureRequested() {
		return false;
	}
	
	public Object onRetainNonConfigurationInstance() {
		return null;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		return false;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}
	
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		return false;
	}
	
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		return false;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
	
	public boolean onTrackballEvent(MotionEvent event) {
		return false;
	}
	
	public boolean onGenericMotionEvent(MotionEvent event) {
		return false;
	}
	
	public View onCreatePanelView(int featureId) {
		return wrapped.get().onCreatePanelView(featureId);
	}
	
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		return wrapped.get().onCreatePanelMenu(featureId, menu);
	}
	
	public boolean onPreparePanel(int featureId, View view, Menu menu) {
		return wrapped.get().onPreparePanel(featureId, view, menu);
	}
	
	public boolean onMenuOpened(int featureId, Menu menu) {
		return wrapped.get().onMenuOpened(featureId, menu);
	}
	
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		return wrapped.get().onMenuItemSelected(featureId, item);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		return false;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}
	
	public boolean onNavigateUp() {
		return false;
	}
	
	@Deprecated()
	public boolean onNavigateUpFromChild(Activity child) {
		return false;
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		return false;
	}
	
	@Deprecated()
	protected Dialog onCreateDialog(int id) {
		return onCreateDialog(id, null);
	}
	
	@Deprecated()
	protected Dialog onCreateDialog(int id, Bundle args) {
		return null;
	}
	
	public boolean onSearchRequested(SearchEvent searchEvent) {
		return wrapped.get().onSearchRequested(searchEvent);
	}
	
	public boolean onSearchRequested() {
		return wrapped.get().onSearchRequested();
	}
	
	public Uri onProvideReferrer() {
		return null;
	}
	
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		return null;
	}
	
	public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
		return null;
	}
	
	public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
		return wrapped.get().onWindowStartingActionMode(callback);
	}
	
	public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
		return wrapped.get().onWindowStartingActionMode(callback, type);
	}
}
