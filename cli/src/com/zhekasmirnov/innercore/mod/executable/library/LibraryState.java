package com.zhekasmirnov.innercore.mod.executable.library;

public enum LibraryState {
    NONE,
    INVALID,
    INITIALIZED,
    PREPARED,
    LOADED;

    public static LibraryState[] valuesCustom() {
        LibraryState[] valuesCustom = values();
        int length = valuesCustom.length;
        LibraryState[] libraryStateArr = new LibraryState[length];
        System.arraycopy(valuesCustom, 0, libraryStateArr, 0, length);
        return libraryStateArr;
    }
}
