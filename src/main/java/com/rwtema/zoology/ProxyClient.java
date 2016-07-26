package com.rwtema.zoology;

import com.rwtema.zoology.networking.NetworkHandler;
import net.minecraft.client.model.ModelSheep1;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ProxyClient extends Proxy {
	@Override
	public NetworkHandler.Message runMessage(NetworkHandler.Message message, MessageContext ctx) {
		if (ctx.side == Side.CLIENT)
			return message.runClient(ctx);
		else
			return message.runServer(ctx);
	}

	@Override
	public void run(ClientRunnable runnable) {
		runnable.run();
	}

	public static ModelSheep1 furLayer = new ModelSheep1();


}
