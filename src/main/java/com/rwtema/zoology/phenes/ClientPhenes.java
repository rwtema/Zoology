package com.rwtema.zoology.phenes;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.rwtema.zoology.Zoology;
import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.hud.HUDHandler;
import com.rwtema.zoology.networking.MessagePhenelistRequest;
import com.rwtema.zoology.networking.NetworkHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.GuiIngameForge;

public class ClientPhenes implements HUDHandler.IHudHandler {
	public static final Cache<Entity, HashMap<Phenotype, Object>> clientCaches =
			CacheBuilder.newBuilder()
					.weakKeys()
					.expireAfterWrite(10, TimeUnit.SECONDS)
					.build();

	final static HashMap<Phenotype, Object> BLANK = new HashMap<>();

	public static HashMap<Phenotype, Object> getCache(Entity entity) {
		synchronized (clientCaches) {
			HashMap<Phenotype, Object> map = clientCaches.getIfPresent(entity);
			if (map == null) {
				NetworkHandler.network.sendToServer(new MessagePhenelistRequest(entity.getEntityId()));
				clientCaches.put(entity, BLANK);
				return BLANK;
			}

			return map;
		}
	}

	public static void init() {
		HUDHandler.register(new ClientPhenes());
	}

	@Override
	public void render(GuiIngameForge hud, ScaledResolution resolution, float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		RayTraceResult ray = mc.objectMouseOver;
		if (ray == null) return;
		Entity entity = ray.entityHit;
		if (entity == null) return;
		if (!EntityGeneRegistry.validClasses.contains(entity.getClass())) {
			return;
		}

		EntityPlayerSP thePlayer = mc.thePlayer;
		if (thePlayer == null) return;
		boolean flag = false;
		for (EnumHand enumHand : EnumHand.values()) {
			ItemStack heldItem = thePlayer.getHeldItem(enumHand);
			if (heldItem != null && heldItem.getItem() == Zoology.itemMagnifyingGlass) {
				flag = true;
				break;
			}
		}

		if (!flag) {
			return;
		}

		Set<Phenotype> phenotypes = EntityGeneRegistry.getPhenotypes((Class<? extends EntityAnimal>) entity.getClass());
		if (phenotypes == null) return;

		HashMap<Phenotype, Object> cache = getCache(entity);

		if (cache == BLANK) return;

		FontRenderer fontRenderer = hud.getFontRenderer();
		List<String> data = Lists.newArrayList();

		String name = entity.getDisplayName().getFormattedText();

		int width = fontRenderer.getStringWidth(name);

		String tildes = "-";
		while (fontRenderer.getStringWidth(tildes) < width) {
			tildes = tildes + "-";
		}

		width = Math.max(width, fontRenderer.getStringWidth(tildes));

		data.add(tildes);
		data.add(name);
		data.add(tildes);

		for (Phenotype phenotype : phenotypes) {
			Object o = cache.get(phenotype);
			if (o == null) continue;
			ITextComponent component = phenotype.buildComponent(o);
			if (component == null) continue;

			String formattedText = component.getFormattedText();
			data.add(formattedText);
			width = Math.max(width, fontRenderer.getStringWidth(formattedText));
		}

		int height = data.size() * fontRenderer.FONT_HEIGHT;

		int x = (resolution.getScaledWidth() - width) / 2;
		int y = (resolution.getScaledHeight() - height) / 2;
		GlStateManager.pushMatrix();

		final int border = 5;

		Gui.drawRect(x - border, y - border, x + width + border, y + height + border, 0x90101010);
		for (int i = 0; i < data.size(); i++) {
			String s = data.get(i);
			if (i < 3) {
				hud.drawCenteredString(fontRenderer, s, x + width / 2, y + i * fontRenderer.FONT_HEIGHT, 0xffffffff);
			} else {
				hud.drawString(fontRenderer, s, x, y + i * fontRenderer.FONT_HEIGHT, 0xffffffff);
			}
		}

		GlStateManager.popMatrix();
	}
}
