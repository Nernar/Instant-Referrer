package io.nernar.instant.visual;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.RelativeLayout;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import io.nernar.instant.referrer.InstantConfig;
import io.nernar.instant.storage.external.InstantConfigInformation;
import org.mineprogramming.horizon.innercore.view.config.ConfigPage;
import org.mineprogramming.horizon.innercore.view.page.PagesManager;
import java.util.ArrayList;
import java.util.Collection;

public class InstantActivityFactory extends Pack.MenuActivityFactory {
	private PagesManager pagesManager;
	
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
	public void onCreateLayout(Activity activity, RelativeLayout content) {
		// TODO: ConfigView already deprecated and located in org.mineprogramming.horizon.innercore.view.config
		// without any backport/backsupport implementation, what are you doing, Igor?
		// ConfigView config = new ConfigView(activity, "Instant Referrer", InstantConfig.getFile().getAbsolutePath());
		pagesManager = new PagesManager(content);
		ConfigPage config = new ConfigPage(pagesManager, "Instant Referrer", InstantConfig.getFile().getAbsolutePath());
		config.loadInfo(new InstantConfigInformation());
		pagesManager.reset(config);
	}
	
	@Override
	public boolean onBackPressed() {
		return pagesManager.navigateBack();
	}
}
