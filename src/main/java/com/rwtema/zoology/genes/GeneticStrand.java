package com.rwtema.zoology.genes;

import com.rwtema.zoology.Zoology;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraftforge.common.util.INBTSerializable;

public class GeneticStrand implements INBTSerializable<NBTTagIntArray> {
	public static final int[] intPairs;
	private static final long INT_MASK = 0xffffffffL;

	static {
		intPairs = new int[Zoology.VALUES_PER_INTEGER];
		int v = 1;
		for (int i = 0; i < Zoology.VALUES_PER_INTEGER; i++) {
			intPairs[Zoology.VALUES_PER_INTEGER - 1 - i] = v;
			v *= 15;
		}
	}

	public GenePair[] strandValues;

	public GeneticStrand() {
	}

	public GeneticStrand(GenePair[] strandValues) {
		this.strandValues = strandValues;
	}

	public static long toLong(int value) {
		return value & INT_MASK;
	}

	@Override
	public NBTTagIntArray serializeNBT() {
		int[] arr = new int[Zoology.INTEGERS_PER_STRAND];
		for (int i = 0; i < Zoology.INTEGERS_PER_STRAND; i++) {
			int i_offset = i * Zoology.VALUES_PER_INTEGER;
			int n = 0;
			for (int j = 0; j < Zoology.VALUES_PER_INTEGER; j++) {
				int k = strandValues[i_offset + j].index;
				n += intPairs[j] * k;
			}
			arr[i] = n;
		}
		return new NBTTagIntArray(arr);
	}

	@Override
	public void deserializeNBT(NBTTagIntArray nbt) {
		int[] ints = nbt.getIntArray();
		strandValues = new GenePair[Zoology.STRAND_SIZE];

		for (int i = 0; i < Zoology.INTEGERS_PER_STRAND; i++) {
			int i_offset = i * Zoology.VALUES_PER_INTEGER;
			long n = toLong(ints[i]);
			for (int j = 0; j < Zoology.VALUES_PER_INTEGER; j++) {
				long o = n / intPairs[j];
				strandValues[i_offset + j] = GenePair.Cache.genesCacheIndex[(int) o];
				n -= o * intPairs[j];
			}
		}
	}
}
