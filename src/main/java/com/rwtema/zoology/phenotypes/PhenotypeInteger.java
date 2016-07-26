package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.utils.NBTSerializer;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.MathHelper;

public class PhenotypeInteger extends Phenotype<Integer, EntityAnimal> {

	private final int mid;
	private final int low;
	private final int upper;

	protected PhenotypeInteger(String name, int reserved_genes, int random_genes, int low, int upper) {
		super(name, Integer.class, EntityAnimal.class, NBTSerializer.INTEGER, reserved_genes, random_genes, Math.abs(upper - low) / 2);
		this.low = low;
		this.upper = upper;
		this.mid = (low + upper) / 2;
	}

	@Override
	public void initApply(EntityAnimal entity, Integer value) {

	}

	@Override
	public Integer initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		return MathHelper.clamp_int((int) (mid + v), low, upper);
	}
}
