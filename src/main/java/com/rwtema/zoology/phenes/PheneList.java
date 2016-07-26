package com.rwtema.zoology.phenes;

import com.rwtema.zoology.Zoology;
import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;

public class PheneList<V extends EntityAnimal> implements ICapabilitySerializable<NBTTagCompound> {
	@CapabilityInject(PheneList.class)
	public static Capability<PheneList> CAPABILITY = null;


	public final V parent;
	HashMap<Phenotype<?, ? super V>, Object> map = new HashMap<>();
	private boolean init = false;

	public PheneList(V parent, Set<Phenotype> set) {
		this.parent = parent;
		for (Phenotype phenotype : set) {
			map.put(phenotype, null);
		}
	}

	public static GeneticStrand loadGenes(Entity entity) {
		NBTTagCompound nbt = entity.getEntityData();
		if (nbt.hasKey("Zoology", Constants.NBT.TAG_COMPOUND)) {
			NBTTagCompound data = nbt.getCompoundTag("Zoology");

			if (data.getInteger("Version") == Zoology.version && data.getInteger("Seed") == Zoology.seed) {
				GeneticStrand strand = new GeneticStrand();
				try {
					strand.deserializeNBT((NBTTagIntArray) data.getTag("Strand"));
					return strand;
				} catch (Exception err) {
					new RuntimeException("NBT Corrupted on " + entity.getClass() + " - Resetting", err).printStackTrace();
				}
			}
		}
		return null;
	}

	public static void saveGenesToNBT(GeneticStrand strand, EntityAnimal parent) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("Strand", strand.serializeNBT());
		nbt.setInteger("Version", Zoology.version);
		nbt.setInteger("Seed", Zoology.seed);
		parent.getEntityData().setTag("Zoology", nbt);
	}

	public Set<Phenotype<?, ? super V>> getRegisteredPhenes() {
		return map.keySet();
	}

	public <T> T getValue(Phenotype<T, ? super V> phenotype) {
		Object o = map.get(phenotype);
		if (o == null) {
			GenePool<V> pool = EntityGeneRegistry.getGenePool(parent.getClass());
			GeneticStrand strand = getStrand(parent, pool);
			o = phenotype.calcValueMissing(parent, strand, (GenePool) pool, pool.getPheneLink(phenotype));
			saveGenesToNBT(strand, parent);
		}

		return phenotype.getValueClass().cast(o);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		if (!init)
			throw new IllegalStateException();

		NBTTagCompound tag = new NBTTagCompound();
		for (Map.Entry<Phenotype<?, ? super V>, ?> entry : map.entrySet()) {
			Phenotype<Object, V> key = (Phenotype<Object, V>) entry.getKey();
			key.writeToNBT(entry.getValue(), parent, tag);
		}
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		init = true;
		GeneticStrand strand = null;
		for (Map.Entry<Phenotype<?, ? super V>, ?> entry : map.entrySet()) {
			Phenotype<?, ? super V> key = entry.getKey();
			Object o = key.readFromNBT(parent, nbt);

			if (o == null) {
				GenePool<V> pool = EntityGeneRegistry.getGenePool(parent.getClass());
				if (strand == null) {
					strand = getStrand(parent, pool);
				}
				o = key.calcValueMissing(parent, strand, (GenePool) pool, pool.getPheneLink(key));
				saveGenesToNBT(strand, parent);

			}
			map.put(key, o);
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CAPABILITY ? CAPABILITY.cast(this) : null;
	}

	public boolean isInitialized() {
		return init;
	}

	public GeneticStrand generate() {
		init = true;
		V parent = this.parent;
		Set<Phenotype> set = EntityGeneRegistry.getPhenotypes(parent.getClass());
		GenePool<V> pool = EntityGeneRegistry.getGenePool(parent.getClass());

		GeneticStrand strand = pool.generate(parent, parent.worldObj.rand);
		loadGenes(set, pool, strand);
		return strand;
	}

	public GeneticStrand generateFromStrand(Set<Phenotype> set, GenePool<V> pool, GeneticStrand strand) {
		init = true;
		loadGenes(set, pool, strand);
		return strand;
	}

	public void loadGenes(Set<Phenotype> set, GenePool<V> pool, GeneticStrand strand) {
		for (Phenotype phenotype : set) {
			reloadPhenotypeValue(pool, strand, phenotype);
		}

		saveGenesToNBT(strand, parent);
	}

	public void reloadPhenotypeValue(GenePool<V> pool, GeneticStrand strand, Phenotype phenotype) {
		Object o = phenotype.calcValue(strand, pool, pool.getPheneLink(phenotype));
		map.put(phenotype, o);
		phenotype.initApply(parent, o);
	}

	public void generateClone(V animal) {
		Set<Phenotype> set = EntityGeneRegistry.getPhenotypes(parent.getClass());
		GenePool<V> pool = EntityGeneRegistry.getGenePool(parent.getClass());

		GeneticStrand strand = getStrand(animal, pool);
		if (strand == null) {
			return;
		}

		init = true;

		loadGenes(set, pool, strand);
	}

	public void generate(V animal, V mate, Random rand) {
		Set<Phenotype> set = EntityGeneRegistry.getPhenotypes(parent.getClass());
		GenePool<V> pool = EntityGeneRegistry.getGenePool(parent.getClass());

		GeneticStrand parent1 = getStrand(animal, pool);
		GeneticStrand parent2 = getStrand(mate, pool);

		if (parent1 == null || parent2 == null) {
			return;
		}

		init = true;


		GeneticStrand strand = new GeneticStrand();

		GenePair[] parentPairs1 = parent1.strandValues;
		GenePair[] parentPairs2 = parent2.strandValues;

		GenePair[] strandValues = strand.strandValues = new GenePair[Zoology.STRAND_SIZE];

		for (int i = 0; i < Zoology.STRAND_SIZE; i++) {
			GenePair mama = parentPairs1[i];
			GenePair papa = parentPairs2[i];
			GenePair combine = GenePair.combine(mama, papa, rand);
			strandValues[i] = combine;
		}

		for (Phenotype phenotype : set) {
			phenotype.onCombine(strand, parent, parent1, parent2, set, pool);
		}

		loadGenes(set, pool, strand);
	}

	public GeneticStrand getStrand(V entity, GenePool<V> pool) {
		GeneticStrand strand = loadGenes(entity);
		if (strand != null) return strand;

		GeneticStrand generate = pool.generate(entity, entity.worldObj.rand);
		saveGenesToNBT(generate, entity);
		return generate;
	}


}
