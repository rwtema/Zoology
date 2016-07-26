package com.rwtema.zoology.networking;

import com.rwtema.zoology.phenotypes.visualphenes.IVisualPhene;
import com.rwtema.zoology.phenotypes.visualphenes.VisualInfo;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageEntityVisuals extends NetworkHandler.Message {
	LinkedHashMap<IVisualPhene, Object> traits;
	private int entityId;
	private ByteBuf buffer;

	public MessageEntityVisuals() {

	}

	public MessageEntityVisuals(int entityId, PacketBuffer buffer) {
		this.entityId = entityId;
		this.buffer = buffer;
	}

	@Override
	public NetworkHandler.Message runClient(MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(entityId);
				if (!(entity instanceof EntityAnimal)) return;
				EntityAnimal animal = (EntityAnimal) entity;
				VisualInfo.clientData.put(animal, traits);
				for (Map.Entry<IVisualPhene, Object> entry : traits.entrySet()) {
					entry.getKey().onApplyClient(animal, entry.getValue());
				}
			}
		});
		return null;
	}

	@Override
	public void readData(PacketBuffer buffer) {
		entityId = buffer.readInt();
		int n = buffer.readShort();
		traits = new LinkedHashMap<>();
		for (int i = 0; i < n; i++) {
			IVisualPhene phene = VisualInfo.networkIDs.get(buffer.readShort());
			Object o = phene.readPacket(buffer);
			traits.put(phene, o);
		}
	}

	@Override
	protected void writeData(PacketBuffer buffer) {
		buffer.writeInt(entityId);
		buffer.writeBytes(this.buffer);
	}
}
