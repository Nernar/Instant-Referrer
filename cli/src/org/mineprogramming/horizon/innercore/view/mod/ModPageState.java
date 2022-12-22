package org.mineprogramming.horizon.innercore.view.mod;

public enum ModPageState {
    NOT_INSTALLED,
    INSTALLED,
    TO_UPDATE;

    public static ModPageState[] valuesCustom() {
        ModPageState[] valuesCustom = values();
        int length = valuesCustom.length;
        ModPageState[] modPageStateArr = new ModPageState[length];
        System.arraycopy(valuesCustom, 0, modPageStateArr, 0, length);
        return modPageStateArr;
    }
}
