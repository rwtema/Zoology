package com.rwtema.zoology.phenes;

import com.google.common.collect.ComparisonChain;
import java.util.Comparator;

public class PhenotypeSorting implements Comparator<Phenotype> {
	@Override
	public int compare(Phenotype o1, Phenotype o2) {
		ComparisonChain comparison = ComparisonChain.start();
		comparison.compare(o1.serializer.getClass().getSimpleName(), o2.serializer.getClass().getSimpleName());
		comparison.compare(o1.name, o2.name);
		return comparison.result();
	}
}
