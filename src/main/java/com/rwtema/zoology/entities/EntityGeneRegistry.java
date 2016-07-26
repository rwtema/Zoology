package com.rwtema.zoology.entities;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.phenotypes.visualphenes.IVisualPhene;
import com.rwtema.zoology.phenotypes.visualphenes.VisualInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;

public class EntityGeneRegistry {
	public static HashSet<Class<? extends Entity>> validClasses = Sets.newHashSet();

	private static LinkedHashMultimap<Class<? extends EntityAnimal>, Phenotype> phenotypes = LinkedHashMultimap.create();
	private static HashMap<Class<? extends EntityAnimal>, Class<? extends EntityAnimal>> equivalentClasses = Maps.newHashMap();

	private static LoadingCache<Class<? extends EntityAnimal>, GenePool<?>> loader = CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends Entity>, GenePool<?>>() {
		@Override
		public GenePool<?> load(@Nonnull Class<? extends Entity> key) throws Exception {
			return new GenePool(key);
		}
	});

	public static <V extends EntityAnimal> GenePool<V> getGenePool(Class<? extends EntityAnimal> clazz) {
		return (GenePool<V>) loader.getUnchecked(getEquiv(clazz));
	}

	public static <V extends EntityAnimal> void register(Class<V> clazz, Phenotype phenotype) {
		validClasses.add(clazz);
		phenotypes.put(clazz, phenotype);
		if(phenotype instanceof IVisualPhene){
			VisualInfo.register(clazz, (IVisualPhene) phenotype);
		}
	}

	public static <V extends EntityAnimal, T extends V> void registerClassEquivalency(Class<V> baseClazz, Class<T> otherClazz) {
		validClasses.add(otherClazz);
		equivalentClasses.put(otherClazz, baseClazz);

	}

	public static Class<? extends EntityAnimal> getEquiv(Class<? extends EntityAnimal> clazz) {
		Class<? extends EntityAnimal> equivClass = equivalentClasses.get(clazz);
		return equivClass != null ? equivClass : clazz;
	}

	public static void recreateGenePool() {
		loader.invalidateAll();
		for (Class<? extends EntityAnimal> aClass : phenotypes.keySet()) {
			loader.getUnchecked(aClass);
		}
	}

	public static void setGenePool(Class<? extends EntityAnimal> clazz, GenePool pool) {
		loader.put(clazz, pool);
	}

	public static Set<Phenotype> getPhenotypes(Class<? extends EntityAnimal> clazz) {
		return phenotypes.get(clazz);
	}

	public static Set<Class<? extends EntityAnimal>> getClasses() {
		return phenotypes.keySet();
	}
}
