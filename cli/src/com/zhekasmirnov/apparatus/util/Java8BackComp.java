package com.zhekasmirnov.apparatus.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Java8BackComp {
    public static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public static int hash(Object... objects) {
        if (objects == null) {
            return 0;
        }
        int length = objects.length;
        int result = 1;
        for (int result2 = 0; result2 < length; result2++) {
            Object element = objects[result2];
            result = (result * 31) + (element == null ? 0 : element.hashCode());
        }
        return result;
    }

    public static <K, V> V getOrDefault(Map<K, V> map, K key, V val) {
        return map.containsKey(key) ? map.get(key) : val;
    }

    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<? super K, ? extends V> f) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        V val = f.apply(key);
        map.put(key, val);
        return val;
    }

    public static <T> Stream<T> stream(Collection<T> collection) {
        return StreamSupport.stream(Spliterators.spliterator(collection, 0), false);
    }

    public static <T> boolean removeIf(Collection<T> collection, Predicate<? super T> filter) {
        try {
            return collection.removeIf(filter);
        } catch (NoSuchMethodError e) {
            boolean removed = false;
            Iterator<T> each = collection.iterator();
            while (each.hasNext()) {
                if (filter.test(each.next())) {
                    each.remove();
                    removed = true;
                }
            }
            return removed;
        }
    }
}
