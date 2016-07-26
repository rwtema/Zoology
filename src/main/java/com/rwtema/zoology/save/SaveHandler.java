package com.rwtema.zoology.save;

import com.rwtema.zoology.Zoology;
import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.genes.GenePool;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class SaveHandler extends WorldSavedData {
	public static final String SAVE_DATA_NAME = Zoology.MODID + "SaveData";
	public static SaveHandler manager;
	NBTTagCompound oldSaveData = new NBTTagCompound();

	public SaveHandler(String name) {
		super(name);
	}

	public static void init() {
		WorldServer worldServer = DimensionManager.getWorld(0);
		manager = (SaveHandler) worldServer.loadItemData(SaveHandler.class, SAVE_DATA_NAME);
		if (manager == null) {
			EntityGeneRegistry.recreateGenePool();
			manager = new SaveHandler(SAVE_DATA_NAME);
			worldServer.setItemData(SAVE_DATA_NAME, manager);
			manager.markDirty();
		}
	}

	public static void markDirtyIfInitialized() {
		if (manager != null) {
			manager.markDirty();
		}
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbt) {
		if (nbt.getInteger("version") != Zoology.version) {
			oldSaveData = new NBTTagCompound();
			return;
		}

		oldSaveData = (NBTTagCompound) nbt.copy();
		Set<Class<? extends EntityAnimal>> classes = EntityGeneRegistry.getClasses();
		for (Class<? extends EntityAnimal> aClass : classes) {
			String s = EntityList.getEntityStringFromClass(aClass);
			NBTTagCompound compoundTag = nbt.getCompoundTag(s);
			EntityGeneRegistry.setGenePool(aClass, GenePool.loadNBT(aClass, compoundTag));
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
		nbt.setInteger("version", Zoology.version);
		nbt.setInteger("seed", Zoology.seed);

		nbt.merge(oldSaveData);

		Set<Class<? extends EntityAnimal>> classes = EntityGeneRegistry.getClasses();
		for (Class<? extends EntityAnimal> aClass : classes) {
			String s = EntityList.getEntityStringFromClass(aClass);
			GenePool<EntityAnimal> pool = EntityGeneRegistry.getGenePool(aClass);
			if (pool == null) continue;
			NBTTagCompound tag = pool.toTag();
			nbt.setTag(s, tag);
		}
		return nbt;
	}
}
