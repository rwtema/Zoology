package com.rwtema.zoology.networking;

import com.rwtema.zoology.phenes.ClientPhenes;
import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenes.Phenotype;
import java.util.HashMap;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessagePhenelist extends NetworkHandler.Message {
	int id;
	HashMap<Phenotype, Object> values;
	private PheneList<?> capability;

	public MessagePhenelist() {

	}

	public MessagePhenelist(PheneList<?> capability) {
		this.capability = capability;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public NetworkHandler.Message runClient(MessageContext ctx) {
		Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(id);
		if (entity != null) {
			synchronized (ClientPhenes.clientCaches) {
				ClientPhenes.clientCaches.put(entity, values);
			}
		}

		return null;
	}

	@Override
	public void readData(PacketBuffer buffer) {
		id = buffer.readInt();
		int n = buffer.readInt();
		values = new HashMap<>();
		for (int i = 0; i < n; i++) {
			String s = buffer.readStringFromBuffer(100);
			Phenotype phenotype = Phenotype.registry.get(s);
			Object o = phenotype.serializer.readFromPacket(buffer);
			values.put(phenotype, o);
		}
	}

	@Override
	protected void writeData(PacketBuffer buffer) {
		int id = capability.parent.getEntityId();
		buffer.writeInt(id);
		Set<Phenotype<?, ?>> set = (Set<Phenotype<?, ?>>) capability.getRegisteredPhenes();

		buffer.writeInt(set.size());
		for (Phenotype phenotype : set) {
			buffer.writeString(phenotype.name);
			Object o = capability.getValue(phenotype);
			phenotype.serializer.writeToPacket(o, buffer);
		}
	}
}
