package com.rwtema.zoology.utils;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;

public abstract class NBTSerializer<A, B extends NBTBase> {
	public static final NBTSerializer<Boolean, NBTTagByte> BOOLEAN = new NBTSerializer<Boolean, NBTTagByte>() {
		@Override
		public NBTTagByte serialize(Boolean value) {
			return new NBTTagByte(value ? (byte) 1 : (byte) 0);
		}

		@Override
		public Boolean deserialize(NBTTagByte value) {
			return value.getByte() != 0;
		}

		@Override
		public void writeToPacket(Boolean value, PacketBuffer buffer) {
			buffer.writeBoolean(value);
		}

		@Override
		public Boolean readFromPacket(PacketBuffer buffer) {
			return buffer.readBoolean();
		}
	};

	public static final NBTSerializer<Integer, NBTTagInt> INTEGER = new NBTSerializer<Integer, NBTTagInt>() {
		@Override
		public NBTTagInt serialize(Integer value) {
			return new NBTTagInt(value);
		}

		@Override
		public Integer deserialize(NBTTagInt value) {
			return value.getInt();
		}

		@Override
		public void writeToPacket(Integer value, PacketBuffer buffer) {
			buffer.writeInt(value);
		}

		@Override
		public Integer readFromPacket(PacketBuffer buffer) {
			return buffer.readInt();
		}
	};

	public static final NBTSerializer<Double, NBTTagDouble> DOUBLE = new NBTSerializer<Double, NBTTagDouble>() {
		@Override
		public NBTTagDouble serialize(Double value) {
			return new NBTTagDouble(value);
		}

		@Override
		public Double deserialize(NBTTagDouble value) {
			return value.getDouble();
		}

		@Override
		public void writeToPacket(Double value, PacketBuffer buffer) {
			buffer.writeDouble(value);
		}

		@Override
		public Double readFromPacket(PacketBuffer buffer) {
			return buffer.readDouble();
		}
	};

	public abstract B serialize(A value);

	public abstract A deserialize(B value);

	public abstract void writeToPacket(A value, PacketBuffer buffer);
	public abstract A readFromPacket(PacketBuffer buffer);

	public String makeString(A value) {
		return value.toString();
	}

	public static class Enum<A extends java.lang.Enum<A>> extends NBTSerializer<A, NBTTagByte> {
		final Class<A> clazz;

		public Enum(Class<A> clazz) {
			this.clazz = clazz;
		}

		@Override
		public NBTTagByte serialize(A value) {
			return new NBTTagByte((byte) value.ordinal());
		}

		@Override
		public A deserialize(NBTTagByte value) {
			return clazz.getEnumConstants()[value.getInt()];
		}

		@Override
		public void writeToPacket(A value, PacketBuffer buffer) {
			buffer.writeByte(value.ordinal());
		}

		@Override
		public A readFromPacket(PacketBuffer buffer) {
			return clazz.getEnumConstants()[buffer.readByte()];
		}

		@Override
		public String makeString(A value) {
			String s = value.toString();
			char[] chars = s.toCharArray();
			boolean up = true;
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (c == '_' || Character.isWhitespace(c)) {
					up = true;
				} else if (up) {
					chars[i] = Character.toUpperCase(c);
					up = false;
				} else {
					chars[i] = Character.toLowerCase(c);
				}

			}
			return new String(chars);
		}
	}
}
