package com.zhekasmirnov.horizon.compiler.packages;

public class ExternalDirectoryManager {
    private boolean shouldDisplayDialog = false;
    private Location location = Location.EXTERNAL;

    public enum Location {
        EXTERNAL,
        INTERNAL;

        public static Location[] valuesCustom() {
            Location[] valuesCustom = values();
            int length = valuesCustom.length;
            Location[] locationArr = new Location[length];
            System.arraycopy(valuesCustom, 0, locationArr, 0, length);
            return locationArr;
        }
    }

    ExternalDirectoryManager(Object context) {
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Object context, Location location) {
        this.location = location;
    }

    public boolean shouldDisplayDialog() {
        boolean z = this.shouldDisplayDialog;
        this.shouldDisplayDialog = false;
        return z;
    }
}
