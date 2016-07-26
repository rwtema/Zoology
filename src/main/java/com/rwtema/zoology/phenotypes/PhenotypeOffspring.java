package com.rwtema.zoology.phenotypes;

import com.rwtema.zoology.genes.Dominance;
import gnu.trove.list.array.TIntArrayList;
import java.util.Random;

public class PhenotypeOffspring extends PhenotypeDouble.Exp {
	public PhenotypeOffspring() {
		super("offspring_chance", 32, 0, 0.032, 5);
	}
}
