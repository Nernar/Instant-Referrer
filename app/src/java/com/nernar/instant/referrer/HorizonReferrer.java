package com.nernar.instant.referrer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.button.MaterialButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nernar.instant.function.AcquiredArrayList;
import com.nernar.instant.function.Shuffles;
import com.nernar.instant.prebuilt.InstantTranslation;
import com.nernar.instant.visual.EventfulProgressBarHolder;
import com.zheka.horizon.R;
import com.zhekasmirnov.horizon.activity.main.AnimatedBitmapCollectionDrawable;
import com.zhekasmirnov.horizon.activity.util.CustomMeasuredLayout;
import com.zhekasmirnov.horizon.launcher.ContextHolder;
import com.zhekasmirnov.horizon.launcher.ads.AdContainer;
import com.zhekasmirnov.horizon.launcher.ads.AdDistributionModel;
import com.zhekasmirnov.horizon.launcher.ads.AdDomain;
import com.zhekasmirnov.horizon.launcher.ads.AdsManager;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.launcher.pack.PackGraphics;
import com.zhekasmirnov.horizon.launcher.pack.PackHolder;
import com.zhekasmirnov.horizon.runtime.task.TaskManager;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class HorizonReferrer {
	
	public static void prepatchHorizonActivity(ViewGroup layout, View button) {
		try {
			// No launch button (special april version) patch
			if (!InstantReferrer.HAD_LAUNCH_BUTTON) {
				removeLaunchButton(layout, button);
				button = null;
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_remove_launch_button"), th);
		}
		InstantReferrer.Horizon horizon = InstantReferrer.Horizon.get();
		PackHolder packHolder = horizon.getPackHolder();
		try {
			// Informative state progress patch
			if (InstantReferrer.hadInformativeProgress()) {
				replaceAndAcquireInformativeProgress(layout, packHolder, horizon);
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_setup_informative_progress"), th);
		}
		ViewGroup parent = (ViewGroup) layout.getParent();
		try {
			// Immersive translucent system bars window patch
			if (InstantReferrer.hadImmersiveMode()) {
				makeImmersiveAndTranslucent(parent, horizon);
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_make_immersive"), th);
		}
		RecyclerView recycler = parent.findViewById(R.id.menu_recycler_view);
		try {
			// Restyle recycler view to improved visual patch
			if (InstantReferrer.hadMenuCardPadding()) {
				restyleWithPaddingRecyclerView(recycler);
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_restyle_padding_recycler"), th);
		}
		CustomMeasuredLayout measured = (CustomMeasuredLayout) recycler.getParent();
		try {
			// Changed recycler view to bottom gravity patch
			if (InstantReferrer.hadMenuChangedGravity()) {
				changeRecyclerViewGravity(recycler, measured);
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_change_recycler_gravity"), th);
		}
		try {
			// Measured recycler view in system layout patch
			if (InstantReferrer.getMenuWidthModifier() != 1.0d) {
				measureRecyclerViewLayout(measured);
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_measure_recycler_layout"), th);
		}
		Pack pack = packHolder.getPack();
		try {
			// Auto-launch switch behold bottom patch
			if (button instanceof MaterialButton) {
				// addAutoLaunchButton(layout, horizon, pack, (MaterialButton) button);
			} else {
				addAutoLaunchButton(layout, horizon, pack, null);
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_add_auto_launch_button"), th);
		}
	}
	
	public static void removeLaunchButton(ViewGroup layout, View button) {
		layout.removeView(button);
	}
	
	public static void replaceAndAcquireInformativeProgress(ViewGroup layout, PackHolder packHolder, InstantReferrer.Horizon horizon)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		ContextHolder context = packHolder.getContextHolder();
		TaskManager manager = context.getTaskManager();
		View progressBar = layout.findViewById(R.id.main_menu_progress_bar);
		TextView label = layout.findViewById(R.id.main_menu_progress_label);
		Field stateCallbacks = manager.getClass().getDeclaredField("stateCallbacks");
		stateCallbacks.setAccessible(true);
		AcquiredArrayList<TaskManager.StateCallback> collection = new AcquiredArrayList<>();
		stateCallbacks.set(manager, collection);
		manager.addStateCallback(new EventfulProgressBarHolder(horizon.getSingleton(), progressBar, label));
		collection.acquire(); // Does not allow to add second (original) callback
	}
	
	public static void makeImmersiveAndTranslucent(ViewGroup parent, InstantReferrer.Horizon horizon) {
		Window window = horizon.getSingleton().getWindow();
		if (Build.VERSION.SDK_INT >= 21) {
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		}
		parent.setFitsSystemWindows(true);
	}
	
	public static void restyleWithPaddingContainer(View container) {
		container = ((ViewGroup) container).getChildAt(0);
		if (container instanceof CardView) {
			CardView content = (CardView) container;
			int dimen = container.getResources().getDimensionPixelSize(R.dimen.tooltip_margin);
			TextView packTitle = container.findViewById(R.id.main_menu_pack_title);
			if (packTitle == null) {
				content.setContentPadding(dimen, dimen, dimen, dimen);
			} else {
				ViewGroup packInformationContainer = (ViewGroup) packTitle.getParent();
				ViewGroup.MarginLayoutParams informationParams = (ViewGroup.MarginLayoutParams) packInformationContainer.getLayoutParams();
				informationParams.setMargins(dimen * 2, dimen, dimen * 2, 0);
				packInformationContainer.setLayoutParams(informationParams);
				ViewGroup categoryContainer = container.findViewById(R.id.main_menu_categories_layout);
				if (categoryContainer != null) {
					int relativeContainerPadding = 0;
					for (int i = 0; i < categoryContainer.getChildCount(); i++) {
						View categoryCard = categoryContainer.getChildAt(i);
						ViewGroup.MarginLayoutParams categoryParams = (ViewGroup.MarginLayoutParams) categoryCard.getLayoutParams();
						relativeContainerPadding = categoryParams.rightMargin = categoryParams.leftMargin = categoryParams.rightMargin * 2;
						categoryParams.bottomMargin = categoryParams.topMargin = 0;
						categoryCard.setLayoutParams(categoryParams);
						categoryCard.setBackground(null);
					}
					categoryContainer.setPadding(0, relativeContainerPadding, 0, relativeContainerPadding);
				}
			}
			content.setRadius(content.getRadius() * InstantReferrer.getMenuCardRadiusModifier());
		}
	}
	
	public static void restyleWithPaddingRecyclerView(RecyclerView recycler) {
		recycler.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
			
			@Override
			public void onChildViewAttachedToWindow(View container) {
				container = ((ViewGroup) container).getChildAt(0);
				if (container != null && container.getTag() != "alreadyHadPadding") {
					container.setTag("alreadyHadPadding");
					restyleWithPaddingContainer(container);
				}
			}
			
			@Override
			public void onChildViewDetachedFromWindow(View p1) {}
		});
	}
	
	public static void changeRecyclerViewGravity(RecyclerView recycler, CustomMeasuredLayout measured) {
		recycler.setLayoutParams(new CustomMeasuredLayout.LayoutParams
			(CustomMeasuredLayout.LayoutParams.MATCH_PARENT, CustomMeasuredLayout.LayoutParams.WRAP_CONTENT));
		if (measured instanceof LinearLayout) {
			measured.setGravity(Gravity.BOTTOM);
		}
	}
	
	public static void measureRecyclerViewLayout(CustomMeasuredLayout measured) {
		ViewGroup.LayoutParams params = measured.getLayoutParams();
		params.width *= InstantReferrer.getMenuWidthModifier();
		measured.setLayoutParams(params);
	}
	
	public static void switchAutoLaunchButton(View view, InstantReferrer.Horizon horizon, Pack pack, MaterialButton button) {
		view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.image_click));
		InstantReferrer.setAutoLaunchEnabled(!InstantReferrer.hadAutoLaunchEnabled());
		((ImageView) view).setImageResource(InstantReferrer.hadAutoLaunchEnabled() ? R.drawable.pack_exit : R.drawable.pack_exit);
		if (button != null && InstantReferrer.hadAbortAbility()) {
			Intent intent = horizon.getSingleton().getIntent();
			if (intent.getBooleanExtra("autoLaunchFlag", false)) {
				intent.removeExtra("autoLaunchFlag");
				abortLaunchIfRequired(horizon, pack, button);
			}
		}
	}
	
	public static void addAutoLaunchButton(ViewGroup layout, final InstantReferrer.Horizon horizon, final Pack pack, final MaterialButton button) {
		ImageView leaveButton = layout.findViewById(R.id.main_menu_exit_pack);
		ViewGroup.LayoutParams leaveButtonParams = leaveButton.getLayoutParams();
		FrameLayout leaveFrame = (FrameLayout) leaveButton.getParent();
		ViewGroup.MarginLayoutParams leaveFrameParams = (ViewGroup.MarginLayoutParams) leaveFrame.getLayoutParams();
		FrameLayout.LayoutParams launchButtonParams = new FrameLayout.LayoutParams
			(leaveButtonParams.width, leaveButtonParams.height);
		ImageView launchButton = new ImageView(layout.getContext(), null, android.R.attr.borderlessButtonStyle);
		if (InstantReferrer.hadAutoLaunchEnabled()) {
			Intent intent = horizon.getSingleton().getIntent();
			intent.putExtra("autoLaunchFlag", true);
			if (button != null) launchPackIfRequired(horizon, button);
			launchButton.setImageResource(R.drawable.pack_exit);
		} else {
			if (InstantReferrer.hadAutoLaunchOverride()) {
				Intent intent = horizon.getSingleton().getIntent();
				intent.removeExtra("autoLaunchFlag");
			}
			launchButton.setImageResource(R.drawable.pack_exit);
		}
		launchButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				switchAutoLaunchButton(view, horizon, pack, button);
			}
		});
		RelativeLayout.LayoutParams launchFrameParams = new RelativeLayout.LayoutParams
			(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		launchFrameParams.setMarginEnd(leaveFrameParams.getMarginStart());
		launchFrameParams.bottomMargin = leaveFrameParams.bottomMargin;
		launchFrameParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		launchFrameParams.addRule(RelativeLayout.ALIGN_PARENT_END);
		FrameLayout launchFrame = new FrameLayout(layout.getContext());
		launchFrame.addView(launchButton, launchButtonParams);
		RelativeLayout frameLayout = (RelativeLayout) leaveFrame.getParent();
		frameLayout.addView(launchFrame, launchFrameParams);
	}
	
	public static void overrideLaunchedHorizonActivity(Pack pack, List<Drawable> drawables) {
		InstantReferrer.Horizon horizon = InstantReferrer.Horizon.get();
		try {
			// Cancellation launch (without leave) patch
			if (InstantReferrer.hadAbortAbility()) {
				patchAbortAbility(pack, horizon, pack.getContextHolder().getTaskManager());
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_patch_abort_ability"), th);
		}
		RecyclerView recycler = horizon.getSingleton().findViewById(R.id.menu_recycler_view);
		try {
			// No 1px divider decoration in immersive mode
			if (InstantReferrer.hadImmersiveMode()) {
				removeRecyclerViewDecoration(recycler);
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_remove_recycler_decoration"), th);
		}
		PackHolder packHolder = horizon.getPackHolder();
		try {
			// Shuffled background arts moved to fullscreen patch
			Drawable drawable = createConfiguredAnimatedBackgroundDrawable(packHolder);
			try {
				if (InstantReferrer.hadFullscreenBackground()) {
					measureAndClipBackground(horizon, drawable);
					drawables.add(new ColorDrawable(Color.TRANSPARENT));
				}
			} catch (Throwable th) {
				drawables.add(drawable);
				throw th;
			}
		} catch (Throwable th) {
			AlertDialogReferrer.showReport(InstantTranslation.translate("fail_measure_and_clip_background"), th);
		}
	}
	
	public static boolean launchPackIfRequired(InstantReferrer.Horizon horizon, MaterialButton button) {
		button.setText(InstantTranslation.translate("abort"));
		if (horizon.isLaunching()) {
			Toast.makeText(horizon.getSingleton(), InstantTranslation.translate("hold_to_abort"), Toast.LENGTH_LONG).show();
			return false;
		}
		horizon.launchPack();
		return true;
	}
	
	public static boolean abortLaunchIfRequired(InstantReferrer.Horizon horizon, Pack pack, MaterialButton button) {
		button.setText(R.string.play_button_text);
		if (horizon.isLaunching()) {
			InstantReferrer.Repository repository = InstantReferrer.Repository.get(pack);
			if (repository != null) {
				repository.abortLaunch(pack);
				return true;
			}
			Toast.makeText(horizon.getSingleton(), InstantTranslation.translate("abort_not_supported"), Toast.LENGTH_LONG).show();
		}
		return false;
	}
	
	public static void patchAbortAbility(final Pack pack, final InstantReferrer.Horizon horizon, final TaskManager taskManager) {
		View launchLayout = horizon.getSingleton().findViewById(R.id.horizon_activity_play_button_layout);
		View launchButton = launchLayout.findViewById(R.id.main_menu_launch_button);
		if (launchButton == null) launchButton = launchLayout.findViewById(0);
		if (launchButton != null) {
			launchButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View view) {
					launchPackIfRequired(horizon, (MaterialButton) view);
				}
			});
			launchButton.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View view) {
					return abortLaunchIfRequired(horizon, pack, (MaterialButton) view);
				}
			});
		}
	}
	
	public static void removeRecyclerViewDecoration(RecyclerView recycler) {
		if (recycler.getItemDecorationCount() > 0) {
			recycler.removeItemDecorationAt(0);
		}
	}
	
	public static void measureAndClipBackground(InstantReferrer.Horizon horizon, Drawable drawable) {
		View view = horizon.getSingleton().findViewById(R.id.main_menu_background);
		ViewGroup parent = (ViewGroup) view.getParent();
		ViewGroup container = (ViewGroup) parent.getParent();
		view.setVisibility(View.GONE);
		container.setBackgroundDrawable(drawable);
	}
	
	public static Drawable createConfiguredAnimatedBackgroundDrawable(PackHolder packHolder) {
		return createConfiguredAnimatedBackgroundDrawable(packHolder, "background");
	}
	
	private static Collection<Bitmap> getGroupWithBrightnessThreshold(PackHolder holder, String group, float threshold) {
		PackGraphics graphics = holder.getGraphics();
		try {
			Method getGroupWithBrightnessThreshold = graphics.getClass().getMethod("getGroupWithBrightnessThreshold", String.class, Float.TYPE);
			return (Collection<Bitmap>) getGroupWithBrightnessThreshold.invoke(graphics, group, threshold);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			return graphics.getGroup(group);
		}
	}
	
	public static Drawable createConfiguredAnimatedBackgroundDrawable(PackHolder packHolder, String category) {
		Collection<Bitmap> allBackgrounds = getGroupWithBrightnessThreshold(packHolder, category,
			1.0f - (float) InstantReferrer.getBackgroundBrightness());
		return createConfiguredAnimatedBackgroundDrawable(packHolder, allBackgrounds);
	}
	
	public static Drawable createConfiguredAnimatedBackgroundDrawable(PackHolder packHolder, Collection<Bitmap> allBackgrounds) {
		if (allBackgrounds != null && allBackgrounds.size() > 0) {
			if (InstantReferrer.hadShuffledBackground()) {
				Shuffles.shuffle(allBackgrounds);
			}
			AnimatedBitmapCollectionDrawable drawable = new AnimatedBitmapCollectionDrawable(allBackgrounds,
				InstantReferrer.getBackgroundDuration(), InstantReferrer.getBackgroundDuration() / 12);
			drawable.setAnimationParameters(3.5E-4f, 50.0f, 25.0f, InstantReferrer.hadBackgroundInterpolation());
			return drawable;
		}
		return null;
	}
	
	public static void denyAnyDeveloperSupport(AdsManager manager, AdDistributionModel model) {
		// Acquire exiting nodes to remove
		try {
			if (model == null || model.getRootName() == "root") {
				model = manager.getDistributionModel();
			}
			model.removeDistributionNodes("pack-dev");
			model.removeDistributionNodes("horizon-dev");
		} catch (Throwable zhekaCopyright) {
			AlertDialogReferrer.awaitReport(InstantTranslation.translate("fail_remove_distribution_nodes"), zhekaCopyright);
		}
		// Finish opened ad activities and close requests
		try {
			manager.closeInterstitialAds();
			manager.closeAllRequests();
		} catch (Throwable zhekaReserved) {
			AlertDialogReferrer.awaitReport(InstantTranslation.translate("fail_close_advertisement_requests"), zhekaReserved);
		}
		// Cleanup exiting containers as well
		try {
			Field illPatch = manager.getClass().getDeclaredField("currentDomain");
			illPatch.setAccessible(true);
			try {
				AdDomain domain = (AdDomain) illPatch.get(manager);
				if (domain == null) {
					throw new NullPointerException();
				}
				List<AdContainer> containers = domain.getContainers();
				if (containers == null || containers.size() == 0) {
					throw new IllegalStateException();
				}
				containers.clear();
				containers = domain.getContainers();
				if (containers != null && containers.size() > 0) {
					throw new IllegalStateException();
				}
			} catch (IllegalStateException editable) {
				illPatch.set(manager, null); // Why do you does not update domain if it is null
			}
		} catch (Throwable zhekaMegamind) {
			AlertDialogReferrer.awaitReport(InstantTranslation.translate("fail_cleanup_exiting_containers"), zhekaMegamind);
		}
	}
}
