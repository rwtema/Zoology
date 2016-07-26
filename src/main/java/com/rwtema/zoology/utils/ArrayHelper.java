package com.rwtema.zoology.utils;

import gnu.trove.function.TFloatFunction;
import java.util.Iterator;

public class ArrayHelper {


	public static float[] apply(float[] f, TFloatFunction func) {
		float[] nf = new float[f.length];
		for (int i = 0; i < f.length; i++) {
			nf[i] = func.execute(f[i]);
		}
		return nf;
	}


	public static <T> Iterable<T[]> getOuterArrays(T[] base, int n) {
		return new Iterable<T[]>() {

			@Override
			public Iterator<T[]> iterator() {
				return new Iterator<T[]>() {
					int[] array = new int[n];

					@Override
					public boolean hasNext() {
						for (int i : array) {
							if (i != (base.length - 1)) {
								return true;
							}
						}

						return false;
					}

					@Override
					public T[] next() {
						for (int i = array.length - 1; i >= 0; i--) {
							if (array[i] == base.length - 1) {
								array[i] = 0;
							} else {
								array[i]++;
								break;
							}
						}

						T[] t_array = (T[]) new Object[n];

						for (int i = 0; i < t_array.length; i++) {
							t_array[i] = base[array[i]];
						}

						return t_array;
					}
				};
			}
		};
	}
}
