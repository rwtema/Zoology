package com.rwtema.zoology.ai;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenotypes.Phenotypes;
import com.rwtema.zoology.potion.PotionClone;
import com.rwtema.zoology.potion.PotionSelfImpregnation;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityAICustomMate extends EntityAIMate {
	public final PheneList<EntityAnimal> pheneList;

	public EntityAICustomMate(EntityAnimal animal, double speedIn) {
		super(animal, speedIn);
		pheneList = animal.getCapability(PheneList.CAPABILITY, null);
	}

	public EntityAICustomMate(EntityAIMate ai) {
		this(ai.theAnimal, ai.moveSpeed);
		spawnBabyDelay = ai.spawnBabyDelay;
	}

	public static EntityAgeable mateAnimals(EntityAnimal theAnimal, EntityAnimal targetMate, boolean doXPDrops) {
		boolean autoBreeding = theAnimal == targetMate;
		if (!isFertile(theAnimal) || !isFertile(targetMate)) {
			theAnimal.resetInLove();
			targetMate.resetInLove();
			return null;
		}

		EntityAgeable child = theAnimal.createChild(targetMate);

		if (child == null) return null;

		EntityPlayer entityplayer = theAnimal.getPlayerInLove();

		if (entityplayer == null && targetMate.getPlayerInLove() != null) {
			entityplayer = targetMate.getPlayerInLove();
		}

		if (entityplayer != null) {
			entityplayer.addStat(StatList.ANIMALS_BRED);

			if (theAnimal instanceof EntityCow) {
				entityplayer.addStat(AchievementList.BREED_COW);
			}
		}

		PheneList childPheneList = child.getCapability(PheneList.CAPABILITY, null);

		if (autoBreeding && theAnimal.getActivePotionEffect(PotionClone.INSTANCE) != null) {
			childPheneList.generateClone(theAnimal);
		} else {
			childPheneList.generate(theAnimal, targetMate, theAnimal.worldObj.rand);
		}

		resetLoveTimer(theAnimal);
		resetLoveTimer(targetMate);

		theAnimal.resetInLove();
		targetMate.resetInLove();

		child.setGrowingAge(-((Double) childPheneList.getValue(Phenotypes.GROWING_AGE)).intValue());
		double posX = (theAnimal.posX + targetMate.posX) / 2;
		double posY = Math.max(theAnimal.posY, targetMate.posY);
		double posZ = (theAnimal.posZ + targetMate.posZ) / 2;
		child.setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
		theAnimal.worldObj.spawnEntityInWorld(child);

		Random random = theAnimal.getRNG();

		for (int i = 0; i < 7; ++i) {
			double d0 = random.nextGaussian() * 0.02D;
			double d1 = random.nextGaussian() * 0.02D;
			double d2 = random.nextGaussian() * 0.02D;
			double d3 = random.nextDouble() * (double) theAnimal.width * 2.0D - (double) theAnimal.width;
			double d4 = 0.5D + random.nextDouble() * (double) theAnimal.height;
			double d5 = random.nextDouble() * (double) theAnimal.width * 2.0D - (double) theAnimal.width;
			theAnimal.worldObj.spawnParticle(EnumParticleTypes.HEART, posX + d3, posY + d4, posZ + d5, d0, d1, d2);
		}

		if (doXPDrops && theAnimal.worldObj.getGameRules().getBoolean("doMobLoot")) {
			spawnXP(theAnimal.worldObj, posX, posY, posZ, random.nextInt(7) + 1);
		}
		return child;
	}

	public static void spawnXP(World worldObj, double posX, double posY, double posZ, int expValue) {
		List<EntityXPOrb> list = worldObj.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(posX, posY, posZ, posX, posY, posZ).expandXyz(8));
		if (list.size() < 8) {
			worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY, posZ, expValue));
		} else {
			EntityXPOrb smallest = null;
			for (EntityXPOrb orb : list) {
				if (smallest == null || orb.getXpValue() < smallest.getXpValue()) {
					smallest = orb;
				}
			}
			if (smallest != null) {
				smallest.xpValue += expValue;
			}
		}

	}

//	public static int getNumOffSpring(EntityAnimal animal) {
//		PheneList<EntityAnimal> pheneList = animal.getCapability(PheneList.CAPABILITY, null);
//		return (int) Math.floor(pheneList.getValue(Phenotypes.OFFSPRING));
//	}

	private static void resetLoveTimer(EntityAnimal animal) {
		PheneList<EntityAnimal> pheneList = animal.getCapability(PheneList.CAPABILITY, null);
		int i = pheneList.getValue(Phenotypes.LOVING_AGE).intValue();
		animal.setGrowingAge(i);
	}

	public static boolean isFertile(EntityAnimal animal) {
		PheneList<?> capability = animal.getCapability(PheneList.CAPABILITY, null);
		return !capability.getValue(Phenotypes.INFERTILE);
	}

	public static void spawnBabies(EntityAnimal theAnimal, EntityAnimal targetMate) {
		int numOffSpring = 1;//Math.max(1, 1 + Math.min(getNumOffSpring(theAnimal), getNumOffSpring(targetMate)));

		for (int i = 0; i < numOffSpring; i++) {
			mateAnimals(theAnimal, targetMate, i == 0);
		}
	}

	@Override
	public EntityAnimal getNearbyMate() {
		List<EntityAnimal> list = this.theWorld.getEntitiesWithinAABB(this.theAnimal.getClass(), this.theAnimal.getEntityBoundingBox().expandXyz(8.0D));
		double d0 = Double.MAX_VALUE;
		EntityAnimal entityanimal = null;

		for (EntityAnimal entityanimal1 : list) {
			if (this.theAnimal.canMateWith(entityanimal1) && this.theAnimal.getDistanceSqToEntity(entityanimal1) < d0) {
				entityanimal = entityanimal1;
				d0 = this.theAnimal.getDistanceSqToEntity(entityanimal1);
			}
		}

		return entityanimal;
	}

	public boolean isGoodPosition(EntityAgeable entity) {
		return true;
	}

	@Override
	public void spawnBaby() {
		spawnBabies(this.theAnimal, this.targetMate);
	}

	@Override
	public boolean shouldExecute() {
		if (!this.theAnimal.isInLove()) {
			return false;
		}

		if (theAnimal.getActivePotionEffect(PotionClone.INSTANCE) != null || theAnimal.getActivePotionEffect(PotionSelfImpregnation.INSTANCE) != null) {
			targetMate = theAnimal;
			return true;
		}

		this.targetMate = this.getNearbyMate();
		return this.targetMate != null;
	}

	@Override
	public void updateTask() {
		++this.spawnBabyDelay;

		double matingRange = Math.max(3, 2 * (theAnimal.width + targetMate.width));
		matingRange = matingRange * matingRange;

		if (this.spawnBabyDelay >= 60 && (theAnimal == targetMate || this.theAnimal.getDistanceSqToEntity(this.targetMate) < matingRange)) {
			this.spawnBaby();
		}

		if (theAnimal != targetMate) {
			this.theAnimal.getLookHelper().setLookPositionWithEntity(this.targetMate, 10.0F, (float) this.theAnimal.getVerticalFaceSpeed());
//			if (this.theAnimal.getDistanceSqToEntity(this.targetMate) >= matingRange) {
				this.theAnimal.getNavigator().tryMoveToEntityLiving(this.targetMate, this.moveSpeed);
//			}
//			else {
//				if (this.theAnimal.getRNG().nextFloat() < 0.4F) {
//					theAnimal.getJumpHelper().setJumping();
//				}
//			}
		}else{
			if (this.theAnimal.getRNG().nextFloat() < 0.8F) {
				theAnimal.getJumpHelper().setJumping();
			}
		}
	}

	@Override
	public boolean continueExecuting() {
		if (targetMate == theAnimal) {
			return theAnimal.isInLove() && (theAnimal.getActivePotionEffect(PotionClone.INSTANCE) != null || theAnimal.getActivePotionEffect(PotionSelfImpregnation.INSTANCE) != null);
		}
		return this.targetMate.isEntityAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
	}
}
