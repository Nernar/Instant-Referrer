package com.nernar.android.wrapped;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

public class WrappedLifecycleActivity extends WrappedActivity {
	protected final Application.ActivityLifecycleCallbacks mCallbacks = new Application.ActivityLifecycleCallbacks() {
		
		@Override
		public void onActivityCreated(Activity p1, Bundle p2) {
			if (wrapped.get() != null && wrapped.get().equals(p1)) {
				WrappedLifecycleActivity.this.onCreate(p2);
			}
		}
		
		@Override
		public void onActivityStarted(Activity p1) {
			if (wrapped.get() != null && wrapped.get().equals(p1)) {
				WrappedLifecycleActivity.this.onStart();
			}
		}
		
		@Override
		public void onActivityResumed(Activity p1) {
			if (wrapped.get() != null && wrapped.get().equals(p1)) {
				WrappedLifecycleActivity.this.onResume();
			}
		}
		
		@Override
		public void onActivityPaused(Activity p1) {
			if (wrapped.get() != null && wrapped.get().equals(p1)) {
				WrappedLifecycleActivity.this.onPause();
			}
		}
		
		@Override
		public void onActivityStopped(Activity p1) {
			if (wrapped.get() != null && wrapped.get().equals(p1)) {
				WrappedLifecycleActivity.this.onStop();
			}
		}
		
		@Override
		public void onActivitySaveInstanceState(Activity p1, Bundle p2) {
			if (wrapped.get() != null && wrapped.get().equals(p1)) {
				WrappedLifecycleActivity.this.onSaveInstanceState(p2);
			}
		}
		
		@Override
		public void onActivityDestroyed(Activity p1) {
			if (wrapped.get() != null && wrapped.get().equals(p1)) {
				WrappedLifecycleActivity.this.onDestroy();
			}
		}
	};
	
	public WrappedLifecycleActivity() {
		super();
	}
	
	@Override
	public void wrapAsActivity(Activity activity) {
		super.wrapAsActivity(activity);
		if (activity != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				activity.registerActivityLifecycleCallbacks(mCallbacks);
			} else {
				getApplication().registerActivityLifecycleCallbacks(mCallbacks);
			}
		}
	}
	
	@Override
	public void unwrap() {
		Activity activity = getWrapped();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			if (activity == null) throw new UnsupportedOperationException();
			activity.unregisterActivityLifecycleCallbacks(mCallbacks);
		} else {
			getApplication().unregisterActivityLifecycleCallbacks(mCallbacks);
		}
		super.unwrap();
	}
}
