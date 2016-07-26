package com.rwtema.zoology.ai;

import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenotypes.Phenotypes;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.passive.EntityAnimal;

public class EntityAICustomEatGrass extends EntityAIEatGrass {
	private final PheneList pheneList;

	public EntityAICustomEatGrass(EntityAnimal grassEaterEntityIn) {
		super(grassEaterEntityIn);
		pheneList = grassEaterEntityIn.getCapability(PheneList.CAPABILITY, null);
	}

	@Override
	public boolean shouldExecute() {
		double t = (double) pheneList.getValue(Phenotypes.GRASS_COOLDOWN);
		if (this.grassEaterEntity.getRNG().nextInt(this.grassEaterEntity.isChild() ? (1 + ((int) t / 20)) : (int) t) != 0) {
			return false;
		}

		return super.shouldExecute();
	}
}
