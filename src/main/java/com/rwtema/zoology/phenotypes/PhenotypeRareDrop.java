package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import gnu.trove.list.array.TIntArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class PhenotypeRareDrop extends PhenotypeDrop {
	Double FAKE_VALUE = -100D;

	public PhenotypeRareDrop(String name, ItemStack drop, double lower, double upper, int reserved_genes) {
		super(name, drop, lower, upper, reserved_genes, 0);
	}

	@Override
	public void onReserve(GenePool<EntityAnimal> entityAnimalGenePool, int i, Random rand) {
		int k = rand.nextInt(5);
		float[] floats = entityAnimalGenePool.probabilities[i];
		for (int j = 0; j < floats.length; j++) {
			floats[j] = k == j ? 0 : 0.25F;
		}
	}

	@Override
	public ITextComponent buildComponent(Double value) {
		if (FAKE_VALUE.equals(value))
			return null;
		return super.buildComponent(value);
	}

	@Nonnull
	@Override
	public Double calcValue(GeneticStrand strand, GenePool<EntityAnimal> pool, GenePool.Link link) {
		for (int i : link.assignedGenes) {
			GenePair pair = strand.strandValues[i];

			if (pool.probabilities[i][pair.a.ordinal()] < 0.01F
					|| pool.probabilities[i][pair.b.ordinal()] < 0.01F
					) {
				return super.calcValue(strand, pool, link);
			}
		}

		return FAKE_VALUE;
	}

	@Nonnull
	@Override
	public Double calcValueMissing(EntityAnimal parent, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		for (int i : link.assignedGenes) {
			GenePair pair = strand.strandValues[i];
			float[] probability = pool.probabilities[i];
			if (probability[pair.a.ordinal()] < 1e-4 || probability[pair.b.ordinal()] < 1e-4) {
				strand.strandValues[i] = GenePool.calculateGenePair(parent.worldObj.rand, probability);
			}
		}
		return FAKE_VALUE;
	}

	@Override
	public void addDrops(EntityLivingBase base, List<EntityItem> drops, double value) {
		if (drops.isEmpty() || FAKE_VALUE.equals(value)) return;
		if (value > 0) {
			ItemStack stack;
			stack = createDrop(base);
			int v = (int) roundFloorRand(value, base.getRNG());
			while (v > 0) {
				stack.stackSize = Math.min(v, stack.getMaxStackSize());
				v -= stack.stackSize;
				drops.add(new EntityItem(base.worldObj, base.posX, base.posY, base.posZ, stack));
			}
		}
	}

	@Override
	public float[][] assignWeightsOveride(GenePool<EntityAnimal> pool, TIntArrayList assignedGenes, Random rand) {
		float[][] weights = new float[assignedGenes.size()][15];
		float max = 0;
		for (int i = 0; i < weights.length; i++) {
			int j = assignedGenes.get(i);
			float[] weight = weights[i];
			float[] pr = GenePool.genNaturalProbabilites(pool.probabilities[j]);

			float t = rand.nextFloat();
			float m = t;
			for (int k = 0; k < pr.length; k++) {
				if (pr[k] < 1e-5) {
					weight[k] = t * rand.nextFloat() * 1.5F;
					m = Math.max(m, weight[k]);
				}
			}

			max += m;
		}

		float add = (float) (low / weights.length);
		float mult = (float) ((upper - low) / max);

		for (float[] weight : weights) {
			for (int j = 0; j < weight.length; j++) {
				weight[j] = add + weight[j] * mult;
			}
		}

		return weights;
	}
}
