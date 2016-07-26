package com.rwtema.zoology.phenotypes;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.Phenotype;
import com.rwtema.zoology.utils.NBTSerializer;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class PhenotypeHorseType extends Phenotype<PhenotypeHorseType.HorseValue, EntityHorse> {
	final static int NUM_HORSE_BASE;
	final static int NUM_HORSE_MARKINGS;

	static {
		NUM_HORSE_BASE = ((String[]) ObfuscationReflectionHelper.getPrivateValue(EntityHorse.class, null, "horseTextures", "field_110268_bz")).length;
		NUM_HORSE_MARKINGS = ((String[]) ObfuscationReflectionHelper.getPrivateValue(EntityHorse.class, null, "horseMarkingTextures", "field_110291_bB")).length;
	}


	public PhenotypeHorseType(String name, Class<HorseValue> valueClazz, Class<EntityHorse> entityClazz, NBTSerializer<HorseValue, ?> serializer, int reserved_genes, int random_genes, double max_range) {
		super(name, valueClazz, entityClazz, new NBTSerializer<HorseValue, NBTTagInt>() {
			@Override
			public NBTTagInt serialize(HorseValue value) {
				return new NBTTagInt(value.horseBase | (value.horseMarkings << 8));
			}

			@Override
			public HorseValue deserialize(NBTTagInt value) {
				int i = value.getInt();
				int horseBase = i & 255;
				int horseMarkings = (i & 65280) >> 8;
				return HorseValue.valueOf(horseBase, horseMarkings);
			}

			@Override
			public void writeToPacket(HorseValue value, PacketBuffer buffer) {
				buffer.writeByte(value.horseBase);
				buffer.writeByte(value.horseMarkings);
			}

			@Override
			public HorseValue readFromPacket(PacketBuffer buffer) {
				short horseBase = buffer.readUnsignedByte();
				short horseMarkings = buffer.readUnsignedByte();
				return HorseValue.valueOf(horseBase, horseMarkings);
			}
		}, reserved_genes, random_genes, max_range);
	}

	@Override
	public void initApply(EntityHorse entity, HorseValue value) {

	}

	@Override
	public HorseValue initValue(double v, GeneticStrand strand, GenePool pool, GenePool.Link link) {
		return null;
	}

	@Override
	public void overwriteGeneratedGeneValues(GenePair[] pairs, EntityHorse parent, GenePool<EntityHorse> entityHorseGenePool, GenePool.Link link) {
		int i = parent.getHorseVariant();
//		int horseBase = i & 255;
//		int horseMarkings = (i & 65280) >> 8;
//		return HorseValue.valueOf(horseBase, horseMarkings);
	}



	public final static class HorseValue {
		private static final Interner<HorseValue> INTERNER = Interners.newStrongInterner();
		byte horseBase;
		byte horseMarkings;

		private HorseValue(byte horseBase, byte horseMarkings) {
			this.horseBase = horseBase;
			this.horseMarkings = horseMarkings;
		}

		public static HorseValue valueOf(int horseBase, int horseMarkings) {
			HorseValue value = new HorseValue((byte) horseBase, (byte) horseMarkings);
			return INTERNER.intern(value);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			HorseValue that = (HorseValue) o;
			return horseBase == that.horseBase && horseMarkings == that.horseMarkings;
		}

		@Override
		public int hashCode() {
			return 31 * (int) horseBase + (int) horseMarkings;
		}
	}
}
