package com.rwtema.zoology.potion;

import com.rwtema.zoology.Zoology;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

public class PotionsHandler {
	static void register(Potion instance, PotionType type) {
		Potion.REGISTRY.register(0, new ResourceLocation(Zoology.MODID, instance.getName()), instance);
		PotionType.REGISTRY.register(0, new ResourceLocation(Zoology.MODID, "zoology." + instance.getName().replace("effect.zoology.","")), type);

		BrewingRecipeRegistry.addRecipe(
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), type),
				new ItemStack(Items.GUNPOWDER),
				PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), type)
		);

		BrewingRecipeRegistry.addRecipe(
				PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), type),
				new ItemStack(Items.DRAGON_BREATH),
				PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), type)
		);
	}

	public static void init() {
		register(PotionRadiation.INSTANCE, PotionRadiation.type);
		BrewingRecipeRegistry.addRecipe(
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), getVanillaType("harming")),
				new ItemStack(Items.SKULL, 1, 1),
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionRadiation.type));

		register(PotionSelfImpregnation.INSTANCE, PotionSelfImpregnation.type);
		BrewingRecipeRegistry.addRecipe(
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), getVanillaType("awkward")),
				new ItemStack(Items.APPLE, 1),
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionSelfImpregnation.type));

		register(PotionClone.INSTANCE, PotionClone.type);
		BrewingRecipeRegistry.addRecipe(
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), getVanillaType("thick")),
				new ItemStack(Items.GOLDEN_APPLE, 1),
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionClone.type));
	}

	public static PotionType getVanillaType(String name) {
		return PotionType.REGISTRY.getObject(new ResourceLocation(name));
	}
}
