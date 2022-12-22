package com.zhekasmirnov.apparatus.api.common;

public class Vector3 {
    public float x;
    public float y;
    public float z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3() {
        this(0.0f, 0.0f, 0.0f);
    }
}
