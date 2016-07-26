package com.rwtema.zoology.networking;

import com.rwtema.zoology.Zoology;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetworkHandler {
	public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Zoology.MODID);
	public static final IMessageHandler<Message, Message> handler = new Handler();


	public static void init() {
		network.registerMessage(handler, MessagePhenelistRequest.class, 0, Side.SERVER);
		network.registerMessage(handler, MessagePhenelist.class, 1, Side.CLIENT);
		network.registerMessage(handler, MessageEntityVisuals.class, 2, Side.CLIENT);
	}

	public static class Handler implements IMessageHandler<Message, Message> {

		@Override
		public Message onMessage(Message message, MessageContext ctx) {
			return Zoology.proxy.runMessage(message, ctx);
		}
	}

	public static abstract class Message implements IMessage {

		PacketBuffer buffer;

		public Message() {

		}

		@Override
		public void fromBytes(ByteBuf buf) {
			buffer = new PacketBuffer(buf);
			readData(buffer);

		}

		public abstract void readData(PacketBuffer buffer);

		@Override
		public void toBytes(ByteBuf buf) {
			writeData(new PacketBuffer(buf));
		}

		protected abstract void writeData(PacketBuffer buffer);

		public Message runServer(MessageContext ctx) {
			throw new IllegalStateException("Unexpected Side");
		}

		@SideOnly(Side.CLIENT)
		public Message runClient(MessageContext ctx) {
			throw new IllegalStateException("Unexpected Side");
		}
	}
}
