package com.zhekasmirnov.innercore.api.runtime;

import java.util.HashMap;

public class TPSMeter {
    private static HashMap<String, TPSMeter> tpsMeterByName = new HashMap<>();
    private int frame;
    private long lastMeasuredFrame;
    private long lastMeasuredTime;
    private final int maxFramesPerMeasure;
    private final int maxTimePerMeasure;
    private float tps;

    public TPSMeter(int maxFramesPerMeasure, int maxTimePerMeasure) {
        this.lastMeasuredTime = -1L;
        this.lastMeasuredFrame = 0L;
        this.frame = 0;
        this.tps = 0.0f;
        this.maxFramesPerMeasure = maxFramesPerMeasure;
        this.maxTimePerMeasure = maxTimePerMeasure;
    }

    public TPSMeter(String name, int maxFramesPerMeasure, int maxTimePerMeasure) {
        this(maxFramesPerMeasure, maxTimePerMeasure);
        tpsMeterByName.put(name, this);
    }

    public static TPSMeter getByName(String name) {
        return tpsMeterByName.get(name);
    }

    public void onTick() {
        long time = System.currentTimeMillis();
        int i = this.frame;
        this.frame = i + 1;
        if (i % this.maxFramesPerMeasure == 0 || time - this.lastMeasuredTime > this.maxTimePerMeasure) {
            this.tps = ((float) ((this.frame - this.lastMeasuredFrame) * 1000)) / ((float) (time - this.lastMeasuredTime));
            this.lastMeasuredFrame = this.frame;
            this.lastMeasuredTime = time;
        }
    }

    public float getTps() {
        return Math.round(this.tps * 10.0f) / 10.0f;
    }
}
