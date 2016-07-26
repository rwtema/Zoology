package com.rwtema.zoology.phenotypes.visualphenes;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.networking.MessageEntityVisuals;
import com.rwtema.zoology.networking.NetworkHandler;
import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.utils.SortedIDMap;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VisualInfo {
	public static WeakHashMap<EntityAnimal, LinkedHashMap<IVisualPhene, Object>> clientData = new WeakHashMap<>();

	public static SortedIDMap<IVisualPhene> networkIDs = new SortedIDMap<>((o1, o2) -> o1.getName().compareTo(o2.getName()));

	public static HashMultimap<Class<? extends EntityAnimal>, IVisualPhene> registeredPhenes = HashMultimap.create();
	public static HashSet<RenderLivingBase> renders = new HashSet<>();
	static float partialTickTime;
	static int clientTime;
	static float renderTime;

	static {
		MinecraftForge.EVENT_BUS.register(new VisualInfo());
	}

	HashSet<Entity> renderingEntities = new HashSet<>();

	public static void register(Class<? extends EntityAnimal> clazz, IVisualPhene phene) {
		networkIDs.add(phene);
		registeredPhenes.put(clazz, phene);
	}

	public static void sendData(EntityAnimal animal, EntityPlayer player) {
		Class<? extends EntityAnimal> aClass = animal.getClass();

		Set<IVisualPhene> set = registeredPhenes.get(EntityGeneRegistry.getEquiv(aClass));
		if (set == null) return;

		if (!animal.hasCapability(PheneList.CAPABILITY, null))
			return;
		PheneList<?> pheneList = animal.getCapability(PheneList.CAPABILITY, null);

		HashSet<IVisualPhene> activePhenes = new HashSet<>();
		for (IVisualPhene iVisualPhene : set) {
			if (iVisualPhene.getEntityClass().isAssignableFrom(aClass) && iVisualPhene.hasVisualInfo(animal, pheneList)) {
				activePhenes.add(iVisualPhene);
			}
		}

		if (activePhenes.isEmpty()) return;

		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

		buffer.writeShort(activePhenes.size());
		for (IVisualPhene phene : activePhenes) {
			buffer.writeShort(networkIDs.get(phene));
			phene.writePacket(buffer, animal, pheneList);
		}

		NetworkHandler.network.sendTo(new MessageEntityVisuals(animal.getEntityId(), buffer), (EntityPlayerMP) player);
	}

	@SubscribeEvent
	public void onStartTracking(PlayerEvent.StartTracking event) {
		Entity target = event.getTarget();
		if (!(target instanceof EntityAnimal))
			return;
		sendData((EntityAnimal) target, event.getEntityPlayer());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderStart(RenderLivingEvent.Pre event) {

		EntityLivingBase entity = event.getEntity();
		if (entity == null || !(entity instanceof EntityAnimal) || renderingEntities.contains(entity)) return;
		HashMap<IVisualPhene, Object> map = clientData.get(entity);
		if (map == null) return;

		RenderLivingBase renderer = event.getRenderer();
		if (!renders.contains(renderer)) {
			renderer.addLayer(new LayerVisualData(renderer));
			renders.add(renderer);
		}

		event.setCanceled(true);
		renderingEntities.add(entity);
		try {
			EntityAnimal animal = (EntityAnimal) entity;

			GlStateManager.pushMatrix();
			GlStateManager.translate(event.getX(), event.getY(), event.getZ());
			for (Map.Entry<IVisualPhene, Object> entry : map.entrySet()) {
				entry.getKey().onRenderStart(animal, renderer, entry.getValue());
			}
			renderer.doRender(animal, 0, 0, 0, ((EntityAnimal) entity).rotationYaw, partialTickTime);
			for (Map.Entry<IVisualPhene, Object> entry : map.entrySet()) {
				entry.getKey().onRenderEnd(animal, renderer, entry.getValue());
			}
			GlStateManager.popMatrix();
			renderingEntities.remove(entity);
		} catch (Exception err) {
			renderingEntities.remove(entity);
			throw Throwables.propagate(err);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void runRenderTick(TickEvent.RenderTickEvent event) {
		partialTickTime = event.renderTickTime;
		renderTime = clientTime + partialTickTime;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void runClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START)
			clientTime++;
	}

}

