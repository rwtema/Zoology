package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.utils.NBTSerializer;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public abstract class PhenotypeFlag extends Phenotype<Boolean, EntityAnimal> {
	private boolean isNegative;

	protected PhenotypeFlag(String name, boolean isNegative) {
		this(name, isNegative, 1);
	}

	protected PhenotypeFlag(String name, boolean isNegative, int reserved_genes) {
		super(name, Boolean.class, EntityAnimal.class, NBTSerializer.BOOLEAN, reserved_genes, 0, 1);
		this.isNegative = isNegative;
	}


	@Override
	public abstract void onReserve(GenePool<EntityAnimal> pool, int i, Random rand);

	@Nonnull
	@Override
	public abstract Boolean calcValue(GeneticStrand strand, GenePool<EntityAnimal> pool, GenePool.Link link);

	@Override
	public void initApply(EntityAnimal entity, Boolean value) {

	}

	@Override
	public Boolean initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		return v > 0;
	}

	@Override
	public ITextComponent buildComponent(Boolean value) {
		ITextComponent displayValue = getDisplayValue(value);
		if (isNegative == value)
			displayValue.getStyle().setColor(TextFormatting.RED);
		return displayValue;
	}

	@Override
	public ITextComponent getDisplayValue(Boolean value) {
		return new TextComponentTranslation("zoology.phenotype.flag." + name + "." + (value ? "on" : "off"));
	}
}
