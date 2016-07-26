package com.rwtema.zoology.phenes;

import com.google.common.collect.Lists;
import com.rwtema.zoology.LogHelper;
import com.rwtema.zoology.genes.Dominance;
import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.utils.NBTSerializer;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.Validate;

public abstract class Phenotype<T, V extends EntityAnimal> {

	public final static TIntObjectHashMap<Phenotype> networkIDS = new TIntObjectHashMap<>();
	public final static HashMap<String, Phenotype> registry = new HashMap<>();
	public final String name;
	public final int reserved_genes;
	public final int random_genes;
	public final double max_range;
	public final NBTSerializer serializer;
	final Class<T> valueClazz;
	final Class<V> entityClazz;
	public int networkID;
	public boolean optional;

	public Phenotype(String name, Class<T> valueClazz, Class<V> entityClazz, NBTSerializer<T, ?> serializer, int reserved_genes, int random_genes, double max_range) {
		this.name = name;
		this.valueClazz = valueClazz;
		this.entityClazz = entityClazz;
		this.serializer = serializer;
		this.random_genes = random_genes;
		this.reserved_genes = reserved_genes;
		this.max_range = max_range;
		Validate.isTrue(registry.put(name, this) == null);
		LogHelper.debug("Registering " + name);
		reevaluate();
	}

	private static void reevaluate() {
		networkIDS.clear();
		ArrayList<String> names = Lists.newArrayList(registry.keySet());
		Collections.sort(names);
		for (int i = 0; i < names.size(); i++) {
			String s = names.get(i);
			Phenotype phenotype = registry.get(s);
			phenotype.networkID = i;
			networkIDS.put(i, phenotype);
		}
	}

	public String getName() {
		return name;
	}

	public float[] generateWeightBase(float[] weights, TIntArrayList assignedGenes, int i, Random rand, Dominance[] dominance) {
		for (int j = 0; j < 5; j++) {
			float v1 = (float) rand.nextGaussian();
			weights[j] = v1;
		}

		for (int j = 0; j < 10; j++) {
			GenePair pair = GenePair.Cache.geneHetero[j];
			Dominance d = dominance[j];
			if (d == Dominance.SECOND_DOMINANT) {
				weights[j + 5] = weights[pair.b.ordinal()];
			} else if (d == Dominance.FIRST_DOMINANT) {
				weights[j + 5] = weights[pair.a.ordinal()];
			} else if (d == Dominance.CO_DOMINANT) {
				float a = weights[pair.a.ordinal()];
				float b = weights[pair.b.ordinal()];
				weights[j + 5] = (a + b) / 2 + getInbreedingModifier(rand);
			}
		}

		return weights;
	}

	protected float getInbreedingModifier(Random rand) {
		return (float) Math.abs(rand.nextGaussian()) / 2;
	}

	public <K extends Phenotype<T, V>> K setOptional() {
		this.optional = true;
		return (K) this;
	}

	public ITextComponent buildComponent(T value) {
		ITextComponent oc = getDisplayName();
		oc.appendText(" = ");
		ITextComponent displayValue = getDisplayValue(value);
		if (displayValue == null) return null;
		oc.appendSibling(displayValue);
		return oc;
	}

	public ITextComponent getDisplayValue(T value) {
		return new TextComponentString(serializer.makeString(value));
	}

	@Override
	public String toString() {
		return "Phenotype{" + name + '}';
	}

	@Nonnull
	public T calcValue(GeneticStrand strand, GenePool<V> pool, GenePool.Link link) {
		double v = 0;
		GenePair[] strandValues = strand.strandValues;
		int[] assignedGenes = link.assignedGenes;
		for (int i = 0; i < assignedGenes.length; i++) {
			GenePair genePair = strandValues[assignedGenes[i]];
			v += link.weights[i][genePair.index];
		}

		return initValue(v, strand, pool, link);
	}

	public int getReservedGenes(Random random) {
		return reserved_genes;
	}

	public int getRandomGenes(Random random) {
		return random_genes;
	}

	public abstract void initApply(V entity, T value);

	public T readFromNBT(V entity, NBTTagCompound tag) {
		NBTBase base = tag.getTag(name);
		if (base == null) return null;
		T t = (T) serializer.deserialize(base);
		onLoad(entity, t, tag);
		return t;
	}

	protected void onLoad(V entity, T t, NBTTagCompound tag) {

	}

	public void writeToNBT(T value, V entity, NBTTagCompound tag) {
		tag.setTag(name, serializer.serialize(value));
	}

	public Class<T> getValueClass() {
		return valueClazz;
	}

	public Class<V> getEntityClass() {
		return entityClazz;
	}

	public abstract T initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link);

	public void onReserve(GenePool<V> vGenePool, int i, Random rand) {

	}

	public void overwriteGeneratedGeneValues(GenePair[] pairs, V parent, GenePool<V> vGenePool, GenePool.Link link) {

	}


	public void onCombine(GeneticStrand strand, V parent, GeneticStrand parent1, GeneticStrand parent2, Set<Phenotype> set, GenePool<V> pool) {

	}

	public ITextComponent getDisplayName() {
		return new TextComponentTranslation("zoology.phenotype." + name);
	}

	@Nonnull
	public GenePool.Link onWeightsGenerated(GenePool<V> vGenePool, @Nonnull GenePool.Link link) {
		return link;
	}

	public float[][] assignWeightsOveride(GenePool<V> pool, TIntArrayList assignedGenes, Random rand) {
		return null;
	}

	public boolean hasNetwork() {
		return false;
	}

	public void addToPacket() {
		throw new RuntimeException("addToPacket unimplemented");
	}

	public void readFromPacket() {
		throw new RuntimeException("readFromPacket unimplemented");
	}

	@Nonnull
	public T calcValueMissing(V parent, GeneticStrand strand, GenePool pool, GenePool.Link pheneLink) {
		return (T) calcValue(strand, pool, pheneLink);
	}
}
