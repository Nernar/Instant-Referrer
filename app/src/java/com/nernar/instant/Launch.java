package com.nernar.instant;

import android.app.Activity;
import android.widget.TextView;
import com.nernar.instant.prebuilt.InstantTranslation;
import com.nernar.instant.referrer.AlertDialogReferrer;
import com.zheka.horizon.R;
import com.zhekasmirnov.horizon.HorizonApplication;
import com.zhekasmirnov.horizon.activity.main.PackSelectorActivity;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.launcher.pack.PackHolder;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.StringUtils;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class Launch {
	public static Pack pendingInstantPack;
	
    public static void boot(HashMap sources) {
		try {
			tryToChangeDescription(InstantTranslation.translate("instant_wait"));
			PackHolder pending = findAnyAvailabledPackHolder();
			try {
				pendingInstantPack = pending.getPack();
			} catch (NullPointerException e) {
				if (AlertDialogReferrer.awaitDecision(InstantTranslation.translate("fail_launch"), InstantTranslation.translate("another_instance_running") + " " + InstantTranslation.translate("restart_to_launch"), Integer.valueOf(R.string.restart), Integer.valueOf(R.string.proceed)) == AlertDialogReferrer.DecisionStatus.ACCEPTED) {
					HorizonApplication.restart();
				}
			}
		} catch (Throwable ohno) {
			Logger.error("InstantReferrer", "Instant Referrer was failed initialization");
			Logger.error("InstantReferrer", StringUtils.getStackTrace(ohno));
			if (AlertDialogReferrer.awaitReport(ohno) == AlertDialogReferrer.DecisionStatus.CANCELLED) {
				throw new RuntimeException("Instant Referrer cancelled initialization, see log for details");
			}
		}
	}
	
	private static PackHolder findAnyAvailabledPackHolder() {
		try {
			Field loadedPackHolders = PackHolder.class.getDeclaredField("loadedPackHolders");
			loadedPackHolders.setAccessible(true);
			List<PackHolder> holders = (List<PackHolder>) loadedPackHolders.get(null);
			if (holders.size() > 1) throw new UnsupportedOperationException();
			return holders.size() > 0 ? holders.get(0) : null;
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static boolean tryToChangeDescription(final CharSequence description) {
		try {
			Activity activity = HorizonApplication.getTopRunningActivity();
			if (activity instanceof PackSelectorActivity) {
				final TextView placeholder = activity.findViewById(R.id.pack_selector_loading_text);
				placeholder.post(new Runnable() {
					
					@Override
					public void run() {
						placeholder.setText(description);
					}
				});
			}
			return true;
		} catch (Throwable e) {
			Logger.error("InstantReferrer", "Something went wrong when tried change description");
			Logger.error("InstantReferrer", StringUtils.getStackTrace(e));
			return false;
		}
	}
}
