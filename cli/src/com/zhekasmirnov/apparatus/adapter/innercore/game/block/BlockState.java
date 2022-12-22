package com.zhekasmirnov.apparatus.adapter.innercore.game.block;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import java.util.HashMap;
import java.util.Map;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class BlockState {
    public final int data;
    public final int id;
    private Map<String, Integer> namedStates;
    private int[] rawStates;
    public final int runtimeId;
    private Map<Integer, Integer> states;

    private static native int[] getAllStatesFromId(int i);

    public static native int getDataFromRuntimeId(int i);

    public static native int getIdFromRuntimeId(int i);

    private static native int getStateFromId(int i, int i2);

    public static native int runtimeIdByStates(int i, int[] iArr);

    public BlockState(int id, int data) {
        this.rawStates = null;
        this.states = null;
        this.namedStates = null;
        this.id = id;
        this.data = data;
        this.runtimeId = -1;
    }

    public BlockState(long idDataAndState) {
        this.rawStates = null;
        this.states = null;
        this.namedStates = null;
        long idData = idDataAndState & (-1);
        this.id = ((int) (idData >> 16)) & 65535;
        this.data = (int) (idData & 65535);
        this.runtimeId = (int) (idDataAndState >> 32);
    }

    public BlockState(int id, Map<?, Integer> map) {
        this.rawStates = null;
        this.states = null;
        this.namedStates = null;
        this.id = id;
        this.rawStates = new int[map.size() * 2];
        int index = 0;
        for (Map.Entry<?, Integer> entry : map.entrySet()) {
            Object key0 = entry.getKey();
            int key = -1;
            if (key0 instanceof Integer) {
                key = ((Integer) key0).intValue();
            } else if (key0 instanceof CharSequence) {
                key = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_states", key0.toString()), -1);
            }
            if (key != -1) {
                int index2 = index + 1;
                this.rawStates[index] = key;
                this.rawStates[index2] = entry.getValue().intValue();
                index = index2 + 1;
            }
        }
        this.runtimeId = runtimeIdByStates(id, this.rawStates);
        this.data = getDataFromRuntimeId(this.runtimeId);
    }

    private BlockState(int id, int[] rawStates) {
        this.rawStates = null;
        this.states = null;
        this.namedStates = null;
        this.id = id;
        this.rawStates = rawStates;
        this.runtimeId = runtimeIdByStates(id, rawStates);
        this.data = getDataFromRuntimeId(this.runtimeId);
    }

    private static Map<Integer, Integer> scriptableToStateMap(Scriptable scriptable) {
        Map<Integer, Integer> result = new HashMap<>();
        for (Object key0 : scriptable.getIds()) {
            String key = key0.toString();
            int state = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_states", key), -1);
            if (state == -1) {
                try {
                    state = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                }
            }
            Object value = scriptable.get(key, scriptable);
            result.put(Integer.valueOf(state), Integer.valueOf(value instanceof Number ? ((Number) value).intValue() : 0));
        }
        return result;
    }

    public BlockState(int id, Scriptable scriptable) {
        this(id, scriptableToStateMap(scriptable));
    }

    public int getId() {
        return this.id;
    }

    public int getData() {
        return this.data;
    }

    public int getRuntimeId() {
        return this.runtimeId;
    }

    public boolean isValidState() {
        return this.runtimeId != -1;
    }

    public int getState(int state) {
        if (this.runtimeId > 0) {
            return getStateFromId(this.runtimeId, state);
        }
        return -1;
    }

    public boolean hasState(int state) {
        return getState(state) != -1;
    }

    public BlockState addState(int state, int value) {
        Map<Integer, Integer> mStates = new HashMap<>(getStates());
        mStates.put(Integer.valueOf(state), Integer.valueOf(value));
        return new BlockState(this.id, mStates);
    }

    public BlockState addStatesMap(Map<?, Integer> states) {
        Map<Object, Integer> mStates = new HashMap<>(getStates());
        mStates.putAll(states);
        return new BlockState(this.id, mStates);
    }

    public BlockState addStates(Scriptable states) {
        return addStatesMap(scriptableToStateMap(states));
    }

    public Map<Integer, Integer> getStates() {
        if (this.states == null) {
            this.states = new HashMap<>();
            if (this.runtimeId > 0) {
                this.rawStates = this.rawStates != null ? this.rawStates : getAllStatesFromId(this.runtimeId);
                for (int i = 0; i < this.rawStates.length / 2; i++) {
                    this.states.put(Integer.valueOf(this.rawStates[i * 2]), Integer.valueOf(this.rawStates[(i * 2) + 1]));
                }
            }
        }
        return this.states;
    }

    public Map<String, Integer> getNamedStates() {
        if (this.namedStates == null) {
            this.namedStates = new HashMap<>();
            Map<Integer, Integer> states = getStates();
            for (Map.Entry<Integer, Integer> entry : states.entrySet()) {
                this.namedStates.put(GameEnums.getSingleton().getKeyForEnum("block_states", entry.getKey()), entry.getValue());
            }
        }
        return this.namedStates;
    }

    public ScriptableObject getStatesScriptable() {
        Map<Integer, Integer> states = getStates();
        ScriptableObject result = ScriptableObjectHelper.createEmpty();
        for (Map.Entry<Integer, Integer> entry : states.entrySet()) {
            result.put(entry.getKey().toString(), result, entry.getValue());
        }
        return result;
    }

    public ScriptableObject getNamedStatesScriptable() {
        Map<String, Integer> states = getNamedStates();
        ScriptableObject result = ScriptableObjectHelper.createEmpty();
        for (Map.Entry<String, Integer> entry : states.entrySet()) {
            result.put(entry.getKey(), result, entry.getValue());
        }
        return result;
    }

    public String toString() {
        return "BlockState{id=" + this.id + ", data=" + this.data + ", runtimeId=" + this.runtimeId + ", states=" + getNamedStates() + "}";
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        BlockState that = (BlockState) object;
        if (this.id != that.id || this.data != that.data || this.runtimeId != that.runtimeId) {
            return false;
        }
        return getStates().equals(that.getStates());
    }
}
