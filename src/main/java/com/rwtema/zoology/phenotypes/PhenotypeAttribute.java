package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.LogHelper;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.utils.NBTSerializer;
import java.util.UUID;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class PhenotypeAttribute extends Phenotype<Double, EntityAnimal> {

	private static final UUID ZOOLOGY_UUID = UUID.fromString("33AC48C7-1BCF-4078-9342-24616556D27A");
	public final int op = 1;
	public final IAttribute attribute;

	public PhenotypeAttribute(IAttribute attribute, int reserved_genes, int random_genes, double max_range) {
		super(attribute.getAttributeUnlocalizedName().substring("generic.".length()), Double.class, EntityAnimal.class, NBTSerializer.DOUBLE, reserved_genes, random_genes, max_range);
		this.attribute = attribute;
	}

	@Override
	public ITextComponent getDisplayValue(Double value) {
		return new TextComponentString(String.format("%+.2g%%", (value - 1) * 100));
	}

	@Override
	public void initApply(EntityAnimal entity, Double value) {
		IAttributeInstance instance = entity.getEntityAttribute(attribute);
		double v = 0;
		boolean isDeObf = LogHelper.isDeObf;
		if (isDeObf) {
			v = instance.getAttributeValue();
		}

		AttributeModifier modifier = instance.getModifier(ZOOLOGY_UUID);
		if (modifier != null) {
			instance.removeModifier(ZOOLOGY_UUID);
		}

		instance.applyModifier(new AttributeModifier(ZOOLOGY_UUID, "Zoology", Math.round(value * 100) / 100F - 1, 1));
		if (isDeObf) {
			double v2 = instance.getAttributeValue();
			LogHelper.info(v2 + " " + v + " " + (v2 - v));
		}
	}

	@Override
	public Double initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		return Math.exp(v);
	}
}
