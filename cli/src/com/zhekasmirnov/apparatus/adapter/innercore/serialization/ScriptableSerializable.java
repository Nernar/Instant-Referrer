package com.zhekasmirnov.apparatus.adapter.innercore.serialization;

public interface ScriptableSerializable {
    Object deserialize(ScriptableData scriptableData);

    ScriptableData serialize(Object obj);
}
