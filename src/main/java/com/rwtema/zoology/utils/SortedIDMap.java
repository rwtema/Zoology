package com.rwtema.zoology.utils;

import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortedIDMap<V> {
	final public Comparator<V> comparator;
	List<V> list = new ArrayList<>();
	TObjectIntHashMap<V> arrayIDs = new TObjectIntHashMap<>();

	public SortedIDMap(Comparator<V> comparator) {
		this.comparator = comparator;
	}

	public void add(V t) {
		if(arrayIDs.containsKey(t))
			return;

		list.add(t);

		Collections.sort(list, comparator);

		arrayIDs.clear();
		for (int i = 0; i < list.size(); i++) {
			arrayIDs.put(list.get(i), i);
		}
	}

	public int get(V t) {
		return arrayIDs.get(t);
	}

	public V get(int t) {
		return list.get(t);
	}
}
