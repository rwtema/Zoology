package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class PhenotypeUnnaturalFlag extends PhenotypeFlag {
	protected PhenotypeUnnaturalFlag(String name, boolean isNegative, int reserved_genes) {
		super(name, isNegative, reserved_genes);
	}

	@Override
	public void onReserve(GenePool<EntityAnimal> pool, int i, Random rand) {
		float[] probability = pool.probabilities[i];
		int k = rand.nextInt(5);
		float sum = 0;
		for (int j = 0; j < 5; j++) {
			float v = j == k ? 0 : (float) -Math.log(rand.nextDouble());
			sum += v;
			probability[j] = v;
		}

		for (int j = 0; j < probability.length; j++) {
			probability[j] /= sum;
		}

		pool.probabilities[i] = probability;
	}

	@Nonnull
	@Override
	public Boolean calcValueMissing(EntityAnimal parent, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		for (int i : link.assignedGenes) {
			GenePair pair = strand.strandValues[i];
			float[] probability = pool.probabilities[i];
			if (probability[pair.a.ordinal()] < 1e-4 || probability[pair.b.ordinal()] < 1e-4) {
				strand.strandValues[i] = GenePool.calculateGenePair(parent.worldObj.rand, probability);
			}
		}

		return Boolean.FALSE;
	}

	@Nonnull
	@Override
	public Boolean calcValue(GeneticStrand strand, GenePool<EntityAnimal> pool, GenePool.Link link) {
		for (int assignedGene : link.assignedGenes) {
			GenePair strandValue = strand.strandValues[assignedGene];
			float[] probability = pool.probabilities[assignedGene];
			if (probability[strandValue.a.ordinal()] >= 1e-8 && probability[strandValue.b.ordinal()] >= 1e-8) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ITextComponent getDisplayValue(Boolean value) {
		return value ? new TextComponentTranslation("zoology.phenotype.flag." + name) : null;
	}
}
