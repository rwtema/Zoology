package com.rwtema.zoology.genes;

import com.google.common.collect.ImmutableList;
import com.rwtema.zoology.Zoology;
import static com.rwtema.zoology.genes.GenePair.Cache.genesCachePairs;
import java.util.List;
import java.util.Random;

public abstract class GenePair {
	public final Gene a;
	public final Gene b;
	public final byte index;

	private GenePair(Gene a, Gene b, byte index) {
		this.a = a;
		this.b = b;
		this.index = index;
	}

	public static GenePair create(Gene a, Gene b) {
		return genesCachePairs[a.ordinal() + b.ordinal() * 5];
	}

	public static GenePair combine(GenePair mama, GenePair papa, Random rand) {
		return create(
				rand.nextInt(Zoology.STRAND_SIZE * 8) == 0 ? Gene.rand(rand) : mama.getRandomGene(rand),
				rand.nextInt(Zoology.STRAND_SIZE * 8) == 0 ? Gene.rand(rand) : papa.getRandomGene(rand)
		);
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public int hashCode() {
		return index;
	}

	public abstract boolean isHomo();

	public abstract Gene getRandomGene(Random random);

	@Override
	public String toString() {
		return "[" + a + b + ']';
	}

	public List<Gene> genesAsList() {
		return ImmutableList.of(a, b);
	}

	public static class Cache {
		public static final GenePair[] genesCacheIndex;
		public static final GenePair[] genesCachePairs;

		public static final GenePair[] geneHetero;
		public static final GenePair[] geneHomo;

		static {
			genesCacheIndex = new GenePair[15];
			genesCachePairs = new GenePair[5 * 5];
			geneHetero = new GenePair[10];
			geneHomo = new GenePair[5];
			byte bit = 0;
			for (Gene gene : Gene.values()) {
				Homo pair = new Homo(gene, bit);
				genesCacheIndex[bit] = pair;
				genesCachePairs[gene.ordinal() + 5 * gene.ordinal()] = pair;
				geneHomo[bit] = pair;
				bit++;
			}
			int k = 0;
			for (Gene a : Gene.values()) {
				for (Gene b : Gene.values()) {
					if (a == b) continue;
					if (a.ordinal() >= b.ordinal()) {
						continue;
					}
					Hetero pair = new Hetero(a, b, bit);
					genesCacheIndex[bit] = pair;
					genesCachePairs[a.ordinal() + 5 * b.ordinal()] = pair;
					genesCachePairs[b.ordinal() + 5 * a.ordinal()] = pair;
					geneHetero[k] = pair;
					bit++;
					k++;
				}
			}
		}
	}

	public static class Homo extends GenePair {

		public Homo(Gene gene, byte index) {
			super(gene, gene, index);
		}

		@Override
		public boolean isHomo() {
			return true;
		}

		@Override
		public Gene getRandomGene(Random random) {
			return a;
		}
	}

	public static class Hetero extends GenePair {
		private Hetero(Gene a, Gene b, byte index) {
			super(a, b, index);
		}

		@Override
		public boolean isHomo() {
			return false;
		}

		@Override
		public Gene getRandomGene(Random random) {
			return random.nextBoolean() ? a : b;
		}
	}

}
