package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ModPackContext {
    private static final ModPackContext instance = new ModPackContext();
    private final ModPackStorage storage = new ModPackStorage(new File(FileTools.DIR_PACK, "modpacks"), new File(FileTools.DIR_PACK, "modpacks-archive"), new File(FileTools.DIR_WORK));
    private ModPack currentModPack = null;
    private final List<ModPackSelectedListener> selectedListenerList = new ArrayList<>();
    private final List<ModPackDeselectedListener> deselectedListenerList = new ArrayList<>();
    private final LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private Thread taskThread = null;

    public interface ModPackDeselectedListener {
        void onDeselected(ModPack modPack);
    }

    public interface ModPackSelectedListener {
        void onSelected(ModPack modPack);
    }

    public static ModPackContext getInstance() {
        return instance;
    }

    private ModPackContext() {
    }

    public ModPackStorage getStorage() {
        return this.storage;
    }

    public ModPack getCurrentModPack() {
        return this.currentModPack;
    }

    public ModPackJsAdapter assureJsAdapter() {
        assurePackSelected();
        return getCurrentModPack().getJsAdapter();
    }

    public synchronized void setCurrentModPack(ModPack currentModPack) {
        if (this.currentModPack == currentModPack) {
            return;
        }
        if (this.currentModPack != null) {
            for (ModPackDeselectedListener listener : this.deselectedListenerList) {
                listener.onDeselected(this.currentModPack);
            }
        }
        this.currentModPack = currentModPack;
        currentModPack.reloadAndValidateManifest();
        if (this.currentModPack != null) {
            for (ModPackSelectedListener listener2 : this.selectedListenerList) {
                listener2.onSelected(this.currentModPack);
            }
        }
    }

    public void assurePackSelected() {
        if (getCurrentModPack() == null) {
            ModPackSelector.restoreSelected();
        }
    }

    public void addSelectedListener(ModPackSelectedListener listener) {
        this.selectedListenerList.add(listener);
    }

    public void addDeselectedListener(ModPackDeselectedListener listener) {
        this.deselectedListenerList.add(listener);
    }

    public void queueTask(Runnable task) {
        synchronized (this.taskQueue) {
            Thread thread = this.taskThread;
            if (thread == null) {
                this.taskThread = new Thread(new Runnable() {
                    @Override
                    public final void run() {
                        ModPackContext.lambda$queueTask$0(ModPackContext.this);
                    }
                });
                thread = this.taskThread;
                thread.start();
            }
            try {
                this.taskQueue.put(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void lambda$queueTask$0(ModPackContext modPackContext) {
        Runnable nextTask = null;
        while (true) {
            try {
                nextTask = modPackContext.taskQueue.poll(5000L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            }
            if (nextTask != null) {
                nextTask.run();
            } else {
                synchronized (modPackContext.taskQueue) {
                    modPackContext.taskThread = null;
                    break;
                }
            }
        }
    }
}
