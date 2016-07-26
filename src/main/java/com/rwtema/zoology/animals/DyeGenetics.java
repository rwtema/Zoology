package com.rwtema.zoology.animals;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rwtema.zoology.LogHelper;
import com.rwtema.zoology.genes.Gene;
import com.rwtema.zoology.genes.GenePair;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.MathHelper;

public class DyeGenetics {
	public static HashMap<EnumDyeColor, List<GeneTriple>> dye2genes = Maps.newHashMap();

	static {
		for (EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
			dye2genes.put(enumDyeColor, Lists.newArrayList());
		}

		for (GenePair r : GenePair.Cache.geneHomo) {
			for (GenePair g : GenePair.Cache.geneHomo) {
				for (GenePair b : GenePair.Cache.geneHomo) {
					GeneTriple triple = GeneTriple.of(r, g, b);
					EnumDyeColor sheepColor = getSheepColor(r, g, b);
					float[] rgb = EntitySheep.getDyeRgb(sheepColor);
					if (rgb[0] != rgb[1] || rgb[0] != rgb[2] || rgb[1] != rgb[2]
							|| (g == GenePair.create(Gene.C, Gene.C)
							&& b == GenePair.create(Gene.C, Gene.C))
							) {
						dye2genes.get(sheepColor).add(triple);
					}
				}
			}
		}

		LogHelper.debug(dye2genes.size());
	}

	private static EnumDyeColor getSheepColor(GenePair r, GenePair g, GenePair b) {
		return getSheepColor(maxVal(r), maxAbsVal(g), maxAbsVal(b));
	}

	public static void init() {

	}

	public static float maxVal(GenePair pair) {
		return Math.max(calcVal(pair.a), calcVal(pair.b));
	}

	public static float maxAbsVal(GenePair pair) {
		float a = calcVal(pair.a);
		float b = calcVal(pair.b);
		double da = Math.abs(a - 0.5);
		double db = Math.abs(b - 0.5);
		if (da == db)
			return (a + b) / 2;
		else if (da < db)
			return b;
		else
			return a;
	}

	public static float meanVal(GenePair pair) {
		return (calcVal(pair.a) + calcVal(pair.b)) / 2;
	}

	public static float calcVal(Gene pair) {
		return pair.ordinal() / 4.0F;
	}

	public static EnumDyeColor getSheepColor(float y, float cr, float cb) {
		float r = MathHelper.clamp_float(y + 1.402F * (cb - 0.5F), 0, 1);
		float g = MathHelper.clamp_float(y - 0.34414F * (cr - 0.5F) - 0.71414F * (cb - 0.5F), 0, 1);
		float b = MathHelper.clamp_float(y + 1.722F * (cr - 0.5F), 0, 1);

		EnumDyeColor closest = null;
		float min = 1000;
		for (EnumDyeColor color : EnumDyeColor.values()) {
			float[] dyeRgb = EntitySheep.getDyeRgb(color);
			float dr = dyeRgb[0] - r;
			float dg = dyeRgb[1] - g;
			float db = dyeRgb[2] - b;
			float val = dr * dr + dg * dg + db * db;
			if (val < min) {
				closest = color;
				min = val;
			}
		}
		return closest;
	}

	public static GenePair[] getRandomGenes(EnumDyeColor fleeceColor, Random rng) {
		List<GeneTriple> list = dye2genes.get(fleeceColor);
		GeneTriple triple = list.get(rng.nextInt(list.size()));
		return new GenePair[]{triple.getA(), triple.getB(), triple.getC()};
	}

	public static EnumDyeColor getColor(GenePair[] genes) {
		return getSheepColor(genes[0], genes[1], genes[2]);
	}

	public static final class GeneTriple {
		private final short val;

		public GeneTriple(@Nonnull GenePair a, @Nonnull GenePair b, @Nonnull GenePair c) {
			this.val = (short) (a.index | (b.index << 4) | (c.index << 8));
		}

		public static GeneTriple of(@Nonnull GenePair a, @Nonnull GenePair b, @Nonnull GenePair c) {
			return new GeneTriple(a, b, c);
		}

		@Nonnull
		public GenePair getA() {
			return GenePair.Cache.genesCacheIndex[val & 15];
		}

		@Nonnull
		public GenePair getB() {
			return GenePair.Cache.genesCacheIndex[(val >> 4) & 15];
		}

		@Override
		public String toString() {
			return "{" + getA() + "," + getB() + "," + getC() + "}";
		}

		@Nonnull
		public GenePair getC() {
			return GenePair.Cache.genesCacheIndex[(val >> 8) & 15];
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			GeneTriple that = (GeneTriple) o;

			return val == that.val;

		}

		@Override
		public int hashCode() {
			return (int) val;
		}
	}
}
