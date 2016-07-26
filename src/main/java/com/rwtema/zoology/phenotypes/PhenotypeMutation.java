package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenotypes.visualphenes.IVisualPhene;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.network.PacketBuffer;

public class PhenotypeMutation extends PhenotypeUnnaturalFlag implements IVisualPhene<Boolean, EntityAnimal> {

	protected PhenotypeMutation(String name, boolean isNegative, int reserved_genes) {
		super(name, isNegative, reserved_genes);
	}

	@Override
	public void writePacket(PacketBuffer buffer, EntityAnimal animal, PheneList<?> pheneList) {

	}

	@Override
	public Boolean readPacket(PacketBuffer buffer) {
		return true;
	}

	@Override
	public boolean hasVisualInfo(EntityAnimal animal, PheneList<?> pheneList) {
		return pheneList.getValue(this);
	}

	@Override
	public void onApplyClient(EntityAnimal entity, Boolean value) {

	}

	@Override
	public void onRenderStart(EntityAnimal entity, RenderLivingBase renderer, Boolean value) {

	}

	@Override
	public void onRenderEnd(EntityAnimal entity, RenderLivingBase renderer, Boolean value) {

	}

	@Override
	public void renderLayer(EntityAnimal animal, RenderLivingBase renderer, Boolean value, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

	}
}
