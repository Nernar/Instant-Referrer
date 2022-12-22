package com.zhekasmirnov.innercore.api.unlimited;

public class IDDataPair {
    public int data;
    public int id;

    public IDDataPair(int id, int data) {
        this.id = id;
        this.data = data;
    }

    public boolean equals(Object o) {
        if (o instanceof IDDataPair) {
            IDDataPair other = (IDDataPair) o;
            return this.id == other.id && this.data == other.data;
        }
        return false;
    }

    public int hashCode() {
        return (this.id & 65535) | ((this.data & 255) << 16);
    }

    public String toString() {
        return String.valueOf(this.id) + ":" + this.data;
    }
}
