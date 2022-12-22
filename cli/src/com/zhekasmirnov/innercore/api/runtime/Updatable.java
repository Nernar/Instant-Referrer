package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import com.zhekasmirnov.innercore.api.runtime.saver.world.ScriptableSaverScope;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataScopeRegistry;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Updatable {
    public static final int MODE_COUNT_BASED = 0;
    public static final int MODE_TIME_BASED = 1;
    private final boolean isMultithreadingAllowed;
    private static int currentMode = 0;
    private static int maxUpdateCallsPerTick = 128;
    private static int maxUpdateTimePerTick = 50;
    private static final Object[] EMPTY_ARGS = new Object[0];
    private static final Updatable serverInstance = new Updatable(true);
    private static final Updatable clientInstance = new Updatable(false);
    private final List<ScriptableObject> updatableObjects = new ArrayList<>();
    private final List<ScriptableObject> disabledDueToError = new ArrayList<>();
    private Context currentContext = null;
    private int currentArrayPosition = 0;
    private final ArrayList<ScriptableObject> postedRemovedUpdatables = new ArrayList<>();
    @SuppressWarnings("unused")
    private int statCurrentCalls = 0;

    static {
        setPreferences(0, 256);
        WorldDataScopeRegistry.getInstance().addScope("_updatables", new ScriptableSaverScope() {
            @Override
            public ScriptableObject save() {
                ArrayList<ScriptableObject> updatableObjectsToSave = new ArrayList<>();
                for (ScriptableObject updatable : Updatable.getForServer().updatableObjects) {
                    if (ObjectSaverRegistry.getSaverFor(updatable) != null) {
                        updatableObjectsToSave.add(updatable);
                    }
                }
                for (ScriptableObject updatable2 : Updatable.getForServer().disabledDueToError) {
                    if (ObjectSaverRegistry.getSaverFor(updatable2) != null && !Updatable.shouldBeRemoved(updatable2)) {
                        updatableObjectsToSave.add(updatable2);
                    }
                }
                return new NativeArray(updatableObjectsToSave.toArray());
            }

            @Override
            public void read(Object scope) {
                int successfullyLoaded = 0;
                if (scope instanceof NativeArray) {
                    Object[] updatableObjectsToRead = ((NativeArray) scope).toArray();
                    for (Object possibleUpdatable : updatableObjectsToRead) {
                        if (possibleUpdatable instanceof ScriptableObject) {
                            ScriptableObject scriptableUpdatable = (ScriptableObject) possibleUpdatable;
                            if (scriptableUpdatable.get("update") instanceof Function) {
                                Updatable.getForServer().addUpdatable(scriptableUpdatable);
                                successfullyLoaded++;
                            }
                        }
                        ICLog.i("UPDATABLE", "loaded updatable data is not a scriptable object or it does not have update function, loading failed. obj=" + possibleUpdatable);
                    }
                } else {
                    ICLog.i("UPDATABLE", "assertion failed: updatable scope is not an array, loading failed");
                }
                ICLog.d("UPDATABLE", "successfully loaded updatables: " + successfullyLoaded);
            }
        });
    }

    private Updatable(boolean isMultithreadingAllowed) {
        this.isMultithreadingAllowed = isMultithreadingAllowed;
    }

    public void cleanUp() {
        this.updatableObjects.clear();
        this.disabledDueToError.clear();
        this.postedRemovedUpdatables.clear();
        this.currentArrayPosition = 0;
        this.currentContext = null;
    }

    public List<ScriptableObject> getAllUpdatableObjects() {
        return this.updatableObjects;
    }

    public void addUpdatable(ScriptableObject obj) {
        Object _update = obj.get("update", obj);
        if (_update instanceof Function) {
            this.updatableObjects.add(obj);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("cannot add updatable object: <obj>.update must be a function, not ");
        sb.append(_update != null ? _update.getClass() : null);
        sb.append(" ");
        sb.append(_update);
        throw new IllegalArgumentException(sb.toString());
    }

    public static void cleanUpAll() {
        getForServer().cleanUp();
        getForClient().cleanUp();
    }

    public static void setPreferences(int mode, int modeValue) {
        switch (mode) {
            case 0:
                maxUpdateCallsPerTick = modeValue;
                ICLog.d("THREADING", "updatable engine uses count-based mode, maxCount=" + modeValue);
                break;
            case 1:
                maxUpdateTimePerTick = modeValue;
                ICLog.d("THREADING", "updatable engine uses time-based mode, maxTime=" + modeValue);
                break;
            default:
                throw new IllegalArgumentException("invalid updatable engine mode: " + mode);
        }
        currentMode = mode;
    }

    public static boolean shouldBeRemoved(Scriptable obj) {
        Object _remove = obj.get("remove", obj);
        return (_remove instanceof Boolean) && ((Boolean) _remove).booleanValue();
    }

    private static String updatableToString(Context ctx, Scriptable obj) {
        Object _to_string = obj.get("_to_string", obj);
        if (_to_string instanceof Function) {
            try {
                Function to_string = (Function) _to_string;
                return new StringBuilder().append(to_string.call(ctx, to_string.getParentScope(), obj, EMPTY_ARGS)).toString();
            } catch (Throwable th) {
            }
        }
        return new StringBuilder().append(obj).toString();
    }

    public static void reportError(Throwable err, String updatableStr) {
        Logger.error("UPDATABLE ERROR", "Updatable " + updatableStr + " was disabled due to error, corresponding object will be disabled. To re-enable it re-enter the world.");
    }

    public void executeUpdateWithContext(Context ctx, ScriptableObject obj) {
        Object _update = obj.get("update", obj);
        try {
            Function update = (Function) _update;
            update.call(ctx, update.getParentScope(), obj, EMPTY_ARGS);
            if (shouldBeRemoved(obj)) {
                this.postedRemovedUpdatables.add(obj);
            }
        } catch (Throwable err) {
            this.disabledDueToError.add(obj);
            this.postedRemovedUpdatables.add(obj);
            Object _handle_error = obj.get("_handle_error", obj);
            if (_handle_error instanceof Function) {
                try {
                    Function handle_error = (Function) _handle_error;
                    handle_error.call(ctx, handle_error.getParentScope(), obj, new Object[]{err});
                    return;
                } catch (Throwable th) {
                    Logger.error("UPDATABLE", "Error occurred in error handler for " + updatableToString(ctx, obj) + " hash=" + obj.hashCode());
                    return;
                }
            }
            reportError(err, updatableToString(ctx, obj));
        }
    }

    private boolean executeUpdate(final ScriptableObject obj) {
        if (ScriptableObjectHelper.getBooleanProperty(obj, "noupdate", false)) {
            return false;
        }
        TickExecutor executor = TickExecutor.getInstance();
        if (this.isMultithreadingAllowed && executor.isAvailable()) {
            TickExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    Updatable.this.executeUpdateWithContext(Compiler.assureContextForCurrentThread(), obj);
                }
            });
            return true;
        }
        executeUpdateWithContext(this.currentContext, obj);
        return true;
    }

    private void removePosted() {
        Iterator<ScriptableObject> it = this.postedRemovedUpdatables.iterator();
        while (it.hasNext()) {
            ScriptableObject updatable = it.next();
            this.updatableObjects.remove(updatable);
        }
        this.postedRemovedUpdatables.clear();
    }

    private boolean executeCurrentToNext() {
        if (this.updatableObjects.size() == 0) {
            return true;
        }
        this.currentArrayPosition %= this.updatableObjects.size();
        boolean executed = executeUpdate(this.updatableObjects.get(this.currentArrayPosition));
        this.currentArrayPosition++;
        if (executed) {
            this.statCurrentCalls++;
        }
        return executed;
    }

    private void onCountBasedTick() {
        int callCount = maxUpdateCallsPerTick;
        int calls = 0;
        int i = 0;
        while (i < callCount && i < this.updatableObjects.size()) {
            int calls2 = calls + 1;
            if (calls < this.updatableObjects.size()) {
                if (executeCurrentToNext()) {
                    i++;
                }
                calls = calls2;
            } else {
                return;
            }
        }
    }

    private void onTimeBasedTick() {
        int callCount = this.updatableObjects.size();
        long timeStart = System.currentTimeMillis();
        for (int i = 0; i < callCount; i++) {
            executeCurrentToNext();
            long timeCur = System.currentTimeMillis();
            if (timeCur - timeStart > maxUpdateTimePerTick) {
                return;
            }
        }
    }

    public void onTick() {
        if (this.currentContext == null) {
            this.currentContext = Compiler.assureContextForCurrentThread();
        }
        this.statCurrentCalls = 0;
        if (currentMode == 0 || (this.isMultithreadingAllowed && TickExecutor.getInstance().isAvailable())) {
            onCountBasedTick();
        } else if (currentMode == 1) {
            onTimeBasedTick();
        }
    }

    public void onPostTick() {
        removePosted();
    }

    public void onTickSingleThreaded() {
        onTick();
        onPostTick();
    }

    public static Updatable getForServer() {
        return serverInstance;
    }

    public static Updatable getForClient() {
        return clientInstance;
    }
}
