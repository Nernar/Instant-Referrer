package com.zhekasmirnov.apparatus.adapter.innercore.game.common;

public class Vector3 {
    public final float x;
    public final float y;
    public final float z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(float[] arr) {
        this(arr[0], arr[1], arr[2]);
    }

    public float lengthSqr() {
        return (this.x * this.x) + (this.y * this.y) + (this.z * this.z);
    }

    public float length() {
        return (float) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z));
    }

    public float distanceSqr(Vector3 other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        float dz = this.z - other.z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public float distance(Vector3 other) {
        return (float) Math.sqrt(distanceSqr(other));
    }
}
