package com.rwtema.zoology.genes;

import com.rwtema.zoology.LogHelper;
import com.rwtema.zoology.Zoology;
import static com.rwtema.zoology.Zoology.STRAND_SIZE;
import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.phenotypes.visualphenes.IVisualPhene;
import com.rwtema.zoology.save.SaveHandler;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.Validate;

public class GenePool<V extends EntityAnimal> {
	public final Set<Phenotype> phenotypes;
	public final ArrayList<IVisualPhene> visualPhenes;
	final Class<V> clazz;
	public float[][] probabilities = new float[STRAND_SIZE][5];
	public Dominance[][] dominance = new Dominance[STRAND_SIZE][10];

	public TIntHashSet reservedGenes = new TIntHashSet();
	public HashMap<Phenotype, Link> links = new HashMap<>();

	public GenePool(Class<V> clazz) {
		this.clazz = clazz;
		phenotypes = EntityGeneRegistry.getPhenotypes(clazz);
		visualPhenes = getiVisualPhenes(phenotypes);
		Random rand = new Random(getBaseSeed());
		probabilities = new float[STRAND_SIZE][5];

		for (int i = 0; i < STRAND_SIZE; i++) {
			for (int j = 0; j < 10; j++) {
				GenePair pair = GenePair.Cache.geneHetero[j];
				if (rand.nextInt(15) == 0) {
					if (rand.nextBoolean()) {
						dominance[i][j] = Dominance.FIRST_DOMINANT;
					} else {
						dominance[i][j] = Dominance.SECOND_DOMINANT;
					}
				} else {
					dominance[i][j] = Dominance.CO_DOMINANT;
				}
			}

			float[] p = new float[5];
			float sum;

			sum = 0;
			do {
				for (int i1 = 0; i1 < 5; i1++) {
					float v;
					if (rand.nextInt(5) == 0)
						v = 0;
					else
						v = (float) -Math.log(rand.nextDouble());
					sum += v;
					p[i1] = v;
				}
			} while (sum == 0);


			for (int i1 = 0; i1 < 5; i1++) {
				p[i1] /= sum;
			}

			probabilities[i] = p;
		}

		for (Phenotype phenotype : phenotypes) {
			links.put(phenotype, calculatePheneLink(phenotype));
		}

		TIntHashSet genes = new TIntHashSet();
		links.values().forEach(link -> genes.addAll(link.assignedGenes));
		LogHelper.debug(genes.size());
	}

	public GenePool(Class<V> clazz, NBTTagCompound tag) {
		this.clazz = clazz;
		phenotypes = EntityGeneRegistry.getPhenotypes(clazz);
		visualPhenes = getiVisualPhenes(phenotypes);


		int[] probs = tag.getIntArray("Probabilities");
		Validate.isTrue(probs.length == STRAND_SIZE * 5);
		for (int i = 0; i < STRAND_SIZE; i++) {
			int[] ints = new int[5];
			System.arraycopy(probs, i * 5, ints, 0, 5);
			this.probabilities[i] = intArrToFloatArr(ints);
		}


		int[] doms = tag.getIntArray("Dominances");
		Validate.isTrue(doms.length == STRAND_SIZE);
		for (int i = 0; i < STRAND_SIZE; i++) {
			dominance[i] = Dominance.fromBits(doms[i], dominance[i]);
		}

		reservedGenes.addAll(tag.getIntArray("Reserved"));

		NBTTagCompound links = tag.getCompoundTag("Links");
		for (String s : links.getKeySet()) {
			try {
				Phenotype phenotype = Phenotype.registry.get(s);
				if (phenotype == null) {
					continue;
				}

				NBTTagCompound compound = links.getCompoundTag(s);
				Link link = new Link(compound);
				this.links.put(phenotype, link);
			} catch (Exception err) {
				new RuntimeException("Unable to reload saved phenotype values " + s, err).printStackTrace();
			}
		}

		for (Phenotype phenotype : phenotypes) {
			if (this.links.containsKey(phenotype)) continue;
			this.links.put(phenotype, calculatePheneLink(phenotype));
		}
	}

	public static int[] floatArrToIntArr(float[] floats) {
		int[] r = new int[floats.length];
		for (int i = 0; i < r.length; i++) {
			r[i] = Float.floatToIntBits(floats[i]);
		}
		return r;
	}

	public static float[] intArrToFloatArr(int[] ints) {
		float[] floats = new float[ints.length];
		for (int i = 0; i < floats.length; i++) {
			floats[i] = Float.intBitsToFloat(ints[i]);
		}
		return floats;
	}

	public static <V extends EntityAnimal> GenePool<V> loadNBT(Class<V> clazz, NBTTagCompound tag) {
		try {
			return new GenePool<>(clazz, tag);
		} catch (Exception err) {
			new RuntimeException("Unable to reload genes for " + clazz.getSimpleName(), err).printStackTrace();
			return new GenePool<>(clazz);
		}
	}

	public static float[] genNaturalProbabilites(float[] p) {
		float[] pr = new float[15];
		for (int i = 0; i < 15; i++) {
			GenePair pair = GenePair.Cache.genesCacheIndex[i];
			if (pair.isHomo()) {
				float v = p[pair.a.ordinal()];
				pr[i] = v * v;
			} else {
				pr[i] = 2 * p[pair.a.ordinal()] * p[pair.b.ordinal()];
			}
		}
		return pr;
	}

	public static GenePair calculateGenePair(Random rand, float[] p) {
		return GenePair.create(
				Gene.selectRandom(p, rand),
				Gene.selectRandom(p, rand)
		);
	}

	private ArrayList<IVisualPhene> getiVisualPhenes(Set<Phenotype> phenotypes) {
		ArrayList<IVisualPhene> list = new ArrayList<>();
		for (Phenotype phenotype : phenotypes) {
			if (phenotype instanceof IVisualPhene) {
				list.add(((IVisualPhene) phenotype));
			}
		}
		if (list.isEmpty()) list = null;
		return list;
	}

	private <P extends Phenotype<T, V>, T> long getSeed(P phenotype) {
		long i = getBaseSeed();
		i = i * 31L + phenotype.name.hashCode();
		return i;
	}

	private long getBaseSeed() {
		long i = Zoology.seed;
		i = i * 31L + clazz.getSimpleName().hashCode();
		return i;
	}

	public Link calculatePheneLink(Phenotype phenotype) {
		Random rand = new Random(getSeed(phenotype));

		int reservedGenes = phenotype.getReservedGenes(rand);
		int randomGenes = phenotype.getRandomGenes(rand);

		TIntArrayList assignedGenes = assignGenes(phenotype, reservedGenes, randomGenes, rand);


		float[][] weights = phenotype.assignWeightsOveride(this, assignedGenes, rand);

		if (weights == null) {
			weights = new float[assignedGenes.size()][];

			float ou = 0, ol = 0;

			for (int i = 0; i < assignedGenes.size(); i++) {
				int pi = assignedGenes.get(i);

				weights[i] = new float[15];
				float pr[] = genNaturalProbabilites(probabilities[pi]);

				float u = 0;
				float[] weight = phenotype.generateWeightBase(weights[i], assignedGenes, i, rand, dominance[pi]);

				for (int j = 0; j < weight.length; j++) {
					float v1 = weight[j];
					u += v1 * pr[j];
				}

				float wu = 0;
				float wl = 0;
				for (int j = 0; j < weight.length; j++) {
					weight[j] -= u;
					wu = Math.max(wu, weight[j]);
					wl = Math.min(wl, weight[j]);
				}

				ou += wu;
				ol += wl;
			}

			float cur_range = (ou - ol) / 2;
			float target_range = (float) phenotype.max_range;
			float mult = target_range / cur_range;

			for (int i = 0; i < assignedGenes.size(); i++) {
				float[] weight = weights[i];
				for (int j = 0; j < weight.length; j++) {
					weight[j] *= mult;
				}
			}

			float ts = 0, tou = 0, tol = 0, rn = 0, tc = 0;
			for (int i = 0; i < weights.length; i++) {
				float[] weight = weights[i];
				int pi = assignedGenes.get(i);

				float[] pr_val = probabilities[pi];
				float pr[] = genNaturalProbabilites(pr_val);
				float u = 0;
				float s2 = 0;
				float wl = 0, wu = 0;
				for (int j = 0; j < weight.length; j++) {
					float v = weight[j];
					wl = Math.min(v, wl);
					wu = Math.max(v, wu);
					u += v * pr[j];
					s2 += v * v * pr[j];
				}
				float s = s2 - u * u;
				ts += s;
				tol += wl;
				tou += wu;


				for (int j = 0; j < pr_val.length; j++) {
					rn += weight[j] * pr_val[j];
				}


				for (int a = 0; a < pr.length; a++) {
					float wA = weight[a];
					for (int b = 0; b < pr.length; b++) {
						for (Gene gA : GenePair.Cache.genesCacheIndex[a].genesAsList()) {
							for (Gene gB : GenePair.Cache.genesCacheIndex[b].genesAsList()) {
								float w = weight[GenePair.create(gA, gB).index];
								tc += pr[a] * pr[b] * w * wA / 4;
							}
						}
					}
				}
			}

			LogHelper.debug(clazz + " " + phenotype + " ");
			LogHelper.debug(Math.sqrt(ts) * 1.96 + " ");
			LogHelper.debug(Math.sqrt(ts) * 1.96 / (tou - tol) + " ");
			LogHelper.debug(tol + " " + tou);
			LogHelper.debug(rn);
			LogHelper.debug(tc / ts);
			LogHelper.debug("");
		}

		SaveHandler.markDirtyIfInitialized();

		Link link = new Link(weights, assignedGenes.toArray());
		link = phenotype.onWeightsGenerated(this, link);
		return link;
	}

	private TIntArrayList assignGenes(Phenotype phenotype, int reservedGenes, int randomGenes, Random rand) {
		TIntArrayList list = new TIntArrayList();
		for (int i = 0; i < reservedGenes; i++) {
			list.add(getReserved(phenotype, rand));
		}

		for (int i = 0; i < randomGenes; i++) {
			int val = rand.nextInt(STRAND_SIZE);
			if (!list.contains(val) && !this.reservedGenes.contains(val))
				list.add(val);
		}

		return list;
	}

	private int getReserved(Phenotype phenotype, Random rand) {
		if (reservedGenes.size() == STRAND_SIZE) {
			throw new IllegalStateException("Ran out of genes for gene code");
		}

		int i;
		do {
			i = rand.nextInt(STRAND_SIZE);
		} while (reservedGenes.contains(i));
		reservedGenes.add(i);
		phenotype.onReserve(this, i, rand);
		return i;
	}

	public GeneticStrand generate(V parent, Random rand) {
		GenePair[] pairs = new GenePair[STRAND_SIZE];
		for (int i = 0; i < pairs.length; i++) {
			pairs[i] = randomGenePair(rand, i);
		}

		for (Phenotype phenotype : phenotypes) {
			phenotype.overwriteGeneratedGeneValues(pairs, parent, this, links.get(phenotype));
		}

		return new GeneticStrand(pairs);
	}

	public GenePair randomGenePair(Random rand, int index) {
		return calculateGenePair(rand, this.probabilities[index]);
	}

	public Link getPheneLink(Phenotype phenotype) {
		return links.get(phenotype);
	}

	public NBTTagCompound toTag() {
		NBTTagCompound tags = new NBTTagCompound();

		int[] probs = new int[STRAND_SIZE * 5];
		for (int i = 0; i < STRAND_SIZE; i++) {
			System.arraycopy(floatArrToIntArr(probabilities[i]), 0, probs, i * 5, 5);
		}
		tags.setIntArray("Probabilities", probs);
		int[] bits = new int[dominance.length];
		for (int i = 0; i < dominance.length; i++) {
			bits[i] = Dominance.toBit(dominance[i]);
		}
		tags.setIntArray("Dominances", bits);
		tags.setIntArray("Reserved", reservedGenes.toArray());

		NBTTagCompound linkTags = new NBTTagCompound();
		tags.setTag("Links", linkTags);

		for (Map.Entry<Phenotype, Link> entry : links.entrySet()) {
			String name = entry.getKey().name;
			linkTags.setTag(name, entry.getValue().toNBT());
		}

		return tags;
	}


	public static class Link {

		public final float[][] weights;
		public final int[] assignedGenes;

		public Link(float[][] weights, int[] assignedGenes) {

			this.weights = weights;
			this.assignedGenes = assignedGenes;
		}

		public Link(NBTTagCompound tag) {
			assignedGenes = tag.getIntArray("Assigned");
			NBTTagList list = tag.getTagList("Weights", Constants.NBT.TAG_INT_ARRAY);
			weights = new float[list.tagCount()][];
			for (int i = 0; i < assignedGenes.length; i++) {
				weights[i] = intArrToFloatArr(list.getIntArrayAt(i));
			}
		}


		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setIntArray("Assigned", assignedGenes);

			NBTTagList list = new NBTTagList();
			for (float[] weight : weights) {
				list.appendTag(new NBTTagIntArray(floatArrToIntArr(weight)));
			}
			tag.setTag("Weights", list);
			return tag;
		}
	}
}
