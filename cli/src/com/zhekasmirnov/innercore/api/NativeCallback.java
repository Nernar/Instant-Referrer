package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.apparatus.adapter.innercore.game.Minecraft;
import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockBreakResult;
import com.zhekasmirnov.apparatus.minecraft.version.VanillaIdConversionMap;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import com.zhekasmirnov.innercore.api.runtime.AsyncModLauncher;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import com.zhekasmirnov.innercore.api.runtime.TPSMeter;
import com.zhekasmirnov.innercore.api.runtime.TickExecutor;
import com.zhekasmirnov.innercore.api.runtime.TickManager;
import com.zhekasmirnov.innercore.api.runtime.Updatable;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaverHandler;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.UIUtils;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NativeCallback {
    public static final String LOGGER_TAG = "INNERCORE-CALLBACK";
    private static BlockBreakResult currentBlockBreakResultOverride;
    private static boolean isDestroyBlockCallbackInProgress;
    private static boolean isFirstLocalTick;
    private static boolean isLocalTickDisabledDueToError;
    private static final Set<Long> allEntities = new HashSet<>(256);
    private static boolean isFirstServerTick = false;
    private static int currentPlayerDimension = 0;
    private static boolean isLevelDisplayed = false;

    @SuppressWarnings("unused")
    private static void assureCopyright(Object context) {
        throw new UnsupportedOperationException();
    }

    public static String getStringParam(String str) {
        throw new UnsupportedOperationException();
    }

    static {
        new TPSMeter("main-thread", 20, 2000);
        new TPSMeter("mod-thread", 20, 2000);
        isLocalTickDisabledDueToError = false;
        isFirstLocalTick = true;
        currentBlockBreakResultOverride = null;
    }

    public static void onCallbackExceptionOccurred(String callback, Throwable error) {
        error.printStackTrace();
        ICLog.e("INNERCORE-CALLBACK", "uncaught error occurred in callback " + callback, error);
    }

    public static void onNativeGuiLoaded() {
    }

    public static void onCopyrightCheck() {
        Logger.info("INNERCORE", "Inner Core is developed fully and only by zheka_smirnov (zheka2304), all rights are reserved.");
    }

    public static void onToastRequested() {
        throw new UnsupportedOperationException();
    }

    public static void onDialogRequested() {
        throw new UnsupportedOperationException();
    }

    public static void onDebugLog() {
        ICLog.d("NATIVE-DEBUG", getStringParam("_log"));
        ICLog.flush();
    }

    public static Collection<Long> getAllEntities() {
        return allEntities;
    }

    @SuppressWarnings("unused")
    private static void showExperimentalWorkbenchWarning() {
        throw new UnsupportedOperationException();
    }

    public static void onFinalInitStarted() {
        EngineConfig.reload();
        if (EngineConfig.isDeveloperMode()) {
            ICLog.i("NativeProfiling", "developer mode is enabled - turning on native callback profiling and signal handling");
        } else {
            ICLog.i("NativeProfiling", "developer mode is disabled - turning off native callback profiling and signal handling");
        }
        NameTranslation.loadBuiltinTranslations();
    }

    public static void onFinalInitComplete() {
        UIUtils.initialize(UIUtils.getContext());
        AsyncModLauncher modLauncher = new AsyncModLauncher();
        if (InnerCoreConfig.getBool("disable_loading_screen")) {
            modLauncher.launchModsInCurrentThread();
        } else {
            modLauncher.launchModsInThread();
        }
    }

    public static void onMinecraftAppSuspended() {
        Callback.invokeAPICallback("AppSuspended", new Object[0]);
    }

    public static void onKeyEventDispatched(int key, int state) {
        Callback.invokeAPICallback("SystemKeyEventDispatched", Integer.valueOf(key), Integer.valueOf(state));
        if (key == 0 && state == 1) {
            Callback.invokeAPICallback("NavigationBackPressed", new Object[0]);
        }
    }

    public static void onLocalServerStarted() {
        isFirstServerTick = true;
        VanillaIdConversionMap.getSingleton().reloadFromAssets();
        AdaptedScriptAPI.IDRegistry.rebuildNetworkIdMap();
        String worldName = getStringParam("world_name");
        String worldDir = getStringParam("world_dir");
        String str = FileTools.DIR_PACK;
        File worldDirFile = new File(str, "worlds/" + worldDir);
        LevelInfo.onEnter(worldName, worldDir);
        ModLoader.instance.addResourceAndBehaviorPacksInWorld(worldDirFile);
        WorldDataSaverHandler.getInstance().onLevelSelected(worldDirFile);
        MainThreadQueue.localThread.clearQueue();
        MainThreadQueue.serverThread.clearQueue();
        Updatable.cleanUpAll();
        NameTranslation.refresh(false);
        Minecraft.onLevelSelected();
        Callback.invokeAPICallback("LevelSelected", worldName, worldDir);
    }

    public static void onLevelCreated() {
        isFirstServerTick = true;
        Callback.invokeAPICallback("LevelCreated", new Object[0]);
    }

    public static boolean isLevelDisplayed() {
        return isLevelDisplayed;
    }

    public static void onLevelLoaded() {
        isLevelDisplayed = true;
        Callback.invokeAPICallback("LevelDisplayed", new Object[0]);
        Minecraft.onLevelDisplayed();
    }

    private static void setupThreadPriorityFromConfig() {
        TickExecutor.getInstance().setAdditionalThreadCount(InnerCoreConfig.getInt("threading.additional_thread_count", 0));
        TickExecutor.getInstance().setAdditionalThreadPriority(InnerCoreConfig.getInt("threading.additional_thread_priority", 12) / 4);
    }

    private static void onLevelPostLoaded(boolean isServer) {
        MainThreadQueue.localThread.clearQueue();
        MainThreadQueue.serverThread.clearQueue();
        Callback.invokeAPICallback("LevelPreLoaded", Boolean.valueOf(isServer));
        if (isServer) {
            Callback.invokeAPICallback("ServerLevelPreLoaded", new Object[0]);
        } else {
            Callback.invokeAPICallback("RemoteLevelPreLoaded", new Object[0]);
        }
        WorldDataSaverHandler.getInstance().onLevelLoading();
        NameTranslation.refresh(true);
        LevelInfo.onLoaded();
        Callback.invokeAPICallback("LevelLoaded", Boolean.valueOf(isServer));
        if (isServer) {
            Callback.invokeAPICallback("ServerLevelLoaded", new Object[0]);
        } else {
            Callback.invokeAPICallback("RemoteLevelLoaded", new Object[0]);
        }
        setupThreadPriorityFromConfig();
        TickManager.setupAndStart();
    }

    public static void onDimensionChanged(int current, int last) {
        if (current != last) {
            Callback.invokeAPICallback("DimensionUnloaded", Integer.valueOf(last));
        }
        currentPlayerDimension = current;
        Callback.invokeAPICallback("DimensionLoaded", Integer.valueOf(current), Integer.valueOf(last));
        ICLog.d("INNERCORE-CALLBACK", "player entered dimension " + current + " from " + last);
    }

    public static void onGameStopped(boolean isServer) {
        if (isServer) {
            TickManager.clearLastFatalError();
            TickManager.stop();
        } else {
            isFirstLocalTick = true;
            isLocalTickDisabledDueToError = false;
            isLevelDisplayed = false;
        }
        boolean callLegacyCallback = isServer == (Minecraft.getLastWorldState() == Minecraft.GameState.HOST_WORLD);
        StringBuilder sb = new StringBuilder();
        sb.append("Shutting down ");
        sb.append(isServer ? "server" : "client");
        sb.append(" level, world state: ");
        sb.append(Minecraft.getLastWorldState());
        sb.append(callLegacyCallback ? " (legacy callback will be called here)" : "");
        ICLog.i("INNERCORE-CALLBACK", sb.toString());
        if (callLegacyCallback) {
            Callback.invokeAPICallback("LevelPreLeft", Boolean.valueOf(isServer));
        }
        if (isServer) {
            Callback.invokeAPICallback("ServerLevelPreLeft", new Object[0]);
        } else {
            Callback.invokeAPICallback("LocalLevelPreLeft", new Object[0]);
        }
        if (isServer) {
            WorldDataSaverHandler.getInstance().onLevelLeft();
        }
        if (callLegacyCallback) {
            Callback.invokeAPICallback("LevelLeft", Boolean.valueOf(isServer));
            Callback.invokeAPICallback("GameLeft", Boolean.valueOf(isServer));
        }
        if (isServer) {
            Callback.invokeAPICallback("ServerLevelLeft", new Object[0]);
        } else {
            Callback.invokeAPICallback("DimensionUnloaded", Integer.valueOf(currentPlayerDimension));
            Callback.invokeAPICallback("LocalLevelLeft", new Object[0]);
        }
        Minecraft.onGameStopped(isServer);
        if (isServer) {
            Updatable.getForServer().cleanUp();
        } else {
            Updatable.getForClient().cleanUp();
        }
        if (isServer) {
            allEntities.clear();
        }
    }

    private static void setupWorld() {
        boolean booleanValue = ((Boolean) InnerCoreConfig.get("performance.time_based_limit")).booleanValue();
        int i = InnerCoreConfig.getInt(!booleanValue ? "performance.max_update_count" : "performance.max_update_time");
        int updatableMode = booleanValue ? 1 : 0;
        Updatable.setPreferences(updatableMode, i);
    }

    public static void onLocalTick() {
        if (isFirstLocalTick) {
            isFirstLocalTick = false;
            Callback.invokeAPICallback("LocalLevelLoaded", new Object[0]);
        }
        if (!isLocalTickDisabledDueToError) {
            try {
                Callback.invokeAPICallbackUnsafe("LocalTick", new Object[0]);
                Updatable.getForClient().onTickSingleThreaded();
            } catch (Throwable e) {
                ICLog.e("INNERCORE-CALLBACK", "error occurred in local tick callback", e);
                DialogHelper.reportFatalError("Fatal error occurred in local tick callback, local tick callback will be turned off until you re-enter the world.", e);
                isLocalTickDisabledDueToError = true;
            }
        }
        if (TickManager.getLastFatalError() != null || isLocalTickDisabledDueToError) {
            StringBuilder message = new StringBuilder();
            message.append("§c");
            message.append(AdaptedScriptAPI.Translation.translate("system.thread_stopped"));
            message.append("\nstopped threads: ");
            if (isLocalTickDisabledDueToError) {
                message.append("local ");
            }
            if (TickManager.getLastFatalError() != null) {
                message.append("server ");
            }
        }
        MainThreadQueue.localThread.executeQueue();
    }

    public static void onTick() {
        if (isFirstServerTick) {
            isFirstServerTick = false;
            onLevelPostLoaded(true);
            setupWorld();
        }
        TickManager.nativeTick();
        MainThreadQueue.serverThread.executeQueue();
    }

    public static void onConnectToHost(int port) {
        String host = getStringParam("host");
        LevelInfo.levelName = null;
        LevelInfo.levelDir = null;
        Minecraft.onConnectToHost(host, port);
        Callback.invokeAPICallback("ConnectingToHost", host, Integer.valueOf(port), 80);
        WorldDataSaverHandler.getInstance().onConnectedToRemoteWorld();
        AdaptedScriptAPI.IDRegistry.rebuildNetworkIdMap();
        Logger.error("CONNECTION ERROR", "Cannot connect to modded server at " + host);
        onLevelPostLoaded(false);
    }

    public static void onBlockDestroyed(final int n, final int n2, final int n3, final int n4, final long n5) {
        if (NativeCallback.isDestroyBlockCallbackInProgress) {
            return;
        }
        NativeCallback.isDestroyBlockCallbackInProgress = true;
        NativeCallback.isDestroyBlockCallbackInProgress = false;
    }

    public static void onBlockDestroyStarted(int x, int y, int z, int side) {
        throw new UnsupportedOperationException();
    }

    public static void _onBlockDestroyStarted(int x, int y, int z, int side) {
        throw new UnsupportedOperationException();
    }

    public static void onBlockDestroyContinued(int x, int y, int z, int side, float progress) {
        throw new UnsupportedOperationException();
    }

    public static void onBlockBuild(int x, int y, int z, int side, long player) {
        throw new UnsupportedOperationException();
    }

    public static void onBlockChanged(int x, int y, int z, int id1, int data1, int id2, int data2, int i1, int i2, long region) {
        throw new UnsupportedOperationException();
    }

    public static void onItemUsed(int x, int y, int z, int side, float fx, float fy, float fz, boolean isServer, boolean isExternal, long player) {
        throw new UnsupportedOperationException();
    }

    public static void onExplode(float x, float y, float z, float power, long entity, boolean b1, boolean b2, float anotherFloat) {
        throw new UnsupportedOperationException();
    }

    public static void onPlayerEat(int food, float ratio, long player) {
        throw new UnsupportedOperationException();
    }

    public static void onPlayerExpAdded(int exp, long player) {
        throw new UnsupportedOperationException();
    }

    public static void onPlayerLevelAdded(int level, long player) {
        throw new UnsupportedOperationException();
    }

    public static void onCommandExec() {
        String command = getStringParam("command");
        Object[] objArr = new Object[1];
        objArr[0] = command == null ? null : command.trim();
        Callback.invokeAPICallback("NativeCommand", objArr);
    }

    public static void onEntityAttacked(long entity, long attacker) {
        throw new UnsupportedOperationException();
    }

    public static void onInteractWithEntity(long entity, long player, float x, float y, float z) {
        throw new UnsupportedOperationException();
    }

    public static void startOverrideBlockBreakResult() {
        throw new UnsupportedOperationException();
    }

    public static BlockBreakResult endOverrideBlockBreakResult() {
        BlockBreakResult result = currentBlockBreakResultOverride;
        currentBlockBreakResultOverride = null;
        return result;
    }

    public static void onEntityAdded(long entity) {
        throw new UnsupportedOperationException();
    }

    public static void onLocalEntityAdded(long entity) {
        throw new UnsupportedOperationException();
    }

    public static void onEntityRemoved(long entity) {
        throw new UnsupportedOperationException();
    }

    public static void onLocalEntityRemoved(long entity) {
        throw new UnsupportedOperationException();
    }

    public static void onEntityPickUpDrop(long entity, long dropEntity, int count) {
        throw new UnsupportedOperationException();
    }

    public static void onExpOrbsSpawned(long region, int amount, float x, float y, float z, long player) {
        throw new UnsupportedOperationException();
    }

    public static void onEntityDied(long entity, long attacker, int damageType) {
        throw new UnsupportedOperationException();
    }

    public static void onEntityHurt(long entity, long attacker, int damageType, int damageValue, boolean someBool1, boolean someBool2) {
        throw new UnsupportedOperationException();
    }

    public static void onThrowableHit(long projectile, float hitX, float hitY, float hitZ, long entity, int blockX, int blockY, int blockZ, int blockSide, int itemId, int itemCount, int itemData, long itemExtra) {
        throw new UnsupportedOperationException();
    }

    public static void onPathNavigationDone(long entity, int result) {
        throw new UnsupportedOperationException();
    }

    public static void onRedstoneSignalChange(int x, int y, int z, int signal, boolean isLoadingChange, long region) {
        throw new UnsupportedOperationException();
    }

    public static void onRandomBlockTick(int x, int y, int z, int id, int data, long region) {
        throw new UnsupportedOperationException();
    }

    public static void onAnimateBlockTick(int x, int y, int z, int id, int data) {
        throw new UnsupportedOperationException();
    }

    public static void onBlockSpawnResources(int x, int y, int z, int id, int data, float f, int i, long region) {
        throw new UnsupportedOperationException();
    }

    public static void onBlockEventEntityInside(int x, int y, int z, long entity) {
        throw new UnsupportedOperationException();
    }

    public static void onBlockEventEntityStepOn(int x, int y, int z, long entity) {
        throw new UnsupportedOperationException();
    }

    public static void onBlockEventNeighbourChange(int x, int y, int z, int changedX, int changedY, int changedZ, long region) {
        throw new UnsupportedOperationException();
    }

    public static void onCustomTessellation(long tessellator, int x, int y, int z, int id, int data, boolean b) {
        throw new UnsupportedOperationException();
    }

    public static void onItemIconOverride(int id, int count, int data, int extra) {
        throw new UnsupportedOperationException();
    }

    public static void onItemModelOverride(long modelPtr, int id, int count, int data, long extra) {
        throw new UnsupportedOperationException();
    }

    public static void onItemNameOverride(int id, int count, int data, int extra) {
        throw new UnsupportedOperationException();
    }

    public static void onItemUsedNoTarget(long player) {
        throw new UnsupportedOperationException();
    }

    public static void onItemUseReleased(int ticks, long player) {
        throw new UnsupportedOperationException();
    }

    public static void onItemUseComplete(long player) {
        throw new UnsupportedOperationException();
    }

    public static void onItemDispensed(float x, float y, float z, int side, int id, int count, int data, long extra, long region, int slot) {
        throw new UnsupportedOperationException();
    }

    void onEnchantPostAttack(int enchantId, int damage, long actor1, long actor2) {
        throw new UnsupportedOperationException();
    }

    void onEnchantPostHurt(int enchantId, int itemId, int itemCount, int itemData, long itemExtraPtr, int damage, long actor1, long actor2) {
        throw new UnsupportedOperationException();
    }

    void onEnchantGetDamageBonus(int enchantId, int damage, long actor) {
        throw new UnsupportedOperationException();
    }

    void onEnchantGetProtectionBonus(int enchantId, int damage, int cause, long attackerActor) {
        throw new UnsupportedOperationException();
    }

    public static void onWorkbenchCraft(long containerPtr, long player, int size) {
        throw new UnsupportedOperationException();
    }

    public static void onScreenChanged(boolean isPushEvent) {
        String name = getStringParam("screen_name");
        String lastName = getStringParam("last_screen_name");
        StringBuilder sb = new StringBuilder();
        sb.append("screen changed: ");
        sb.append(lastName);
        sb.append(" -> ");
        sb.append(name);
        sb.append(isPushEvent ? " (pushed)" : " (popped)");
        Logger.debug(sb.toString());
        if (name.equals("leave_level_screen") || name.equals("pause_screen") || name.startsWith("world_loading_progress_screen")) {
            WorldDataSaverHandler.getInstance().onPauseScreenOpened();
        }
        Callback.invokeAPICallback("NativeGuiChanged", name, lastName, Boolean.valueOf(isPushEvent));
    }

    public static void onPreChunkPostProcessed(int x, int z) {
        throw new UnsupportedOperationException();
    }

    public static void onChunkPostProcessed(int x, int z) {
        throw new UnsupportedOperationException();
    }

    public static void onBiomeMapGenerated(int dimension, int x, int z) {
        throw new UnsupportedOperationException();
    }

    public static void onCustomDimensionTransfer(long entity, int from, int to) {
        throw new UnsupportedOperationException();
    }

    public static void onModdedServerPacketReceived(int formatId) {
        throw new UnsupportedOperationException();
    }

    public static void onModdedClientPacketReceived(int formatId) {
        throw new UnsupportedOperationException();
    }
}
