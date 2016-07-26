package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.Phenotype;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseType;

class PhenotypeInfertile extends PhenotypeFlag {
	public PhenotypeInfertile() {
		super("infertile", true, 4);
	}

	@Override
	public void onCombine(GeneticStrand strand, EntityAnimal parent, GeneticStrand parent1, GeneticStrand parent2, Set<Phenotype> set, GenePool<EntityAnimal> pool) {
		super.onCombine(strand, parent, parent1, parent2, set, pool);
		checkForInfertility(strand.strandValues, parent, pool, pool.getPheneLink(this));
	}

	@Override
	public void onReserve(GenePool<EntityAnimal> pool, int i, Random rand) {
		float[] probability = new float[5];

		for (int i1 = 0; i1 < 4; i1++) {
			probability[rand.nextInt(5)] = 1;
		}
		float n = 0;
		for (float v : probability) {
			n += v;
		}
		for (int i1 = 0; i1 < probability.length; i1++) {
			probability[i1] /= n;
		}
		pool.probabilities[i] = probability;
	}

	@Nonnull
	@Override
	public Boolean calcValue(GeneticStrand strand, GenePool<EntityAnimal> pool, GenePool.Link link) {
		for (int i : link.assignedGenes) {
			GenePair pair = strand.strandValues[i];
			float[] probability = pool.probabilities[i];
			if (probability[pair.a.ordinal()] < 1e-4 && probability[pair.b.ordinal()] < 1e-4) {
				return true;
			}
		}

		return false;
	}

	@Nonnull
	@Override
	public Boolean calcValueMissing(EntityAnimal parent, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		if (parent.getClass() == EntityHorse.class) {
			EntityHorse horse = (EntityHorse) parent;
			HorseType type = horse.getType();
			if (!type.canMate()) {
				for (int i : link.assignedGenes) {
					float[] probability = pool.probabilities[i];
					for (int j = 0; j < 5; j++) {
						if (probability[j] < 1e-5) {
							strand.strandValues[i] = GenePair.Cache.geneHetero[j];
							if (horse.getRNG().nextBoolean())
								break;
						}
					}
				}
				return Boolean.TRUE;
			}
		}


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
	public GenePool.Link onWeightsGenerated(GenePool<EntityAnimal> entityAnimalGenePool, @Nonnull GenePool.Link link) {
		return super.onWeightsGenerated(entityAnimalGenePool, link);
	}

	@Override
	public void overwriteGeneratedGeneValues(GenePair[] pairs, EntityAnimal parent, GenePool<EntityAnimal> pool, GenePool.Link link) {
		checkForInfertility(pairs, parent, pool, link);
	}

	private void checkForInfertility(GenePair[] pairs, Entity parent, GenePool<EntityAnimal> pool, GenePool.Link link) {
		if (parent.getClass() == EntityHorse.class) {
			EntityHorse horse = (EntityHorse) parent;
			HorseType type = horse.getType();
			if (!type.canMate()) {

				for (int i : link.assignedGenes) {
					float[] probability = pool.probabilities[i];
					for (int j = 0; j < 5; j++) {
						if (probability[j] < 1e-5) {
							pairs[i] = GenePair.Cache.geneHetero[j];
							if (horse.getRNG().nextBoolean())
								break;
						}
					}
				}
			}
		}
	}

}
