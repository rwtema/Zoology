package com.rwtema.zoology.phenotypes;

import com.google.common.collect.Lists;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenes.Phenotype;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

public class PhenotypeDrop extends PhenotypeDouble {
	static {
		MinecraftForge.EVENT_BUS.register(new DropHandler());
	}

	final ItemStack drop;
	final ArrayList<ItemStack> equivalentDrops = Lists.newArrayList();
	ItemStack burningDrop = null;

	public PhenotypeDrop(String name, ItemStack drop) {
		this(name, drop, 12, 16, 32);
	}

	public PhenotypeDrop(String name, ItemStack drop, double limit, int reserved_genes, int random_genes) {
		this(name, drop, -limit, limit, reserved_genes, random_genes);
	}

	public PhenotypeDrop(String name, ItemStack drop, double lower, double upper, int reserved_genes, int random_genes) {
		super("drop_" + name, reserved_genes, random_genes, lower, upper);
		this.drop = getUnwildStack(drop);
		equivalentDrops.add(drop.copy());
	}

	private static ItemStack getUnwildStack(ItemStack drop) {
		ItemStack newDrop;
		if (drop.getMetadata() == OreDictionary.WILDCARD_VALUE) {
			newDrop = new ItemStack(drop.getItem(), 1, 0);
			if (drop.hasTagCompound()) {
				newDrop.setTagCompound((NBTTagCompound) drop.getTagCompound().copy());
			}
		} else
			newDrop = drop.copy();
		return newDrop;
	}


	static int roundFloorRand(double v, Random rand) {
		if (v < 0) return -roundFloorRand(-v, rand);
		int k = (int) Math.floor(v);
		double p = v - k;
		if (p >= 1e-8 && rand.nextFloat() < p) {
			return k + 1;
		}

		return k;
	}

	public PhenotypeDrop addEquivalentDrop(ItemStack newDrop) {
		equivalentDrops.add(newDrop.copy());
		return this;
	}

	public PhenotypeDrop setBurningDrop(ItemStack drop) {
		burningDrop = drop.copy();
		equivalentDrops.add(drop);
		return this;
	}

	@Override
	public void initApply(EntityAnimal entity, Double value) {

	}

	@Override
	public Double initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		return v;
	}

	@Override
	public ITextComponent getDisplayValue(Double value) {
		return new TextComponentString(String.format("%.2g", value));
	}

	@Override
	public ITextComponent getDisplayName() {
		ITextComponent oc = new TextComponentTranslation("zoology.phenotype.drop");
		return oc.appendText(" ").appendSibling(drop.getTextComponent());
	}

	@Override
	public ITextComponent buildComponent(Double value) {
		ITextComponent oc = getDisplayName();
		oc.appendText(": ");
//		oc.appendText(String.format("%d  (", (int) (Math.floor(value))));
		oc.appendSibling(getDisplayValue(value));
//		oc.appendText(")");
		return oc;
	}

	public void addDrops(EntityLivingBase base, List<EntityItem> drops, double value) {
		if (value < 0) {
			int v = (int) roundFloorRand(-value, base.getRNG());
			int total = 0;
			for (EntityItem item : drops) {
				ItemStack stack = item.getEntityItem();
				if (stack != null) {
					for (ItemStack equivalentDrop : equivalentDrops) {
						if (OreDictionary.itemMatches(stack, equivalentDrop, false)) {
							total += stack.stackSize;
						}
					}
				}
			}
			if (total <= 1) return;

			int toRemove = Math.min(v, total - 1);

			for (Iterator<EntityItem> iterator = drops.iterator(); iterator.hasNext(); ) {
				EntityItem item = iterator.next();
				ItemStack stack = item.getEntityItem();
				for (ItemStack equivalentDrop : equivalentDrops) {
					if (OreDictionary.itemMatches(stack, equivalentDrop, false)) {
						int v1 = stack.stackSize - toRemove;
						if (v1 <= 0) {
							toRemove -= stack.stackSize;
							iterator.remove();
						} else {
							stack.stackSize = v1;
							item.setEntityItemStack(stack);
							return;
						}

						break;
					}
				}
			}
		} else if (value > 0) {
			boolean flag = false;
			searchForExistingItem:
			for (EntityItem item : drops) {
				ItemStack stack = item.getEntityItem();
				if (stack == null) continue;
				for (ItemStack itemStack : equivalentDrops) {
					if (OreDictionary.itemMatches(stack, itemStack, false)) {
						flag = true;
						break searchForExistingItem;
					}
				}
			}

			if (!flag) return;

			ItemStack stack;
			stack = createDrop(base);
			int v = (int) roundFloorRand(value, base.getRNG());
			while (v > 0) {
				stack.stackSize = Math.min(v, stack.getMaxStackSize());
				v -= stack.stackSize;
				drops.add(new EntityItem(base.worldObj, base.posX, base.posY, base.posZ, stack));
			}
		}
	}

	protected ItemStack createDrop(EntityLivingBase base) {
		ItemStack stack;
		if (burningDrop != null && base.isBurning()) {
			stack = burningDrop.copy();
		} else {
			stack = drop.copy();
		}
		return stack;
	}

	private static class DropHandler {
		@SubscribeEvent(priority = EventPriority.HIGH)
		public void onDeath(LivingDropsEvent event) {
			EntityLivingBase base = event.getEntityLiving();
			if (base.worldObj.isRemote) return;
			if (base.hasCapability(PheneList.CAPABILITY, null)) {
				PheneList<?> list = base.getCapability(PheneList.CAPABILITY, null);
				for (Phenotype phenotype : list.getRegisteredPhenes()) {
					if (phenotype instanceof PhenotypeDrop) {
						double value = list.getValue((PhenotypeDouble) phenotype);
						((PhenotypeDrop) phenotype).addDrops(base, event.getDrops(), value);
					}
				}
			}

		}
	}
}
