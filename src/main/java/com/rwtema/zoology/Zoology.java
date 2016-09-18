package com.rwtema.zoology;

import com.rwtema.zoology.animals.DyeGenetics;
import com.rwtema.zoology.command.CommandDebug;
import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.entities.EntityHandler;
import com.rwtema.zoology.item.ItemMagnifyingGlass;
import com.rwtema.zoology.networking.NetworkHandler;
import com.rwtema.zoology.phenes.ClientPhenes;
import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenotypes.Phenotypes;
import com.rwtema.zoology.potion.PotionsHandler;
import com.rwtema.zoology.save.SaveHandler;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = Zoology.MODID, version = Zoology.VERSION, dependencies = "required-after:Forge@[12.18.1.2085,)")
public class Zoology {
	public static final String MODID = "Zoology";
	public static final String VERSION = "1.0";
	public static final int VALUES_PER_INTEGER = 8;
	public static final int INTEGERS_PER_STRAND = 64;
	public static final int STRAND_SIZE = VALUES_PER_INTEGER * INTEGERS_PER_STRAND;
	public static final Random rand = new Random();
	public final static boolean isDeobf;
	public static int seed = 0;
	public static int version = 0;

	@SidedProxy(serverSide = "com.rwtema.zoology.Proxy", clientSide = "com.rwtema.zoology.ProxyClient")
	public static Proxy proxy;
	public static ItemMagnifyingGlass itemMagnifyingGlass;

	static {
		DyeGenetics.init();
	}

	static {
		boolean deObf;
		try {
			World.class.getMethod("getBlockState", BlockPos.class);
			deObf = true;
		} catch (NoSuchMethodException e) {
			deObf = false;
		}

		isDeobf = deObf;

//		if (isDeobf) {
//			float[] thresholds = new float[]{0, 0.2F, 0.4F, 0.5F, 0.8F, 0.9F, 1};
//			for (float v = -0.05F; v <= 1.1F; v += 0.05F) {
//				int i = Arrays.binarySearch(thresholds, v);
//				int i2 = i >= 0 ? i : -i - 2;
//				LogHelper.info(String.format("v=%s, i=%s, i2=%s ", v, i, i2));
//			}
//		}
	}

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
		configuration.load();
		seed = configuration.getInt("Seed", "General", rand.nextInt(), Integer.MIN_VALUE, Integer.MAX_VALUE, "Changes the seed used to generate the genetic code..");
		if (configuration.hasChanged())
			configuration.save();


		Phenotypes.registerPhenes();

		itemMagnifyingGlass = new ItemMagnifyingGlass();
		GameRegistry.register(itemMagnifyingGlass, new ResourceLocation(MODID, "MagnifyingGlass"));

		NetworkHandler.init();

		proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				ModelLoader.setCustomModelResourceLocation(itemMagnifyingGlass, 0, new ModelResourceLocation("zoology:magnifying_glass", "inventory"));
				ClientPhenes.init();
			}
		});

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemMagnifyingGlass), " r ", "rgr", "sr ", 's', "stickWood", 'g', "paneGlassColorless", 'r', "ingotIron"));

		PotionsHandler.init();

		if (LogHelper.isDeObf) {
			for (Class<? extends EntityAnimal> clazz : EntityGeneRegistry.getClasses()) {
				EntityGeneRegistry.getGenePool(clazz);
			}
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		CapabilityManager.INSTANCE.register(PheneList.class, new Capability.IStorage<PheneList>() {
			@Override
			public NBTBase writeNBT(Capability<PheneList> capability, PheneList instance, EnumFacing side) {
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<PheneList> capability, PheneList instance, EnumFacing side, NBTBase nbt) {
				instance.deserializeNBT((NBTTagCompound) nbt);
			}
		}, new Callable<PheneList>() {
			@Override
			public PheneList call() throws Exception {
				return null;
			}
		});
		MinecraftForge.EVENT_BUS.register(new EntityHandler());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandDebug());
		SaveHandler.init();
	}

}
