package com.nernar.instant.visual;

import android.app.Activity;
import android.widget.RelativeLayout;

public class InstantLiteActivityFactory extends InstantActivityFactory {
	
	@Override
	public void onCreateLayout(Activity activity, RelativeLayout content) {
		super.onCreateLayout(activity, content);
		this.activity.requireLite();
	}
}
