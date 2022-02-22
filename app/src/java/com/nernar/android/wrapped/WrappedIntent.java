package com.nernar.android.wrapped;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.zhekasmirnov.horizon.activity.util.EmptyActivity;
import com.zhekasmirnov.innercore.api.runtime.other.PrintStacking;
import java.util.ArrayList;

public class WrappedIntent extends Intent {
	protected WrappedActivity wrapped;
	
	public WrappedIntent() {
		super();
	}
	
	public WrappedIntent(Intent o) {
		super(o);
	}
	
	public WrappedIntent(WrappedIntent o) {
		super(o);
		wrapped = o.wrapped;
		if (!queue.contains(this)) queue.add(this);
	}
	
    public WrappedIntent(String action) {
		super(action);
	}
	
    public WrappedIntent(String action, Uri uri) {
		super(action, uri);
	}
	
	public WrappedIntent(Context packageContext, Class<?> cls) {
		super(packageContext, isWrapped(cls) ? EmptyActivity.class : cls);
		if (isWrapped(cls)) wrapped = createWrappedActivity(packageContext, cls);
	}
	
    public WrappedIntent(String action, Uri uri, Context packageContext, Class<?> cls) {
		super(action, uri, packageContext, isWrapped(cls) ? EmptyActivity.class : cls);
		if (isWrapped(cls)) wrapped = createWrappedActivity(packageContext, cls);
	}
	
	private static boolean isWrapped(Class<?> cls) {
		if (cls == null) return false;
		while ((cls = cls.getSuperclass()) != null) {
			if (cls == WrappedActivity.class) return true;
		}
		return false;
	}
	
	protected <T extends WrappedActivity> T createWrappedActivity(Context packageContext, Class<?> cls) {
		try {
			T wrapped = (T) cls.getConstructor().newInstance();
			PrintStacking.print("Golly, instance alright");
			if (!queue.contains(this)) queue.add(this);
			return wrapped;
		} catch (Throwable t) {
			throw new IllegalStateException(t);
		}
	}
	
	public boolean isWrappedActivity() {
		return wrapped != null;
	}
	
	public WrappedActivity getWrappedActivity() {
		return wrapped;
	}
	
	private static ArrayList<WrappedIntent> queue = new ArrayList<>();
	
	public static class Receiver implements Application.ActivityLifecycleCallbacks {
		
		@Override
		public void onActivityCreated(Activity p1, Bundle p2) {
			Intent from = p1.getIntent();
			PrintStacking.print("Created " + p1);
			if (from instanceof WrappedIntent) {
				PrintStacking.print("There's good intent!");
				WrappedIntent intent = (WrappedIntent) from;
				if (queue.contains(intent)) {
					PrintStacking.print("It had in queue");
					if (intent.isWrappedActivity()) {
						PrintStacking.print("And wrapped activity as well");
						WrappedActivity wrapped = intent.getWrappedActivity();
						if (wrapped != null) {
							wrapped.wrapAsActivity(p1);
							wrapped.onCreate(p2);
							PrintStacking.print("Everything might be wonderful!");
						}
					}
					queue.remove(intent);
				}
			}
		}
		
		@Override
		public void onActivityStarted(Activity p1) {}
		@Override
		public void onActivityResumed(Activity p1) {}
		@Override
		public void onActivityPaused(Activity p1) {}
		@Override
		public void onActivityStopped(Activity p1) {}
		@Override
		public void onActivitySaveInstanceState(Activity p1, Bundle p2) {}
		@Override
		public void onActivityDestroyed(Activity p1) {}
	}
	
	@Override
	public Intent cloneFilter() {
		if (isWrappedActivity()) {
			Uri data = getData();
			String action = getAction();
			if (data != null || action != null) {
				return new WrappedIntent(action, data, wrapped.getWrapped(), wrapped.getClass());
			}
			return new WrappedIntent(wrapped.getWrapped(), wrapped.getClass());
		}
		return super.cloneFilter();
	}
}
