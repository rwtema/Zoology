package com.rwtema.zoology;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ClientRunnable {
	@SideOnly(Side.CLIENT)
	void run();
}
