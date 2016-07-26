package com.rwtema.zoology.entities;

import com.google.common.base.Function;
import com.rwtema.zoology.ai.EntityAICustomEatGrass;
import com.rwtema.zoology.ai.EntityAICustomMate;
import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.phenotypes.Phenotypes;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityHandler {

	public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation("Zoology", "Phenes");

	@SubscribeEvent
	public void cap(AttachCapabilitiesEvent.Entity event) {
		Entity entity = event.getEntity();
		if (!EntityGeneRegistry.validClasses.contains(entity.getClass()))
			return;

		Class<? extends EntityAnimal> clazz = (Class<? extends EntityAnimal>) entity.getClass();
		Set<Phenotype> set = EntityGeneRegistry.getPhenotypes(clazz);


		PheneList<EntityAnimal> list = new PheneList<>((EntityAnimal) entity, set);
		event.addCapability(RESOURCE_LOCATION, list);

	}

	@SubscribeEvent
	public void startTracking(PlayerEvent.StartTracking event) {
		Entity target = event.getTarget();
		if (!target.hasCapability(PheneList.CAPABILITY, null)) return;
		PheneList<?> pheneList = target.getCapability(PheneList.CAPABILITY, null);

	}

	@SubscribeEvent
	public void spawn(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (!EntityGeneRegistry.validClasses.contains(entity.getClass()))
			return;

		PheneList pheneList = entity.getCapability(PheneList.CAPABILITY, null);
		if (!pheneList.isInitialized()) {
			pheneList.generate();
		}

		EntityAnimal animal = (EntityAnimal) entity;
		replaceAI(animal, EntityAIMate.class, matingAI -> new EntityAICustomMate((EntityAIMate) matingAI));

//		if (pheneList.getRegisteredPhenes().contains(Phenotypes.GRASS_COOLDOWN)) {
//			this.<EntityAIEatGrass, EntityAICustomEatGrass>replaceAI(animal, EntityAIEatGrass.class, matingAI -> new EntityAICustomEatGrass(animal));
//		}

	}

	private <A extends EntityAIBase, B extends A> void replaceAI(EntityAnimal entity, Class<? super A> aiClazz, Function<A, B> factory) {
		HashSet<EntityAITasks.EntityAITaskEntry> matingAIs = new HashSet<>();
		for (EntityAITasks.EntityAITaskEntry taskEntry : entity.tasks.taskEntries) {
			EntityAIBase action = taskEntry.action;
			if (action.getClass() == aiClazz) {
				matingAIs.add(taskEntry);
			}
		}

		for (EntityAITasks.EntityAITaskEntry matingAI : matingAIs) {
			entity.tasks.removeTask(matingAI.action);
			entity.tasks.addTask(matingAI.priority, factory.apply((A) matingAI.action));
		}
	}
}
