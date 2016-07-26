package com.rwtema.zoology.potion;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;

public class PotionSelfImpregnation extends Potion {
	public static final PotionSelfImpregnation INSTANCE = new PotionSelfImpregnation();
	public static final PotionType type = new PotionType(new PotionEffect(INSTANCE, 60 * 20));

	protected PotionSelfImpregnation() {
		super(false, 0xff8050);
		setPotionName("effect.zoology.autogamy");
	}
}
