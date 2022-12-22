package com.zhekasmirnov.horizon.modloader.repo;

import com.zhekasmirnov.horizon.modloader.ModContext;
import com.zhekasmirnov.horizon.modloader.mod.Mod;
import com.zhekasmirnov.horizon.modloader.repo.location.ModLocation;
import com.zhekasmirnov.horizon.modloader.repo.storage.ModRepository;
import com.zhekasmirnov.horizon.modloader.repo.storage.TemporaryStorage;
import com.zhekasmirnov.horizon.runtime.logger.EventLogger;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.runtime.task.Task;
import com.zhekasmirnov.horizon.runtime.task.TaskManager;
import com.zhekasmirnov.horizon.runtime.task.TaskSequence;
import com.zhekasmirnov.horizon.runtime.task.TaskWatcher;
import java.util.ArrayList;
import java.util.List;

public class ModList {
    private final ModContext context;
    private final TaskManager manager;
    private final TemporaryStorage storage;
    private Runnable interrupt = null;
    private final List<ModRepository> repositories = new ArrayList<>();
    private TaskWatcher.Callback taskCallback = new TaskWatcher.Callback() {
        @Override
        public void complete(Task task) {
        }

        @Override
        public boolean error(Task task, Throwable th) {
            ModList.this.manager.interruptTaskSequence(ModList.this.LAUNCH_SEQUENCE);
            ModList.this.context.getEventLogger().fault("UPDATE", "Failed to update mod list", th);
            if (ModList.this.interrupt != null) {
                ModList.this.interrupt.run();
                return true;
            }
            return true;
        }
    };
    private final TaskSequence REFRESH_SEQUENCE = new TaskSequence("mod_list_rebuild_and_launch_sequence", new TaskSequence.AnonymousTask() {
        @Override
        public String getDescription() {
            return "refreshing lists";
        }

        @Override
        public void run() {
            ModList.this.initializeContext();
        }
    });
    private final TaskSequence REBUILD_SEQUENCE = new TaskSequence("mod_list_rebuild_and_launch_sequence", this.REFRESH_SEQUENCE, new TaskSequence.AnonymousTask() {
        @Override
        public String getDescription() {
            return "build";
        }

        @Override
        public void run() {
            ModList.this.context.injectAll();
            ModList.this.context.buildAll();
            List<EventLogger.Message> messages = ModList.this.context.getEventLogger().getMessages(new EventLogger.Filter() {
                @Override
                public boolean filter(EventLogger.Message message) {
                    return message.type == EventLogger.MessageType.EXCEPTION || message.type == EventLogger.MessageType.FAULT;
                }
            });
            ModList.this.context.getEventLogger().clear();
            boolean hasAnyException = false;
            for (EventLogger.Message message : messages) {
                hasAnyException = true;
                Logger.error(message.tag, message.message);
            }
            if (!hasAnyException) {
                return;
            }
            ModList.this.manager.interruptTaskSequence(ModList.this.LAUNCH_SEQUENCE);
            if (ModList.this.interrupt != null) {
                ModList.this.interrupt.run();
            }
        }
    }, new TaskSequence.AnonymousTask() {
        @Override
        public String getDescription() {
            return "initialization";
        }

        @Override
        public void run() {
            ModList.this.context.initializeAll();
        }
    });
    private final TaskSequence LAUNCH_SEQUENCE = new TaskSequence("mod_list_rebuild_and_launch_sequence", this.REBUILD_SEQUENCE, new TaskSequence.AnonymousTask() {
        @Override
        public String getDescription() {
            return "launching";
        }

        @Override
        public void run() {
            ModList.this.context.launchAll();
        }
    });

    public ModList(ModContext modContext, TaskManager taskManager, TemporaryStorage temporaryStorage) {
        this.context = modContext;
        this.storage = temporaryStorage;
        this.manager = taskManager;
    }

    public void initializeContext() {
        this.context.clearModsAndContext();
        for (ModRepository modRepository : this.repositories) {
            modRepository.refresh(this.context.getEventLogger());
            for (ModLocation modLocation : modRepository.getAllLocations()) {
                addModByLocation(modLocation);
            }
        }
    }

    private void addModByLocation(ModLocation modLocation) {
        ModContext modContext = this.context;
        Mod mod = new Mod(modContext, modLocation.initializeInLocalStorage(this.storage, modContext.getEventLogger()));
        this.context.addMod(mod);
        for (ModLocation modLocation2 : mod.subModLocations) {
            addModByLocation(modLocation2);
        }
        mod.getGraphics();
    }

    public void addModRepository(ModRepository modRepository) {
        this.repositories.add(modRepository);
    }

    public void startRefreshTask(Runnable runnable) {
        this.manager.addTaskSequence(this.REFRESH_SEQUENCE, this.taskCallback, runnable);
    }

    public void startRebuildTask(Runnable runnable) {
        this.manager.addTaskSequence(this.REBUILD_SEQUENCE, runnable);
    }

    public void startLaunchTask(Runnable runnable, Runnable runnable2) {
        this.interrupt = runnable2;
        this.manager.addTaskSequence(this.LAUNCH_SEQUENCE, this.taskCallback, runnable);
    }
}
