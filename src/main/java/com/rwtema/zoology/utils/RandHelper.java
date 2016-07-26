package com.rwtema.zoology.utils;

import java.util.Random;

public class RandHelper {
	public static int getRandInt(double base, Random rand) {
		if (base < 0) throw new IllegalArgumentException("no less than 0 - " + base);
		int b = (int) base;
		double k = base - b;
		if (rand.nextDouble() < k) {
			b++;
		}
		return b;
	}
}
