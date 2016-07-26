package com.rwtema.zoology.networking;

import com.rwtema.zoology.phenes.PheneList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePhenelistRequest extends NetworkHandler.Message {
	int entityID;

	public MessagePhenelistRequest(int entityID) {
		this.entityID = entityID;
	}

	public MessagePhenelistRequest() {
		super();
	}

	@Override
	public void readData(PacketBuffer buffer) {
		entityID = buffer.readInt();
	}

	@Override
	protected void writeData(PacketBuffer buffer) {
		buffer.writeInt(entityID);
	}

	@Override
	public NetworkHandler.Message runServer(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		Entity entity = player.worldObj.getEntityByID(entityID);
		if (entity == null || !entity.hasCapability(PheneList.CAPABILITY, null)) {
			return null;
		}
		PheneList<?> capability = entity.getCapability(PheneList.CAPABILITY, null);
		if(capability == null) return null;

		return new MessagePhenelist(capability);
	}
}
