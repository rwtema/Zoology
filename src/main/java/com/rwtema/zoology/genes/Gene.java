package com.rwtema.zoology.genes;

import java.util.Random;

public enum Gene {
	A, B, C, D, E;

	public static Gene selectRandom(float[] weights, Random rand) {
		float p = rand.nextFloat() * 0.999F;
		float c = 0;
		Gene[] values = Gene.values();
		for (int i = 0; i < values.length; i++) {
			c += weights[i];
			if (c >= p) {
				return values[i];
			}
		}

		return values[values.length - 1];
//		throw new RuntimeException("Value " + p + " is greater than " + c + " from array " + Arrays.toString(weights) );
	}

	public static Gene rand(Random rand) {
		return values()[rand.nextInt(5)];
	}
}
