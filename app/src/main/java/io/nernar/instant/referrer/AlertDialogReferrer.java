package io.nernar.instant.referrer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.appcompat.R;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;
import io.nernar.instant.prebuilt.InstantTranslation;
import com.zhekasmirnov.horizon.HorizonApplication;
import com.zhekasmirnov.horizon.util.StringUtils;

public class AlertDialogReferrer extends AlertDialog.Builder {
	
	public AlertDialogReferrer() {
		this(HorizonApplication.getTopRunningActivity());
	}
	
	public AlertDialogReferrer(int themeResId) {
		this(HorizonApplication.getTopRunningActivity(), themeResId);
	}
	
	public AlertDialogReferrer(Context context) {
		super(context);
	}
	
	public AlertDialogReferrer(Context context, int themeResId) {
		super(context, themeResId);
	}
	
	{
		setTitle(InstantTranslation.translate("instant_referrer"));
	}
	
	protected class CustomizeInterface implements DialogInterface.OnShowListener {
		protected AlertDialog dialog;
		
		public CustomizeInterface(AlertDialog dialog) {
			this.dialog = dialog;
		}
		
		public void onShow(DialogInterface watcher) {
			overrideDialogContent(watcher, dialog);
		}
	}
	
	public void overrideDialogContent(DialogInterface watcher, AlertDialog dialog) {
		TextView text = dialog.findViewById(android.R.id.message);
		if (text != null) {
			text.setTextSize(text.getTextSize() * textScaleModifier / 2);
			text.setTextIsSelectable(selectable);
		}
	}
	
	@Override
	public AlertDialog create() {
		AlertDialog dialog = super.create();
		dialog.setOnShowListener(new CustomizeInterface(dialog));
		if (widthScale > 0.0f && heightScale > 0.0f) {
			WindowManager manager = (WindowManager) getContext().getSystemService("window");
			Display display = manager.getDefaultDisplay();
			int width = (int) (display.getWidth() * widthScale);
			int height = (int) (display.getHeight() * heightScale);
			dialog.getWindow().setLayout(width, height);
		}
		return dialog;
	}
	
	protected float textScaleModifier = 1.0f;
	
	public void setTextScaleModifier(float scale) {
		textScaleModifier = scale;
	}
	
	protected float widthScale = 0.0f;
	protected float heightScale = 0.0f;
	
	public void setLayoutScale(float width, float height) {
		widthScale = width;
		heightScale = height;
	}
	
	protected boolean selectable = false;
	
	public void setMessageIsSelectable(boolean enabled) {
		selectable = enabled;
	}
	
	public static class Decision extends AlertDialogReferrer {
		
		public Decision() {
			super();
		}
		
		public Decision(int themeResId) {
			super(themeResId);
		}
		
		public Decision(Context context) {
			super(context);
		}
		
		public Decision(Context context, int themeResId) {
			super(context, themeResId);
		}
		
		{
			setPositiveButton(InstantTranslation.translate("yes"), null);
			setNegativeButton(InstantTranslation.translate("no"), null);
			setCancelable(false);
		}
	}
	
	public static class Report extends AlertDialogReferrer {
		
		public Report() {
			super(R.style.Theme_AppCompat_DialogWhenLarge);
		}
		
		public Report(int themeResId) {
			super(themeResId);
		}
		
		public Report(Context context) {
			super(context, R.style.Theme_AppCompat_DialogWhenLarge);
		}
		
		public Report(Context context, int themeResId) {
			super(context, themeResId);
		}
		
		{
			setPositiveButton(InstantTranslation.translate("proceed"), null);
			setTextScaleModifier(0.75f);
			setMessageIsSelectable(true);
			setLayoutScale(0.7f, 0.8f);
		}
		
		@Override
		public void overrideDialogContent(DialogInterface watcher, AlertDialog dialog) {
			TextView text = dialog.findViewById(android.R.id.message);
			if (text != null) {
				text.setTypeface(Typeface.MONOSPACE);
			}
			super.overrideDialogContent(watcher, dialog);
		}
	}
	
	public static enum DecisionStatus {
		WAITING,
		CANCELLED,
		DECLINED,
		ACCEPTED;
		
		public static interface Lock {
			
			public boolean isLocked();
			
			public DecisionStatus getStatus();
			
			public void unlock(DecisionStatus status);
		}
		
		protected static abstract class LockRunnable implements Runnable, Lock {
			protected DecisionStatus status = WAITING;
			protected boolean locked = true;
			
			public boolean isLocked() {
				return this.locked;
			}
			
			public DecisionStatus getStatus() {
				return this.status;
			}
			
			public void unlock(DecisionStatus status) {
				this.status = status;
				this.locked = false;
			}
		}
	}
	
	protected static DecisionStatus awaitInterface(Activity context, DecisionStatus.LockRunnable runnable) {
		if (context == null) {
			return DecisionStatus.CANCELLED;
		}
		context.runOnUiThread(runnable);
		while (runnable.isLocked()) {
			Thread.yield();
		}
		return runnable.getStatus();
	}
	
	public static AlertDialogReferrer createTipMessage(Object title, Object message, Object accept) {
		AlertDialogReferrer dialog = new AlertDialogReferrer();
		if (title instanceof CharSequence) {
			dialog.setTitle((CharSequence) title);
		} else if (title instanceof Integer) {
			dialog.setTitle(((Integer) title).intValue());
		}
		if (message instanceof CharSequence) {
			dialog.setMessage((CharSequence) message);
		} else if (message instanceof Integer) {
			dialog.setMessage(((Integer) message).intValue());
		}
		if (accept instanceof CharSequence) {
			dialog.setPositiveButton((CharSequence) accept, null);
		} else if (accept instanceof Integer) {
			dialog.setPositiveButton(((Integer) accept).intValue(), null);
		}
		return dialog;
	}
	
	public static void showTipMessage(Object title, Object message, Object accept) {
		createTipMessage(title, message, accept).create().show();
	}
	
	public static void showTipMessage(Object title, Object message) {
		showTipMessage(title, message, null);
	}
	
	public static void showTipMessage(Object message) {
		showTipMessage(null, message);
	}
	
	public static DecisionStatus awaitTipMessage(Activity context, final Object title, final Object message, final Object accept, final Runnable post) {
		return awaitInterface(context, new DecisionStatus.LockRunnable() {
			public void run() {
				try {
					AlertDialog window = createTipMessage(title, message, accept).create();
					window.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface watcher) {
							if (isLocked()) {
								unlock(DecisionStatus.ACCEPTED);
							}
						}
					});
					window.show();
					if (post != null) {
						post.run();
					}
				} catch (NullPointerException e) {
					unlock(DecisionStatus.CANCELLED);
				}
			}
		});
	}
	
	public static DecisionStatus awaitTipMessage(Object title, Object message, Object accept, Runnable post) {
		return awaitTipMessage(HorizonApplication.getTopRunningActivity(), title, message, accept, post);
	}
	
	public static DecisionStatus awaitTipMessage(Object title, Object message, Object accept) {
		return awaitTipMessage(title, message, accept, null);
	}
	
	public static DecisionStatus awaitTipMessage(Object title, Object message) {
		return awaitTipMessage(title, message, null);
	}
	
	public static DecisionStatus awaitTipMessage(Object message) {
		return awaitTipMessage(null, message);
	}
	
	public static Decision createDecision(Object title, Object message, Object accept, Object decline, DialogInterface.OnClickListener action) {
		Decision dialog = new Decision();
		if (title instanceof CharSequence) {
			dialog.setTitle((CharSequence) title);
		} else if (title instanceof Integer) {
			dialog.setTitle(((Integer) title).intValue());
		}
		if (message instanceof CharSequence) {
			dialog.setMessage((CharSequence) message);
		} else if (message instanceof Integer) {
			dialog.setMessage(((Integer) message).intValue());
		}
		if (accept instanceof CharSequence) {
			dialog.setPositiveButton((CharSequence) accept, action);
		} else if (accept instanceof Integer) {
			dialog.setPositiveButton(((Integer) accept).intValue(), action);
		} else if (action != null) {
			dialog.setPositiveButton(InstantTranslation.translate("yes"), action);
		}
		if (decline instanceof CharSequence) {
			dialog.setNegativeButton((CharSequence) decline, null);
		} else if (decline instanceof Integer) {
			dialog.setNegativeButton(((Integer) decline).intValue(), null);
		}
		return dialog;
	}
	
	public static void showDecision(Object title, Object message, Object accept, Object decline, DialogInterface.OnClickListener action) {
		createDecision(title, message, accept, decline, action).create().show();
	}
	
	public static void showDecision(Object title, Object message, Object accept, DialogInterface.OnClickListener action) {
		showDecision(title, message, accept, null, action);
	}
	
	public static void showDecision(Object title, Object message, DialogInterface.OnClickListener action) {
		showDecision(title, message, null, action);
	}
	
	public static void showDecision(Object title, Object message, Object accept) {
		showDecision(title, message, accept, null);
	}
	
	public static void showDecision(Object message, DialogInterface.OnClickListener action) {
		showDecision(null, message, action);
	}
	
	public static void showDecision(Object message) {
		showDecision(message, null);
	}
	
	public static DecisionStatus awaitDecision(Activity context, final Object title, final Object message, final Object accept, final Object decline, final Runnable post) {
		return awaitInterface(context, new DecisionStatus.LockRunnable() {
			public void run() {
				try {
					Decision dialog = createDecision(title, message, accept, decline, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface watcher, int index) {
							unlock(DecisionStatus.ACCEPTED);
						}
					});
					AlertDialog window = dialog.create();
					window.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface watcher) {
							if (isLocked()) {
								unlock(DecisionStatus.DECLINED);
							}
						}
					});
					window.show();
					if (post != null) {
						post.run();
					}
				} catch (NullPointerException e) {
					unlock(DecisionStatus.CANCELLED);
				}
			}
		});
	}
	
	public static DecisionStatus awaitDecision(Object title, Object message, Object accept, Object decline, Runnable post) {
		return awaitDecision(HorizonApplication.getTopRunningActivity(), title, message, accept, decline, post);
	}
	
	public static DecisionStatus awaitDecision(Object title, Object message, Object accept, Object decline) {
		return awaitDecision(title, message, accept, decline, null);
	}
	
	public static DecisionStatus awaitDecision(Object title, Object message, Runnable post) {
		return awaitDecision(title, message, null, null, post);
	}
	
	public static DecisionStatus awaitDecision(Object message, Runnable post) {
		return awaitDecision(null, message, post);
	}
	
	public static DecisionStatus awaitDecision(Object message) {
		return awaitDecision(message, null);
	}
	
	public static Report createReport(Object title, Object message) {
		Report dialog = new Report();
		if (title instanceof CharSequence) {
			dialog.setTitle((CharSequence) title);
		} else if (title instanceof Integer) {
			dialog.setTitle(((Integer) title).intValue());
		}
		if (message instanceof CharSequence) {
			dialog.setMessage((CharSequence) message);
		} else if (message instanceof Integer) {
			dialog.setMessage(((Integer) message).intValue());
		} else if (message instanceof Throwable) {
			dialog.setMessage(StringUtils.getStackTrace((Throwable) message));
		}
		return dialog;
	}
	
	public static void showReport(Object title, Object message) {
		createReport(title, message).create().show();
	}
	
	public static void showReport(Object message) {
		showReport(null, message);
	}
	
	public static DecisionStatus awaitReport(Activity context, final Object title, final Object message, final Runnable post) {
		return awaitInterface(context, new DecisionStatus.LockRunnable() {
			public void run() {
				try {
					AlertDialog window = createReport(title, message).create();
					window.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface watcher) {
							if (isLocked()) {
								unlock(DecisionStatus.ACCEPTED);
							}
						}
					});
					window.show();
					if (post != null) {
						post.run();
					}
				} catch (NullPointerException e) {
					unlock(DecisionStatus.CANCELLED);
				}
			}
		});
	}
	
	public static DecisionStatus awaitReport(Object title, Object message, Runnable post) {
		return awaitReport(HorizonApplication.getTopRunningActivity(), title, message, post);
	}
	
	public static DecisionStatus awaitReport(Object title, Object message) {
		return awaitReport(title, message, null);
	}
	
	public static DecisionStatus awaitReport(Object message) {
		return awaitReport(null, message);
	}
}
