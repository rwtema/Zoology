package com.rwtema.zoology.phenotypes;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PhenotypeThreeHeads extends PhenotypeMutation {
	private final ResourceLocation location;

	public PhenotypeThreeHeads(String name, ResourceLocation location) {
		super("triheaded_" + name , true, 2);
		this.location = location;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderLayer(EntityAnimal animal, RenderLivingBase renderer, Boolean value, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (animal.isChild() || animal.isInvisible()) return;
		GlStateManager.color(1, 1, 1, 1);
		ModelBase mainModel = renderer.getMainModel();
		renderer.bindTexture(getTextureLocation(animal));

		ModelRenderer head = null;
		if (mainModel instanceof ModelQuadruped) {
			head = ((ModelQuadruped) mainModel).head;
		}

		if (head != null) {
			GlStateManager.pushMatrix();
			renderHead(animal, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, scale, mainModel, head);
			GlStateManager.popMatrix();
		}
	}

	protected ResourceLocation getTextureLocation(EntityAnimal animal) {
		return location;
	}

	public void renderHead(EntityAnimal animal, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float scale, ModelBase mainModel, ModelRenderer head) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(-3 * scale, 1 * scale, 2 * scale);
		mainModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw + 60, 0, scale, animal);
		head.render(scale);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.translate(3 * scale, 1 * scale, 2 * scale);
		mainModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw - 60, 0, scale, animal);
		head.render(scale);
		GlStateManager.popMatrix();
	}

	@Override
	public ITextComponent getDisplayValue(Boolean value) {
		return value ? new TextComponentTranslation("zoology.phenotype.flag.tri_headed") : null;
	}
}
