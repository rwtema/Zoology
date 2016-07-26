package com.rwtema.zoology.phenotypes.visualphenes;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;

public class LayerVisualData implements LayerRenderer<EntityAnimal> {

	private final RenderLivingBase renderer;

	public LayerVisualData(RenderLivingBase renderer) {

		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(@Nonnull EntityAnimal animal, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		
		HashMap<IVisualPhene, Object> map = VisualInfo.clientData.get(animal);
		if (map == null) return;

		for (Map.Entry<IVisualPhene, Object> entry : map.entrySet()) {
			entry.getKey().renderLayer(animal, renderer, entry.getValue(),  limbSwing,  limbSwingAmount,  partialTicks,  ageInTicks,  netHeadYaw,  headPitch,  scale);
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
