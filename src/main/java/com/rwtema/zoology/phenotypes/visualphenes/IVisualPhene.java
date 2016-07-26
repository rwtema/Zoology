package com.rwtema.zoology.phenotypes.visualphenes;

import com.rwtema.zoology.phenes.PheneList;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.network.PacketBuffer;

public interface IVisualPhene<T, V extends EntityAnimal> {
	String getName();

	Class<V> getEntityClass();

	void writePacket(PacketBuffer buffer, V animal, PheneList<?> pheneList);

	T readPacket(PacketBuffer buffer);

	boolean hasVisualInfo(V animal, PheneList<?> pheneList);

	void onApplyClient(V entity, T value);

	void onRenderStart(V entity, RenderLivingBase renderer, T value);

	void onRenderEnd(V entity, RenderLivingBase renderer, T value);

	void renderLayer(V animal, RenderLivingBase renderer, T value, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale);
}
