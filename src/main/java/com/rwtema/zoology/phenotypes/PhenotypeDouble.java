package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.utils.NBTSerializer;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.Validate;

public class PhenotypeDouble extends Phenotype<Double, EntityAnimal> {

	protected final double mid;
	protected final double low;
	protected final double upper;

	public PhenotypeDouble(String name, int reserved_genes, int random_genes, double low, double upper) {
		super(name, Double.class, EntityAnimal.class, NBTSerializer.DOUBLE, reserved_genes, random_genes, Math.abs(upper - low) / 2);
		this.low = low;
		this.upper = upper;
		this.mid = (low + upper) / 2;
	}

	@Override
	public void initApply(EntityAnimal entity, Double value) {

	}

	@Override
	public Double initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		return MathHelper.clamp_double(mid + v, Math.min(low, upper), Math.max(low, upper));
	}

	@Override
	public ITextComponent getDisplayValue(Double value) {
		return new TextComponentString(String.format("%.2g", value));
	}

	public static class Exp extends PhenotypeDouble {
		final boolean negativeSign;

		protected Exp(String name, int reserved_genes, int random_genes, double low, double upper) {
			super(name, reserved_genes, random_genes, Math.log(Math.abs(low)), Math.log(Math.abs(upper)));
			Validate.isTrue(Math.signum(low) == Math.signum(upper));
			negativeSign = Math.signum(low) == -1;
		}

		@Override
		public Double initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link) {
			double value = Math.exp(super.initValue(v, strand, pool, link));
			return negativeSign ? -value : value;
		}
	}
}
