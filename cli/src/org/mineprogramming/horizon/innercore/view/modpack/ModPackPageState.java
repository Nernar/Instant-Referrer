package org.mineprogramming.horizon.innercore.view.modpack;

enum ModPackPageState {
    NOT_INSTALLED,
    ARCHIVED,
    INSTALLED,
    SELECTED,
    TO_UPDATE;

    public static ModPackPageState[] valuesCustom() {
        ModPackPageState[] valuesCustom = values();
        int length = valuesCustom.length;
        ModPackPageState[] modPackPageStateArr = new ModPackPageState[length];
        System.arraycopy(valuesCustom, 0, modPackPageStateArr, 0, length);
        return modPackPageStateArr;
    }
}
