package com.nernar.instant.visual;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import com.nernar.android.wrapped.WrappedLifecycleActivity;
import com.zheka.horizon.R;
import org.json.JSONException;
import org.json.JSONObject;

public class InstantActivity extends WrappedLifecycleActivity {
	private boolean mayLeave = false;
	
	public JSONObject getInstantPrototypeConfigInfo() {
		try {
			JSONObject json = new JSONObject();
			JSONObject description = new JSONObject();
			description.put("en", "Could not startup config information for Instant Referrer, try reinstall it from integrated Modification Browser.");
			description.put("ru", "Не удалось запустить описание настроек для среды раннего запуска, попробуйте переустановить ее из встроенного браузера модификаций.");
			json.put("description", description);
			return json;
		} catch (JSONException j) {
			// Should not happen
		}
		return null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ViewGroup layout = findViewById(R.id.custom_activity_root);
		Snackbar.make(layout, "Do you want something there?", Snackbar.LENGTH_INDEFINITE).setAction("Let me off", new View.OnClickListener() {
			
			@Override
			public void onClick(View p1) {
				mayLeave = true;
			}
		}).show();
		layout.addView(new Button(this));
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(InstantActivity.this, "Removing views and attach layout", Toast.LENGTH_LONG).show();
						/* final ConfigView config = new ConfigView(InstantActivity.this, "Instant Referrer", InstantConfig.getFile().getAbsolutePath());
						config.loadInfo(new InstantConfigInformation());
						layout.removeAllViews();
						config.display(layout); */
						Toast.makeText(InstantActivity.this, "Sequence fully completed", Toast.LENGTH_LONG).show();
					}
				});
			}
		}).start();
		Toast.makeText(this, "Added thread", Toast.LENGTH_LONG).show();
	}
	
	public boolean isMayLeave() {
		return mayLeave;
	}
	
	public void requireLite() {
		ViewGroup group = findViewById(R.id.custom_activity_root);
		group.removeAllViews();
		group.addView(new Switch(this));
	}
}
