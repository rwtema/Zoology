package com.rwtema.zoology.potion;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;

public class PotionClone extends Potion {
	public static final PotionClone INSTANCE = new PotionClone();
	public static final PotionType type = new PotionType(new PotionEffect(INSTANCE, 60 * 20));

	protected PotionClone() {
		super(false, 0xa0a0a0);
		setPotionName("effect.zoology.clone");
	}
}
