package com.rwtema.zoology.phenotypes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rwtema.zoology.ProxyClient;
import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.phenes.Phenotype;
import java.util.List;
import java.util.Set;
import net.minecraft.client.model.ModelSheep1;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Phenotypes {
	public static final PhenotypeAttribute MAX_HEALTH = new PhenotypeAttribute(SharedMonsterAttributes.MAX_HEALTH, 5, 15, 0.7){
		@Override
		public void initApply(EntityAnimal entity, Double value) {
			super.initApply(entity, value);
			entity.heal(entity.getMaxHealth());
		}
	};
	public static final PhenotypeAttribute MOVE_SPEED = new PhenotypeAttribute(SharedMonsterAttributes.MOVEMENT_SPEED, 10, 20, 0.5);
	public static final PhenotypeDouble LOVING_AGE = new PhenotypeAge("loving_age", 20, 30, 6000 * 20, 6000 / 20);
	public static final PhenotypeDouble GROWING_AGE = new PhenotypeAge("growing_age", 20, 30, 24000 * 20, 24000 / 20);
	public static final PhenotypeFlag INFERTILE = new PhenotypeInfertile();
	public static final Phenotype INBRED = new PhenotypeInbred().setOptional();
	//	public static final PhenotypeOffspring OFFSPRING = new PhenotypeOffspring();
	public static final PhenotypeDouble GRASS_COOLDOWN = new PhenotypeAge("grass_eating", 4, 2, 50 * 20 / 4, 50 * 20 * 4);

	public static final PhenotypeWool WOOL = new PhenotypeWool();

	public static final PhenotypeSize SIZE = new PhenotypeSize();
	public static final PhenotypeGlowing GLOW = new PhenotypeGlowing();

	public static void registerPhenes() {



		List<Class> mainClazzes = Lists.<Class>newArrayList(
				EntityCow.class,
				EntitySheep.class,
				EntityPig.class,
				EntityChicken.class,
				EntityRabbit.class,
				EntityHorse.class,
				EntityWolf.class,
				EntityOcelot.class
		);

		Class<EntityAnimal> polarBearClass = null;
		try {
			polarBearClass = (Class<EntityAnimal>) Class.forName("net.minecraft.entity.monster.EntityPolarBear");
			mainClazzes.add(polarBearClass);
		} catch (ClassNotFoundException | ClassCastException ignore ) {

		}

		EntityGeneRegistry.registerClassEquivalency(EntityCow.class, EntityMooshroom.class);

		for (Class clazz : mainClazzes) {
			EntityGeneRegistry.register(clazz, SIZE);
			EntityGeneRegistry.register(clazz, MAX_HEALTH);
			EntityGeneRegistry.register(clazz, MOVE_SPEED);
			EntityGeneRegistry.register(clazz, GROWING_AGE);
			EntityGeneRegistry.register(clazz, LOVING_AGE);
			EntityGeneRegistry.register(clazz, INFERTILE);
		}

		// Rabbit
		EntityGeneRegistry.register(EntityRabbit.class, new PhenotypeDrop("rabbit_meat", new ItemStack(Items.RABBIT)).setBurningDrop(new ItemStack(Items.COOKED_RABBIT)));
		EntityGeneRegistry.register(EntityRabbit.class, new PhenotypeDrop("rabbit_hide", new ItemStack(Items.RABBIT_HIDE)));

		// Horse
		EntityGeneRegistry.register(EntityHorse.class, new PhenotypeDrop("horse_leather", new ItemStack(Items.LEATHER)));

		// Chicken
		EntityGeneRegistry.register(EntityChicken.class, new PhenotypeDrop("chicken_drumstick", new ItemStack(Items.CHICKEN)).setBurningDrop(new ItemStack(Items.COOKED_CHICKEN)));

		// Pig
		EntityGeneRegistry.register(EntityPig.class, new PhenotypeDrop("pig_porkchop", new ItemStack(Items.PORKCHOP)).setBurningDrop(new ItemStack(Items.COOKED_PORKCHOP)));
		EntityGeneRegistry.register(EntityPig.class, new PhenotypeThreeHeads("pig", new ResourceLocation("textures/entity/pig/pig.png")));

		// Cow
		EntityGeneRegistry.register(EntityCow.class, new PhenotypeDrop("cow_beef", new ItemStack(Items.BEEF)).setBurningDrop(new ItemStack(Items.COOKED_BEEF)));
		EntityGeneRegistry.register(EntityCow.class, new PhenotypeDrop("cow_leather", new ItemStack(Items.LEATHER)));
		EntityGeneRegistry.register(EntityCow.class, new PhenotypeThreeHeads("cow", new ResourceLocation("textures/entity/cow/cow.png")) {
			private final ResourceLocation MOOSHROOM_TEXTURES = new ResourceLocation("textures/entity/cow/mooshroom.png");

			@Override
			protected ResourceLocation getTextureLocation(EntityAnimal animal) {
				if (animal instanceof EntityMooshroom) {
					return MOOSHROOM_TEXTURES;
				}
				return super.getTextureLocation(animal);
			}
		});

		// Polar Bear
		if(polarBearClass != null){
			EntityGeneRegistry.register(polarBearClass, new PhenotypeThreeHeads("polar_bear", new ResourceLocation("textures/entity/bear/polarbear.png")));
		}

		// Sheep
		EntityGeneRegistry.register(EntitySheep.class, WOOL);
		EntityGeneRegistry.register(EntitySheep.class, new PhenotypeDrop("sheep_mutton", new ItemStack(Items.MUTTON)).setBurningDrop(new ItemStack(Items.COOKED_MUTTON)));
		EntityGeneRegistry.register(EntitySheep.class, new PhenotypeWoolDrop());
		EntityGeneRegistry.register(EntitySheep.class, new PhenotypeRareDrop("sheep_diamond", new ItemStack(Items.DIAMOND), -1, 2, 30));
		EntityGeneRegistry.register(EntitySheep.class, new PhenotypeThreeHeads("sheep", new ResourceLocation("textures/entity/sheep/sheep.png")) {
			private final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/sheep/sheep_fur.png");

			@Override
			@SideOnly(Side.CLIENT)
			public void renderLayer(EntityAnimal animal, RenderLivingBase renderer, Boolean value, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {


				if (animal.isChild() || animal.isInvisible()) return;

				super.renderLayer(animal, renderer, value, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

				EntitySheep sheep = (EntitySheep) animal;
				if (sheep.getSheared()) return;

				renderer.bindTexture(TEXTURE);
				ModelSheep1 modelSheep1 = ProxyClient.furLayer;
				float[] afloat = EntitySheep.getDyeRgb(sheep.getFleeceColor());
				GlStateManager.color(afloat[0], afloat[1], afloat[2]);
				modelSheep1.setModelAttributes(renderer.getMainModel());
				modelSheep1.setLivingAnimations(animal, limbSwing, limbSwingAmount, partialTicks);
				renderHead(animal, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, scale, modelSheep1, modelSheep1.head);
			}
		});


		for (Class clazz : mainClazzes) {
			EntityGeneRegistry.register(clazz, GLOW);
			EntityGeneRegistry.register(clazz, INBRED);
		}
	}
}
