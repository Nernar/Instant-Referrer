package com.zhekasmirnov.innercore.api.unlimited;

public class BlockShape {
    public float x1;
    public float x2;
    public float y1;
    public float y2;
    public float z1;
    public float z2;

    public BlockShape(float x1, float y1, float z1, float x2, float y2, float z2) {
        set(x1, y1, z1, x2, y2, z2);
    }

    public BlockShape() {
        set(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public void set(float x1, float y1, float z1, float x2, float y2, float z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public void setToBlock(int id, int data) {
        throw new UnsupportedOperationException();
    }
}
