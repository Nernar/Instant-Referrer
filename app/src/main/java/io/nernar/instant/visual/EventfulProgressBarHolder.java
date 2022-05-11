package io.nernar.instant.visual;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.zhekasmirnov.horizon.runtime.task.TaskManager;

public class EventfulProgressBarHolder implements TaskManager.StateCallback {
	private final View progressBar;
	private final TextView label;
	private final Activity context;
	private int maximumTasks = 2;
	private boolean isVisible;
	private String description;
	
	public EventfulProgressBarHolder(Activity context, View progressBar, TextView label) {
		if (context == null || progressBar == null) {
			throw new NullPointerException();
		}
		this.progressBar = progressBar;
		this.label = label;
		this.context = context;
	}
	
	public EventfulProgressBarHolder(Activity context, View progressBar, TextView label, int maximumTasks) {
		this(context, progressBar, label);
		setMaximumVisibleTasks(maximumTasks);
	}
	
	public EventfulProgressBarHolder(Activity context, View progressBar) {
		this(context, progressBar, null);
	}
	
	public EventfulProgressBarHolder(Activity context, View progressBar, int maximumTasks) {
		this(context, progressBar, null, maximumTasks);
	}
	
	protected Runnable queue = new Runnable() {
		
		@Override
		public void run() {
			progressBar.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			if (label != null) {
				label.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
				label.setText(description != null ? description : "...");
			}
		}
	};
	
	@Override
	public void onStateUpdated(TaskManager manager, TaskManager.ThreadHolder holder) {
		isVisible = holder.isRunning() || manager.getNumRunningThreads() > 0;
		if (label != null) {
			description = manager.getFormattedTaskDescriptions(maximumTasks);
		}
		progressBar.removeCallbacks(queue);
		progressBar.post(queue);
		context.runOnUiThread(queue);
	}
	
	public int getMaximumVisibleTasks() {
		return maximumTasks;
	}
	
	public void setMaximumVisibleTasks(int count) {
		maximumTasks = count;
	}
	
	public String getCurrentlyDescription() {
		return description;
	}
	
	public boolean isVisible() {
		return isVisible;
	}
}
