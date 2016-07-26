package com.rwtema.zoology.genes;

public enum Dominance {
	CO_DOMINANT,
	FIRST_DOMINANT,
	SECOND_DOMINANT;


	public static int toBit(Dominance[] doms) {
		int t = 0;
		for (int i = 0; i < doms.length; i++) {
			t += doms[i].ordinal() << (i * 2);
		}
		return t;
	}

	public static Dominance[] fromBits(int t, Dominance[] dest) {
		for (int i = 0; i < dest.length; i++) {
			dest[i] = values()[(t >> (i * 2)) & 3];
		}
		return dest;
	}

}
