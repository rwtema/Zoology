package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.utils.NBTSerializer;
import gnu.trove.list.array.TIntArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public abstract class PhenotypeLevels<T extends Enum<T>, V extends EntityAnimal> extends Phenotype<T, V> {
	final T[] levels;
	final float[] thresholds;
	final EnumSet<T> negativeLevels;

	public PhenotypeLevels(String name, Class<V> entityClazz, int reserved_genes, int random_genes, T[] levels, float[] thresholds, EnumSet<T> negativeLevels) {
		super(name, levels[0].getDeclaringClass(), entityClazz, new NBTSerializer.Enum<T>(levels[0].getDeclaringClass()), reserved_genes, random_genes, 0);
		this.levels = levels;
		this.thresholds = thresholds;
		this.negativeLevels = negativeLevels;
	}


	@Override
	public T initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		int i = Arrays.binarySearch(thresholds, (float) v);
		if (i < 0)
			return levels[0];
		else
			return levels[i];
	}

	@Override
	public float[][] assignWeightsOveride(GenePool<V> pool, TIntArrayList assignedGenes, Random rand) {
		float[][] weights = new float[assignedGenes.size()][];

		float maxTotal = 0;
		for (int i = 0; i < assignedGenes.size(); i++) {
			int pi = assignedGenes.get(i);

			weights[i] = new float[15];
			float pr[] = GenePool.genNaturalProbabilites(pool.probabilities[pi]);

			for (int j = 0; j < 5; j++) {
				weights[i][j] = (float) -Math.log(rand.nextDouble());
			}

			for (int j = 0; j < 10; j++) {
				GenePair pair = GenePair.Cache.geneHetero[j];
				float a = weights[i][pair.a.ordinal()];
				float b = weights[i][pair.b.ordinal()];
				weights[i][j + 5] = Math.max(0, (float) -Math.log(
						(Math.exp(a) + Math.exp(b)) / 2
								+ getInbreedingModifier(rand)
				));

			}


			float min = Float.MAX_VALUE;
			for (int j = 0; j < weights.length; j++) {
				min = Math.min(weights[i][j], min);
			}

			for (int j = 0; j < weights.length; j++) {
				weights[i][j] -= min;
			}

			float max = 0;
			float max_div = 0;
			for (int j = 0; j < 5; j++) {
				max += weights[i][j] * pr[j];
				max_div += pr[j];
			}
			maxTotal += max / max_div;
		}

		maxTotal *= 0.99999F;

		for (float[] weight : weights) {
			for (int i = 0; i < weight.length; i++) {
				weight[i] /= maxTotal;
			}
		}

		return weights;
	}

	@Override
	public ITextComponent buildComponent(T value) {
		return getDisplayName();
	}

	@Override
	public ITextComponent getDisplayValue(T value) {
		TextComponentTranslation iTextComponents = new TextComponentTranslation("zoology.phenotype." + name + "." + value.name().toLowerCase());
		if(negativeLevels.contains(value)){
			iTextComponents.getStyle().setColor(TextFormatting.RED);
		}
		return iTextComponents;
	}
}
