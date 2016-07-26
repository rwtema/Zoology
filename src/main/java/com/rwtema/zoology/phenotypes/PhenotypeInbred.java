package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import gnu.trove.list.array.TIntArrayList;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class PhenotypeInbred extends PhenotypeDouble {

	protected PhenotypeInbred() {
		super("inbreeding_level", 0, 0, 0, 1);
	}

	@Override
	public void onReserve(GenePool<EntityAnimal> pool, int i, Random rand) {

	}

	@Override
	public float[][] assignWeightsOveride(GenePool<EntityAnimal> pool, TIntArrayList assignedGenes, Random rand) {
		return new float[0][0];
	}

	@Nonnull
	@Override
	public Double calcValue(GeneticStrand strand, GenePool<EntityAnimal> pool, GenePool.Link link) {
		GenePair[] strandValues = strand.strandValues;

		float n = 0, x = 0, v = 0;

		for (int i = 0; i < strandValues.length; i++) {
			float[] probabilites = GenePool.genNaturalProbabilites(pool.probabilities[i]);
			if (!strandValues[i].isHomo()) {
				x++;
			}

			float p = 0;
			for (int j = 5; j < probabilites.length; j++) {
				p += probabilites[j];
			}

			n += p;
			v += p * (1 - p);
		}

		return (x - n) / Math.sqrt(v);
	}

	@Override
	public ITextComponent buildComponent(Double value) {
		Stage stage = getStage(value);
		if (stage == null) return null;
		ITextComponent component = new TextComponentTranslation(stage.key);
		component.getStyle().setColor(TextFormatting.RED);
		return component;
	}

	private Stage getStage(Double value) {
		Stage s = null;
		for (Stage stage : Stage.values()) {
			if (value <= stage.threshold) {
				s = stage;
			} else
				break;
		}
		return s;
	}


	@Override
	public ITextComponent getDisplayValue(Double value) {
		Stage stage = getStage(value);
		if (stage == null) return null;
		return new TextComponentTranslation(stage.key);
	}

	enum Stage {
		MINOR(-5),
		SIGNIFICANT(-10),
		MAJOR(-16),
		ROYAL(-24);


		private final double threshold;
		private final String key;

		Stage(double threshold) {
			this.threshold = threshold;
			key = "zoology.phenotype.inbred." + this.name().toLowerCase();
		}
	}
}
