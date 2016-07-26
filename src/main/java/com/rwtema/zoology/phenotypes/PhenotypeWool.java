package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.LogHelper;
import com.rwtema.zoology.animals.DyeGenetics;
import com.rwtema.zoology.genes.Dominance;
import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.utils.NBTSerializer;
import java.util.Random;
import java.util.Set;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class PhenotypeWool extends Phenotype<EnumDyeColor, EntitySheep> {
	protected PhenotypeWool() {
		super("wool", EnumDyeColor.class, EntitySheep.class, new NBTSerializer.Enum(EnumDyeColor.class), 3, 0, 1);
	}

	@Override
	public void overwriteGeneratedGeneValues(GenePair[] pairs, EntitySheep parent, GenePool<EntitySheep> pool, GenePool.Link link) {
		int[] assignedGenes = link.assignedGenes;
		GenePair[] randomGenes = DyeGenetics.getRandomGenes(parent.getFleeceColor(), parent.getRNG());

		for (int i = 0; i < 3; i++) {
			int j = assignedGenes[i];
			pairs[j] = randomGenes[i];
		}
	}

	@Override
	public void onReserve(GenePool<EntitySheep> pool, int i, Random rand) {

	}

	@Override
	public void initApply(EntitySheep entity, EnumDyeColor value) {
		entity.setFleeceColor(value);
	}

	@Override
	public ITextComponent getDisplayValue(EnumDyeColor value) {
		return new TextComponentTranslation("zoology.color." + value);
	}

	@Override
	public void onCombine(GeneticStrand strand, EntitySheep parent, GeneticStrand parent1, GeneticStrand parent2, Set<Phenotype> set, GenePool<EntitySheep> pool) {
//		GenePool.Link pheneLink = pool.getPheneLink(this);


	}

	@Override
	public EnumDyeColor initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		GenePair[] genes = new GenePair[3];
		for (int i = 0; i < 3; i++) {
			int j = link.assignedGenes[i];
			genes[i] = strand.strandValues[j];
		}

		return DyeGenetics.getColor(genes);
	}

}
