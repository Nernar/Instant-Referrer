package io.nernar.instant.cli;

import java.util.List;

import com.zhekasmirnov.horizon.launcher.ContextHolder;
import com.zhekasmirnov.horizon.launcher.pack.PackDirectory;
import com.zhekasmirnov.horizon.launcher.pack.PackHolder;
import com.zhekasmirnov.horizon.launcher.pack.PackStorage;
import com.zhekasmirnov.horizon.runtime.task.TaskManager;
import com.zhekasmirnov.horizon.runtime.task.TaskSequence;
import com.zhekasmirnov.horizon.util.LocaleUtils;

public class Main {
	public static void main(String[] args) {
		LocaleUtils.updateDefaultLanguage(null);
		IndependentPackLauncher packLauncher = new IndependentPackLauncher();
        PackStorage location = packLauncher.getPackStorage();
        List<PackDirectory> directories = location.getPackDirectories();
        if (directories.isEmpty()) {
            location.makeNewPackDirectory("Instant_Referrer");
        }
        final PackHolder packHolder = packLauncher.from(directories.get(0));
		if (packHolder == null) {
			return;
		}
		ContextHolder contextHolder = packHolder.getContextHolder();
        TaskManager taskManager = contextHolder.getTaskManager();
        taskManager.addTask(new TaskSequence.AnonymousTask() {
            @Override
            public String getDescription() {
                return "preparing for launch";
            }

            @Override
            public void run() {
                packHolder.prepareForLaunch();
                packHolder.getPack().launch(null, null, null);
            }
        });
	}
}
