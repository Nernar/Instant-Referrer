package com.nernar.instant.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Shuffles {
	
	public static <T> void shuffle(List<T> list) {
		Collections.shuffle(list);
	}
	
	public static <T> void shuffle(T[] array) {
		synchronized (array) {
			List<T> list = Arrays.asList(array);
			shuffle(list);
			for (int i = 0; i < array.length; i++) {
				array[i] = list.get(i);
			}
		}
	}
	
	public static <T> void shuffle(Collection<T> collection) {
		synchronized (collection) {
			T[] array = (T[]) collection.toArray();
			shuffle(array);
			collection.clear();
			Collections.addAll(collection, array);
		}
	}
}
