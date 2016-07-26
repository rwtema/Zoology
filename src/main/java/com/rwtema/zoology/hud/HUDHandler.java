package com.rwtema.zoology.hud;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HUDHandler {
	private static ArrayList<IHudHandler> handlers = new ArrayList<>();

	static {
		MinecraftForge.EVENT_BUS.register(new HUDHandler());
	}

	public static void register(IHudHandler handler) {
		handlers.add(handler);
	}

	@SubscribeEvent
	public void hudDraw(RenderGameOverlayEvent.Post event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
		GuiIngameForge currentScreen = (GuiIngameForge) Minecraft.getMinecraft().ingameGUI;
		for (IHudHandler handler : handlers) {
			handler.render(currentScreen, event.getResolution(), event.getPartialTicks());
		}
	}

	public interface IHudHandler {
		void render(GuiIngameForge hud, ScaledResolution resolution, float partialTicks);
	}


}
