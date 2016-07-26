package com.rwtema.zoology.phenotypes;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class PhenotypeAggressive<V extends EntityAnimal> extends PhenotypeLevels<PhenotypeAggressive.Level, V> {
	final double attack ;

	public PhenotypeAggressive(Class<V> entityAnimalClass, double attack) {
		super("Aggression", entityAnimalClass, 30, 0,
				new Level[]{Level.DOCILE, Level.DEFENSIVE, Level.HOSTILE, Level.CANNIBAL},
				new float[]{0, 0.75F, 1F, 1.2F},
				EnumSet.of(Level.DEFENSIVE, Level.HOSTILE, Level.CANNIBAL));
		this.attack = attack;
	}


	@Override
	protected void onLoad(V entity, Level level, NBTTagCompound tag) {
		addAI(entity, level);
	}

	private void addAI(V entity, Level level) {
		if (level == Level.DOCILE)
			return;
		AbstractAttributeMap attributeMap = entity.getAttributeMap();
		//noinspection ConstantConditions
		if(attributeMap.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
			attributeMap.registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(attack);
		}

		entity.tasks.addTask(2, new EntityAIAttackMelee(entity, 1, false));
		entity.targetTasks.addTask(6, new EntityAIHurtByTarget(entity, false));
		if (level.clazz != null) {
			entity.targetTasks.addTask(7, new EntityAINearestAttackableTarget(entity, level.clazz, false));
		}
	}

	@Override
	public void initApply(V entity, Level value) {
		addAI(entity, value);
	}

	enum Level {
		DOCILE(null),
		DEFENSIVE(null),
		HOSTILE(EntityPlayer.class),
		CANNIBAL(EntityAnimal.class);

		private final Class<? extends Entity> clazz;

		Level(Class<? extends Entity> clazz) {
			this.clazz = clazz;
		}
	}
}
