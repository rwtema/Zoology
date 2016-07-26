package com.rwtema.zoology.phenotypes;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class PhenotypeAge extends PhenotypeDouble.Exp {
	static final int TIME_MINUTE = 60;
	static final int TIME_HOUR = 60 * 60;
	static final int TIME_DAY = 60 * 60 * 24;

	public PhenotypeAge(String name, int reserved_genes, int random_genes, double low, double upper) {
		super(name, reserved_genes, random_genes, low, upper);
	}

	public static ITextComponent getTimeValue(double time) {
		int x = (int) Math.abs(Math.round(time / 20));
		ITextComponent text = new TextComponentString("");
		boolean flag;

		int d = x / TIME_DAY;
		x -= d * TIME_DAY;
		flag = d != 0;
		if (flag) text.appendText(d + "d ");


		int h = x / TIME_HOUR;
		x -= h * TIME_HOUR;
		flag |= h != 0;
		if (flag) text.appendText(h + "h ");

		int m = x / TIME_MINUTE;
		x -= m * TIME_MINUTE;
		flag |= m != 0;
		if (flag) text.appendText(m + "m ");

		text.appendText(x + "s");

		return text;
	}

	@Override
	public ITextComponent getDisplayValue(Double value) {
		return getTimeValue(value);
	}
}
