package com.rwtema.zoology.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;

public class ItemMagnifyingGlass extends Item {
	public ItemMagnifyingGlass() {
		setUnlocalizedName("zoology.magnifying_glass");
		setCreativeTab(CreativeTabs.TOOLS);
		setMaxStackSize(1);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
//		if (playerIn.worldObj.isRemote) return true;

//		if(target instanceof EntityAnimal){
//			playerIn.addChatComponentMessage(new TextComponentString("" + ((EntityAnimal) target).getGrowingAge()));
//			return true;
//		}
//		if (playerIn.worldObj.isRemote) return true;
//		if (target.hasCapability(PheneList.CAPABILITY, null)) {
//			PheneList<?> capability = target.getCapability(PheneList.CAPABILITY, null);
//			ITextComponent chat = new TextComponentString("~~~~~~~\n");
//			chat.appendSibling(target.getDisplayName());
//			chat.appendText("\n~~~~~~~");
////			GenePool pool = EntityRegistry.getGenePool(target.getClass());
////			GeneticStrand strand = ((PheneList) capability).getStrand(target, pool);
////
////			int n = 0;
////			for (GenePair value : strand.strandValues) {
////				if (value == GenePair.AB) {
////					n++;
////				}
////			}
////			chat.appendText("\n" + n + "\n");
//
//			for (Phenotype phenotype : capability.getRegisteredPhenes()) {
//				Object value = capability.getValue(phenotype);
//				ITextComponent iTextComponent = phenotype.buildComponent(value);
//				if(iTextComponent != null) {
//					chat.appendText("\n");
//					chat.appendSibling(iTextComponent);
//
//				}
//			}
//			playerIn.addChatComponentMessage(chat);
//		}

		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}

}
