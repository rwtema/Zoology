package com.rwtema.zoology.potion;

import com.rwtema.zoology.Zoology;
import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.genes.Gene;
import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenotypes.Phenotypes;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.text.TextComponentTranslation;

public class PotionRadiation extends Potion {
	public static final PotionRadiation INSTANCE = new PotionRadiation();
	public static final PotionType type = new PotionType(new PotionEffect(INSTANCE, 1));

	public PotionRadiation() {
		super(true, 0x242020);
		setPotionName("effect.zoology.radiation");
	}

	@Override
	public boolean isInstant() {
		return true;
	}

	@Override
	public void performEffect(EntityLivingBase entityLivingBaseIn, int p_76394_2_) {
		corruptDNA(entityLivingBaseIn);
	}

	@Override
	public void affectEntity(Entity source, Entity indirectSource, EntityLivingBase target, int amplifier, double health) {
		corruptDNA(target);
	}

	private void corruptDNA(EntityLivingBase target) {
		if (target.worldObj.isRemote) {
			return;
		}
		if (target instanceof EntityPlayer) {
			NBTTagCompound data = target.getEntityData();
			boolean radioactive = data.getBoolean("Radioactive");
			if (radioactive) return;
			if (target.worldObj.rand.nextInt(5) == 0) {
				((EntityPlayer) target).addChatComponentMessage(new TextComponentTranslation("zoology.mutation.message"));
				data.setBoolean("Radioactive", true);
			}
		} else if (target instanceof EntityAnimal) {
			EntityAnimal animal = (EntityAnimal) target;
			if (!animal.hasCapability(PheneList.CAPABILITY, null)) return;
			PheneList pheneList = animal.getCapability(PheneList.CAPABILITY, null);
			GenePool<?> pool = EntityGeneRegistry.getGenePool(animal.getClass());

			GeneticStrand strand = pheneList.getStrand(animal, pool);
			Random rand = animal.worldObj.rand;

			GenePair[] values = strand.strandValues;
			for (int i = 0; i < values.length; i++) {
				GenePair pair = values[i];
				Gene a = rand.nextInt(6) == 0 ? Gene.rand(rand) : pair.a;
				Gene b = rand.nextInt(6) == 0 ? Gene.rand(rand) : pair.b;
				values[i] = GenePair.create(a, b);
			}

			PheneList.saveGenesToNBT(strand, animal);
			if (!((Boolean) pheneList.getValue(Phenotypes.INFERTILE))) {
				pheneList.reloadPhenotypeValue(pool, strand, Phenotypes.INFERTILE);
			}

			if (Zoology.isDeobf)
				pheneList.reloadPhenotypeValue(pool, strand, Phenotypes.INBRED);
		}
	}

	public boolean isReady(int p_76397_1_, int p_76397_2_) {
		return p_76397_1_ >= 1;
	}
}
