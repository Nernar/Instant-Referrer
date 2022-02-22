package com.nernar.instant.visual;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.RelativeLayout;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import java.util.ArrayList;
import java.util.Collection;

public class InstantActivityFactory extends Pack.MenuActivityFactory {
	protected InstantActivity activity;
	
	@Override
	public String getMenuTitle() {
		return "Instant Referrer";
	}
	
	protected Bitmap createColoredBitmap(int color) {
		Bitmap bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
		bitmap.setPixel(1, 1, color + 100);
		bitmap.setPixel(1, 2, color);
		bitmap.setPixel(2, 1, color);
		bitmap.setPixel(2, 2, color - 100);
		return bitmap;
	}
	
	@Override
	public Collection<Bitmap> getIconGraphicsBitmaps() {
		ArrayList<Bitmap> graphics = new ArrayList<>();
		graphics.add(createColoredBitmap(Color.YELLOW));
		graphics.add(createColoredBitmap(Color.GREEN));
		graphics.add(createColoredBitmap(Color.RED));
		graphics.add(createColoredBitmap(Color.BLUE));
		return graphics;
	}
	
	@Override
	public String getIconGraphics() {
		return "thumbnail";
	}
	
	@Override
	public void onCreateLayout(final Activity activity, final RelativeLayout content) {
		InstantActivity instant = new InstantActivity();
		instant.wrapAsActivity(activity);
		this.activity = instant;
	}
	
	@Override
	public boolean onBackPressed() {
		if (activity != null) {
			if (!activity.isMayLeave()) {
				return true;
			}
			activity = null;
		}
		return false;
	}
}
