package com.rwtema.zoology.networking;

import java.io.IOException;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import org.apache.commons.lang3.Validate;

public class DataKey {
	static final DataParameter<DataKey> DATA_MANAGER_KEY;
	static final DataSerializer<DataKey> DATA_SERIALIZER;

	static {
		DATA_SERIALIZER = new DataSerializer<DataKey>() {
			@Override
			public void write(PacketBuffer buf, DataKey value) {
				value.write(buf);
			}

			@Override
			public DataKey read(PacketBuffer buf) throws IOException {
				return new DataKey(buf);
			}

			@Override
			public DataParameter<DataKey> createKey(int id) {
				return new DataParameter<>(id, this);
			}
		};
		DataSerializers.registerSerializer(DATA_SERIALIZER);
		DATA_MANAGER_KEY = EntityDataManager.createKey(EntityAnimal.class, DATA_SERIALIZER);
	}

	public DataKey(PacketBuffer buf) {
		read(buf);
	}

	protected void read(PacketBuffer buf) {

	}

	public static void init() {
		Validate.isTrue(DATA_SERIALIZER != null);
	}

	public static void register(Class<? extends EntityAnimal> clazz) {

	}

	protected void write(PacketBuffer buf) {

	}


}
