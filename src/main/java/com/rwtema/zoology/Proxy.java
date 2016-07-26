package com.rwtema.zoology;

import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.networking.NetworkHandler;
import com.rwtema.zoology.phenotypes.PhenotypeDrop;
import com.rwtema.zoology.phenotypes.PhenotypeRareDrop;
import com.rwtema.zoology.phenotypes.PhenotypeWoolDrop;
import com.rwtema.zoology.phenotypes.Phenotypes;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class Proxy {
	public void run(ClientRunnable runnable) {

	}

	public NetworkHandler.Message runMessage(NetworkHandler.Message message, MessageContext ctx) {
		if (ctx.side == Side.CLIENT)
			return null;
		else
			return message.runServer(ctx);
	}

}
