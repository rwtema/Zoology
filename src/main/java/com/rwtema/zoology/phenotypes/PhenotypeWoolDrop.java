package com.rwtema.zoology.phenotypes;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class PhenotypeWoolDrop extends PhenotypeDrop {
	public PhenotypeWoolDrop() {
		super("sheep_wool", new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE), 8, 32, 8);
	}

	@Override
	protected ItemStack createDrop(EntityLivingBase base) {
		return new ItemStack(Blocks.WOOL, 1, ((EntitySheep) base).getFleeceColor().getMetadata());
	}
}
