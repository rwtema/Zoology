package com.rwtema.zoology.phenotypes;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class PhenotypeGlowing extends PhenotypeMutation {

	protected PhenotypeGlowing() {
		super("glow", true, 2);
	}

	@Override
	public void onRenderStart(EntityAnimal entity, RenderLivingBase renderer, Boolean value) {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderLayer(EntityAnimal animal, RenderLivingBase renderer, Boolean value, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ModelBase mainModel = renderer.getMainModel();

		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.depthFunc(514);
		GlStateManager.disableLighting();
		renderer.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.depthFunc(GL11.GL_EQUAL);
		GlStateManager.disableLighting();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

		float f = (float) animal.ticksExisted + partialTicks;
		GlStateManager.color(0.38f, 0.8F , 0.25f, 1.0F);


		for (int i = 0; i < 2; ++i) {

			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.loadIdentity();
			float f3 = 0.33333334F;
			GlStateManager.scale(f3, f3, f3);
			GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);

			mainModel.setLivingAnimations(animal, limbSwing, limbSwingAmount, partialTicks);
			mainModel.render(animal, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		}

		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
		GlStateManager.disableBlend();
	}


}
