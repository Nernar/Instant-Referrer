package com.zhekasmirnov.horizon.modloader.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class JavaCompilerArguments {
    private ArrayList<String> args = new ArrayList<>();

    public JavaCompilerArguments(String... strArr) {
        add(strArr);
    }

    public JavaCompilerArguments add(String... strArr) {
        this.args.addAll(Arrays.asList(strArr));
        return this;
    }

    public String toString() {
        return "Argument{args=" + this.args + '}';
    }

    public String[] toArray() {
        ArrayList<String> arrayList = new ArrayList<>();
        Iterator<String> it = this.args.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (next != null && !next.isEmpty()) {
                arrayList.add(next);
            }
        }
        String[] strArr = new String[arrayList.size()];
        arrayList.toArray(strArr);
        return strArr;
    }
}
