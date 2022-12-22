package org.mineprogramming.horizon.innercore.view.page;

import com.zhekasmirnov.apparatus.util.Java8BackComp;
import java.util.HashMap;

public class PageState extends HashMap<String, Object> {
    private static final long serialVersionUID = -2796202591735004711L;

    public int getInt(String key) {
        return ((Integer) get(key)).intValue();
    }

    public int optInt(String key, int fallback) {
        return ((Integer) Java8BackComp.getOrDefault(this, key, Integer.valueOf(fallback))).intValue();
    }
}
