package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenotypes.visualphenes.IVisualPhene;
import java.util.Random;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class PhenotypeSize extends PhenotypeDouble.Exp implements IVisualPhene<Double, EntityAnimal> {
	public PhenotypeSize() {
		super("size", 24, 16, 1 / 4F, 4F);
	}

	@Override
	public void initApply(EntityAnimal entity, Double value) {
		setSize(entity, value);
	}

	@Override
	protected void onLoad(EntityAnimal entity, Double aDouble, NBTTagCompound tag) {
		setSize(entity, aDouble);
	}

	@Override
	protected float getInbreedingModifier(Random rand) {
		return 0;
	}

	private void setSize(EntityAnimal entity, Double value) {
		entity.ageWidth *= value;
		entity.ageHeight *= value;
		entity.setScaleForAge(entity.isChild());
	}

	@Override
	public void writePacket(PacketBuffer buffer, EntityAnimal animal, PheneList<?> pheneList) {
		buffer.writeDouble(pheneList.getValue(this));
	}

	@Override
	public Double readPacket(PacketBuffer buffer) {
		return buffer.readDouble();
	}

	@Override
	public boolean hasVisualInfo(EntityAnimal animal, PheneList<?> pheneList) {
		return true;
	}

	@Override
	public void onApplyClient(EntityAnimal entity, Double value) {
		setSize(entity, value);
	}

	@Override
	public void onRenderStart(EntityAnimal entity, RenderLivingBase renderer, Double value) {
		GlStateManager.scale(value, value, value);
	}

	@Override
	public void onRenderEnd(EntityAnimal entity, RenderLivingBase renderer, Double value) {

	}

	@Override
	public void renderLayer(EntityAnimal animal, RenderLivingBase renderer, Double value, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

	}

	@Override
	public ITextComponent getDisplayValue(Double value) {
		return new TextComponentString(String.format("%.1f%%", (value) * 100));
	}
}
