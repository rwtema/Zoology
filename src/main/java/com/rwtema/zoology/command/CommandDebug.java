package com.rwtema.zoology.command;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rwtema.zoology.Zoology;
import com.rwtema.zoology.ai.EntityAICustomMate;
import com.rwtema.zoology.entities.EntityGeneRegistry;
import com.rwtema.zoology.genes.Gene;
import com.rwtema.zoology.genes.GenePair;
import com.rwtema.zoology.genes.GenePool;
import com.rwtema.zoology.genes.GeneticStrand;
import com.rwtema.zoology.phenes.PheneList;
import com.rwtema.zoology.phenes.Phenotype;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.Validate;

@SuppressWarnings("Guava")
public class CommandDebug extends CommandBase {
	private final static HashMap<String, SubCommand> commands = new HashMap<>();

	static {
		register(new SubCommand("spawn_phene_factor") {
			ParameterClass<EntityAnimal> param1 = addParam(new ParameterClass(EntityAnimal.class));
			ParameterPhenotype param2 = addParam(new ParameterPhenotype(param1));
			ParameterString param3 = addParam(new ParameterString() {
				@Override
				public List<String> values(ParamMap assignedValues) {
					if (assignedValues.isAssigned(param2)) {
						Phenotype value = assignedValues.getValue(param2);
						Class valueClass = value.getValueClass();
						if (valueClass == Boolean.class) {
							return ImmutableList.of(Boolean.TRUE.toString(), Boolean.FALSE.toString());
						}

						Object[] enumConstants = valueClass.getEnumConstants();
						if (enumConstants != null) {
							ArrayList<String> list = Lists.newArrayListWithExpectedSize(enumConstants.length);
							for (Object constant : enumConstants) {
								list.add(constant.toString());
							}
							return list;
						}
					}

					return ImmutableList.of();
				}
			});

			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				Class<? extends EntityAnimal> clazz = parameters.getValue(param1);
				GenePool<EntityAnimal> genePool = EntityGeneRegistry.getGenePool(clazz);
				Phenotype phenotype = parameters.getValue(param2);
				Validate.isTrue(genePool.phenotypes.contains(phenotype));
				String target = parameters.getValue(param3);

				EntityAnimal animal = (EntityAnimal) EntityList.createEntityByName(EntityList.CLASS_TO_NAME.get(clazz), sender.getEntityWorld());
				PheneList pheneList = animal.getCapability(PheneList.CAPABILITY, null);
				GenePool.Link link = genePool.getPheneLink(phenotype);

				Random rand = animal.worldObj.rand;
				GeneticStrand strand = genePool.generate(animal, rand);

				int[] assignedGenes = link.assignedGenes;

				int k = 0;

				while (k < 1000000) {
					k++;
					int i = assignedGenes[rand.nextInt(assignedGenes.length)];

					strand.strandValues[i] = GenePair.create(Gene.rand(rand), Gene.rand(rand));
					String string = (phenotype.calcValue(strand, genePool, link)).toString();
					if (target.equalsIgnoreCase(string))
						break;
				}

				sender.addChatMessage(new TextComponentString("Iter =" +k));

				pheneList.generateFromStrand(EntityGeneRegistry.getPhenotypes(clazz), genePool, strand);

				sender.getPosition();

				double x = sender.getPositionVector().xCoord;
				double y = sender.getPositionVector().yCoord;
				double z = sender.getPositionVector().zCoord;

				animal.setLocationAndAngles(x, y, z, animal.rotationYaw, animal.rotationPitch);
				animal.onInitialSpawn(sender.getEntityWorld().getDifficultyForLocation(new BlockPos(animal)), null);
				sender.getEntityWorld().spawnEntityInWorld(animal);
			}
		});

		register(new SubCommand("spawn_phene_numeric") {
			ParameterClass<EntityAnimal> param1 = addParam(new ParameterClass(EntityAnimal.class));
			ParameterPhenotype param2 = addParam(new ParameterPhenotype(param1));
			ParameterDouble param3 = addParam(new ParameterDouble());

			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				Class<? extends EntityAnimal> clazz = parameters.getValue(param1);
				GenePool<EntityAnimal> genePool = EntityGeneRegistry.getGenePool(clazz);
				Phenotype phenotype = parameters.getValue(param2);
				Validate.isTrue(genePool.phenotypes.contains(phenotype));
				Validate.isTrue(Number.class.isAssignableFrom(phenotype.getValueClass()));
				double target = parameters.getValue(param3);

				EntityAnimal animal = (EntityAnimal) EntityList.createEntityByName(EntityList.CLASS_TO_NAME.get(clazz), sender.getEntityWorld());
				PheneList pheneList = animal.getCapability(PheneList.CAPABILITY, null);
				GenePool.Link link = genePool.getPheneLink(phenotype);

				GeneticStrand strand = genePool.generate(animal, animal.worldObj.rand);

				double value = ((Number) phenotype.calcValue(strand, genePool, link)).doubleValue();

				for (int i : link.assignedGenes) {
					GenePair bestPair = strand.strandValues[i];
					for (GenePair pair : GenePair.Cache.genesCacheIndex) {
						if (pair == bestPair) continue;
						strand.strandValues[i] = pair;

						double newValue = ((Number) phenotype.calcValue(strand, genePool, link)).doubleValue();
						if (Math.abs(newValue - target) < Math.abs(value - target)) {
							value = newValue;
							bestPair = pair;
						} else {
							strand.strandValues[i] = bestPair;
						}
					}
				}

				pheneList.generateFromStrand(EntityGeneRegistry.getPhenotypes(clazz), genePool, strand);

				sender.getPosition();

				double x = sender.getPositionVector().xCoord;
				double y = sender.getPositionVector().yCoord;
				double z = sender.getPositionVector().zCoord;

				animal.setLocationAndAngles(x, y, z, animal.rotationYaw, animal.rotationPitch);
				animal.onInitialSpawn(sender.getEntityWorld().getDifficultyForLocation(new BlockPos(animal)), null);
				sender.getEntityWorld().spawnEntityInWorld(animal);
			}
		});
		register(new SubCommand("reset_age") {
			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				for (EntityAnimal animal : sender.getEntityWorld().getEntities(EntityAnimal.class, Predicates.alwaysTrue())) {
					animal.setGrowingAge(0);
				}
			}
		});

		register(new SubCommand("mate_all") {
			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				for (EntityAnimal animal : sender.getEntityWorld().getEntities(EntityAnimal.class, Predicates.alwaysTrue())) {
					animal.setGrowingAge(0);
					if (sender instanceof EntityPlayer) {
						animal.setInLove((EntityPlayer) sender);
					}
				}
			}
		});

		register(new SubCommand("test_save") {
			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				for (int k = 0; k < 10000; k++) {
					GeneticStrand strand = new GeneticStrand();
					strand.strandValues = new GenePair[Zoology.STRAND_SIZE];

					for (int i = 0; i < strand.strandValues.length; i++) {
						strand.strandValues[i] = GenePair.create(Gene.rand(Zoology.rand), Gene.rand(Zoology.rand));
					}

					NBTTagIntArray nbt = strand.serializeNBT();
					GeneticStrand strand1 = new GeneticStrand();
					strand1.deserializeNBT(nbt);

					for (int i = 0; i < strand.strandValues.length; i++) {
						if (strand.strandValues[i] != strand1.strandValues[i]) {
							sender.addChatMessage(new TextComponentString("Fail"));
							return;
						}
					}
				}

				sender.addChatMessage(new TextComponentString("Success"));
			}
		});

		register(new SubCommand("find_best") {
			ParameterClass<EntityAnimal> param1 = addParam(new ParameterClass(EntityAnimal.class));
			ParameterPhenotype param2 = addParam(new ParameterPhenotype(param1));

			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				Class<? extends EntityAnimal> clazz = parameters.getValue(param1);
				GenePool<EntityAnimal> genePool = EntityGeneRegistry.getGenePool(clazz);
				Phenotype phenotype = parameters.getValue(param2);
				Validate.isTrue(genePool.phenotypes.contains(phenotype));
				Validate.isTrue(Comparable.class.isAssignableFrom(phenotype.getValueClass()));
				EntityAnimal b = null;
				Comparable a = null;
				for (EntityAnimal entity : sender.getEntityWorld().getEntities(clazz, Predicates.alwaysTrue())) {
					if (!entity.hasCapability(PheneList.CAPABILITY, null)) {
						continue;
					}
					PheneList list = entity.getCapability(PheneList.CAPABILITY, null);
					Comparable value = (Comparable) list.getValue(phenotype);
					if (a == null || (a.compareTo(value) < 0)) {
						a = value;
						b = entity;
					}
				}
				if (b != null) {
					PotionEffect potioneffect = new PotionEffect(MobEffects.GLOWING, 200, 0);
					b.addPotionEffect(potioneffect);
				}
			}
		});

		register(new SubCommand("select") {
			ParameterClass<EntityAnimal> param1 = addParam(new ParameterClass(EntityAnimal.class));
			ParameterPhenotype param2 = addParam(new ParameterPhenotype(param1));
			ParameterInteger param3 = addParam(new ParameterInteger());
			Parameter<Boolean> param4 = addParam(new ParameterDefault<>(new ParameterBoolean(), "true"));

			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				Class<? extends EntityAnimal> clazz = parameters.getValue(param1);
				GenePool<EntityAnimal> genePool = EntityGeneRegistry.getGenePool(clazz);
				Phenotype phenotype = parameters.getValue(param2);
				Validate.isTrue(genePool.phenotypes.contains(phenotype));
				Validate.isTrue(Comparable.class.isAssignableFrom(phenotype.getValueClass()));

				int num = parameters.getValue(param3);
				boolean t = parameters.getValue(param4);

				Set<EntityAnimal> toSurvive = Sets.newIdentityHashSet();
				List<EntityAnimal> entities = sender.getEntityWorld().getEntities(clazz, Predicates.alwaysTrue());
				for (EntityAnimal entity : entities) {
					if (entity.isDead) continue;
					if (!entity.hasCapability(PheneList.CAPABILITY, null)) {
						continue;
					}
					PheneList list = entity.getCapability(PheneList.CAPABILITY, null);
					Comparable value = (Comparable) list.getValue(phenotype);

					if (toSurvive.size() > num) {
						EntityAnimal worst = entity;
						Comparable worstValue = value;
						for (EntityAnimal animal : toSurvive) {
							Comparable o = (Comparable) animal.getCapability(PheneList.CAPABILITY, null).getValue(phenotype);
							if (t ? o.compareTo(worstValue) > 0 : o.compareTo(worstValue) < 0) {
								worstValue = o;
								worst = animal;
							}
						}
						if (worst != entity) {
							toSurvive.remove(worst);
							toSurvive.add(entity);
						}
					} else
						toSurvive.add(entity);
				}
				for (EntityAnimal entity : entities) {
					if (!toSurvive.contains(entity)) {
						entity.setDead();
					}
				}
			}
		});

		register(new SubCommand("mate_best") {
			ParameterClass<EntityAnimal> param1 = addParam(new ParameterClass(EntityAnimal.class));
			ParameterPhenotype param2 = addParam(new ParameterPhenotype(param1));

			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				Class<? extends EntityAnimal> clazz = parameters.getValue(param1);
				GenePool<EntityAnimal> genePool = EntityGeneRegistry.getGenePool(clazz);
				Phenotype phenotype = parameters.getValue(param2);
				Validate.isTrue(genePool.phenotypes.contains(phenotype));
				Validate.isTrue(Comparable.class.isAssignableFrom(phenotype.getValueClass()));

				EntityAnimal b1 = null;
				EntityAnimal b2 = null;
				Comparable a1 = null;
				Comparable a2 = null;
				for (EntityAnimal entity : sender.getEntityWorld().getEntities(clazz, Predicates.alwaysTrue())) {
					if (!entity.hasCapability(PheneList.CAPABILITY, null)) {
						continue;
					}
					PheneList list = entity.getCapability(PheneList.CAPABILITY, null);
					Comparable value = (Comparable) list.getValue(phenotype);
					if (b1 == null) {
						b1 = entity;
						a1 = value;
					} else if (b2 == null) {
						b2 = entity;
						a2 = value;
					} else {
						boolean c1 = value.compareTo(a1) > 0;
						boolean c2 = value.compareTo(a2) > 0;
						if (c1 || c2) {
							if (c1 && c2) {
								if (a1.compareTo(a2) > 0) {
									a2 = value;
									b2 = entity;
								} else {
									a1 = value;
									b1 = entity;
								}
							} else if (c1) {
								a1 = value;
								b1 = entity;
							} else {
								a2 = value;
								b2 = entity;
							}
						}
					}
				}

				if (b1 != null && b2 != null) {
					b1.setGrowingAge(0);
					b2.setGrowingAge(0);
					EntityAgeable child = EntityAICustomMate.mateAnimals(b1, b2, false);
					if (child == null) return;

					ITextComponent text = phenotype.getDisplayValue(b1.getCapability(PheneList.CAPABILITY, null).getValue(phenotype));
					text.appendText(" + ");
					text.appendSibling(phenotype.getDisplayValue(b2.getCapability(PheneList.CAPABILITY, null).getValue(phenotype)));
					text.appendText(" = ");
					text.appendSibling(phenotype.getDisplayValue(child.getCapability(PheneList.CAPABILITY, null).getValue(phenotype)));
					sender.addChatMessage(text);
				}
			}
		});

		register(new SubCommand("overmate_all") {
			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				List<EntityAnimal> list = sender.getEntityWorld().getEntities(EntityAnimal.class, Predicates.alwaysTrue());
				int t = list.size();
				for (EntityAnimal a : list) {
					if (a.isDead) continue;
					for (EntityAnimal b : list) {
						if (b.isDead) continue;
						if (a != b && a.getClass() == b.getClass()) {
							t++;
							EntityAICustomMate.mateAnimals(a, b, false);
							if (t > 1000) return;
						}
					}
				}
			}
		});

		register(new SubCommand("kill_all") {
			Parameter<Optional<Class<? extends Entity>>> param1 = addParam(new ParameterOptional<>(new ParameterClass(Entity.class)));

			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				Optional<Class<? extends Entity>> value = parameters.getValue(param1);

				for (Entity living : sender.getEntityWorld().getEntities(value.or(EntityLiving.class), Predicates.alwaysTrue())) {
					living.setDead();
				}
			}
		});

		register(new SubCommand("kill_xp") {
			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				for (EntityXPOrb entityXPOrb : sender.getEntityWorld().getEntities(EntityXPOrb.class, Predicates.alwaysTrue())) {
					entityXPOrb.setDead();
				}
			}
		});

		register(new SubCommand("kill_items") {
			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				for (EntityItem entityItem : sender.getEntityWorld().getEntities(EntityItem.class, Predicates.alwaysTrue())) {
					entityItem.setDead();
				}
			}
		});

		register(new SubCommand("kill_some") {
			ParameterInteger param1 = addParam(new ParameterInteger());

			@Override
			public void process(MinecraftServer server, ICommandSender sender, ParamMap parameters) {
				List<EntityLiving> list = sender.getEntityWorld().getEntities(EntityLiving.class, Predicates.alwaysTrue());
				Collections.sort(list, new Comparator<EntityLiving>() {
					@Override
					public int compare(EntityLiving o1, EntityLiving o2) {
						int y = o2.getAge();
						int x = o1.getAge();
						return ((x < y) ? -1 : ((x == y) ? 0 : 1));
					}
				});

				list = Lists.reverse(list);

				int integer = parameters.getValue(param1);
				byte b = 0;
				for (EntityLiving entityLiving : list) {
					if (b < integer) {
						b++;
						continue;
					}
					entityLiving.setDead();
				}
			}
		});
	}

	public static void register(SubCommand command) {
		commands.put(command.name, command);
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Nonnull
	@Override
	public String getCommandName() {
		return "gen_debug";
	}

	@Nonnull
	@Override
	public String getCommandUsage(@Nonnull ICommandSender sender) {
		return "gen_debug";
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		if (args.length == 0) throw new CommandException("Unknown Command");
		String cmd = args[0];

		SubCommand command = commands.get(cmd);
		if (command == null) {
			throw new CommandException("Unknown Command");
		}

		ParamMap map = new ParamMap();
		try {
			for (int i = 0; i < command.parameters.size(); i++) {
				Parameter parameter = command.parameters.get(i);
				if ((i + 1) < args.length) {
					Object convert = parameter.convert(args[i + 1], sender, server);
					map.putValue(parameter, convert);
				} else {
					map.putValue(parameter, parameter.getMissingValue());
				}
			}
		} catch (IllegalArgumentException err) {
			throw new CommandException(err.getMessage());
		}

		command.process(server, sender, map);
	}

	@Nonnull
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 0 || args.length == 1) {
			return getListOfStringsMatchingLastWord(args, commands.keySet());
		}
		String name = args[0];
		SubCommand subCommand = commands.get(name);
		if (subCommand == null) {
			return Collections.emptyList();
		}

		try {
			int index = args.length - 2;
			if (index >= subCommand.parameters.size()) return Collections.emptyList();
			Parameter p = subCommand.parameters.get(index);
			ParamMap map = new ParamMap();
			for (int i = 0; i < index; i++) {
				Parameter parameter = subCommand.parameters.get(i);
				map.putValue(parameter, parameter.convert(args[i + 1], sender, server));
			}
			return getListOfStringsMatchingLastWord(args, p.values(map));
		} catch (CommandException err) {
			return Collections.emptyList();
		}
	}

	public abstract static class SubCommand {
		final String name;
		final List<Parameter> parameters = new ArrayList<>();

		public SubCommand(String name) {
			this.name = name;
		}

		public <T extends Parameter> T addParam(T parameter) {
			parameters.add(parameter);
			return parameter;
		}

		public abstract void process(MinecraftServer server, ICommandSender sender, ParamMap parameters);
	}

	public static class ParamMap {
		HashMap<Parameter, Object> values = new HashMap<>();

		public boolean isAssigned(Parameter parameter) {
			return values.containsKey(parameter);
		}

		public <T> T getValue(Parameter<T> parameter) {
			Object o = values.get(parameter);
			if (o == null) throw new IllegalStateException();
			return (T) o;
		}

		public <T> void putValue(Parameter<T> parameter, T value) {
			values.put(parameter, value);
		}
	}

	public abstract static class Parameter<T> {
		public abstract T convert(String string, ICommandSender sender, MinecraftServer server) throws IllegalArgumentException, CommandException;

		public T getMissingValue() throws IllegalArgumentException, CommandException {
			throw new IllegalArgumentException();
		}

		public List<String> values(ParamMap assignedValues) {
			return Collections.emptyList();
		}
	}


	public static class ParameterOptional<T> extends Parameter<Optional<T>> {
		final Parameter<T> parameter;

		public ParameterOptional(Parameter<T> parameter) {
			this.parameter = parameter;
		}

		@Override
		public Optional<T> getMissingValue() throws IllegalArgumentException {
			return Optional.absent();
		}

		@Override
		public Optional<T> convert(String string, ICommandSender sender, MinecraftServer server) throws IllegalArgumentException, CommandException {
			return Optional.fromNullable(parameter.convert(string, sender, server));
		}
	}

	public static class ParameterDefault<T> extends Parameter<T> {
		final Parameter<T> base;
		final String defaultValue;

		public ParameterDefault(Parameter<T> base, String defaultValue) {
			this.base = base;
			this.defaultValue = defaultValue;
		}

		public ParameterDefault(Parameter<T> base, T defaultValue) {
			this.base = base;
			this.defaultValue = defaultValue.toString();
		}

		@Override
		public T convert(String string, ICommandSender sender, MinecraftServer server) throws IllegalArgumentException, CommandException {
			return base.convert(string, sender, server);
		}

		@Override
		public T getMissingValue() throws IllegalArgumentException, CommandException {
			return base.convert(defaultValue, null, null);
		}

		@Override
		public List<String> values(ParamMap assignedValues) {
			List<String> list = base.values(assignedValues);
			if (list.contains(defaultValue)) return list;
			list = new ArrayList<>(list);
			list.add(defaultValue);
			return list;
		}
	}

	public static class ParameterEntitySelect<T extends Entity> extends Parameter<T> {
		final Class<T> clazz;

		public ParameterEntitySelect(Class<T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public T convert(String string, ICommandSender sender, MinecraftServer server) throws IllegalArgumentException, CommandException {
			if ("@t".equalsIgnoreCase(string)) {
				if (sender instanceof EntityPlayer) {
					Vec3d vec3d = ((EntityPlayer) sender).getPositionEyes((float) 0);
					Vec3d vec3d1 = ((EntityPlayer) sender).getLook((float) 0);
					Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * (double) 100, vec3d1.yCoord * (double) 100, vec3d1.zCoord * (double) 100);
					RayTraceResult result = ((EntityPlayer) sender).worldObj.rayTraceBlocks(vec3d, vec3d2, false, false, true);

					if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY && clazz.isAssignableFrom(result.entityHit.getClass())) {
						return (T) result.entityHit;
					}

					throw new EntityNotFoundException();
				}
				throw new IllegalArgumentException("Command user is not a player");
			}

			return getEntity(server, sender, string, clazz);

		}
	}

	public static class ParameterPhenotype extends Parameter<Phenotype> {
		final ParameterClass<EntityAnimal> animalClass;
		final ParameterEntitySelect<EntityAnimal> selector;

		public ParameterPhenotype() {
			this(null, null);
		}

		public ParameterPhenotype(ParameterClass<EntityAnimal> animalClass) {
			this(animalClass, null);
		}

		public ParameterPhenotype(ParameterEntitySelect<EntityAnimal> selector) {
			this(null, selector);
		}

		public ParameterPhenotype(ParameterClass<EntityAnimal> animalClass, ParameterEntitySelect<EntityAnimal> selector) {
			this.animalClass = animalClass;
			this.selector = selector;
		}

		@Override
		public List<String> values(ParamMap assignedValues) {
			Class clazz = null;
			if (animalClass != null && assignedValues.isAssigned(animalClass)) {
				clazz = assignedValues.getValue(animalClass);
			}

			if (selector != null && assignedValues.isAssigned(selector)) {
				clazz = assignedValues.getValue(selector).getClass();
			}

			if (clazz != null) {
				Set<Phenotype> phenotypes = EntityGeneRegistry.getGenePool(clazz).phenotypes;
				return phenotypes.stream().map((phenotype) -> phenotype.name).collect(Collectors.toList());
			}
			return Lists.newArrayList(Phenotype.registry.keySet());
		}

		@Override
		public Phenotype convert(String string, ICommandSender sender, MinecraftServer server) throws IllegalArgumentException {
			Phenotype phenotype = Phenotype.registry.get(string);
			if (phenotype == null) throw new IllegalArgumentException("Unable to find Phenotype");
			return phenotype;
		}
	}

	public static class ParameterClass<V extends Entity> extends Parameter<Class<? extends V>> {
		final Class<V> vClass;

		public ParameterClass(Class<V> vClass) {
			this.vClass = vClass;
		}

		@Override
		public Class<? extends V> convert(String string, ICommandSender sender, MinecraftServer server) throws IllegalArgumentException {
			Class<? extends Entity> aClass = EntityList.NAME_TO_CLASS.get(string);
			if (aClass != null && vClass.isAssignableFrom(aClass))
				return (Class<? extends V>) aClass;

			throw new IllegalArgumentException("Unable to find animal id " + string);
		}

		@Override
		public List<String> values(ParamMap assignedValues) {
			return EntityList.NAME_TO_CLASS.values().stream()
					.filter(vClass::isAssignableFrom)
					.map(EntityList.CLASS_TO_NAME::get)
					.collect(Collectors.toList());
		}
	}

	public static class ParameterBoolean extends Parameter<Boolean> {

		@Override
		public Boolean convert(String string, ICommandSender sender, MinecraftServer server) throws IllegalArgumentException {
			return Boolean.valueOf(string);
		}

		@Override
		public List<String> values(ParamMap assignedValues) {
			return ImmutableList.of("true", "false");
		}
	}

	public static class ParameterInteger extends Parameter<Integer> {
		@Override
		public Integer convert(String string, ICommandSender sender, MinecraftServer server) {
			return Integer.valueOf(string);
		}
	}

	public static class ParameterString extends Parameter<String> {

		@Override
		public String convert(String string, ICommandSender sender, MinecraftServer server) throws IllegalArgumentException, CommandException {
			return string;
		}
	}

	public static class ParameterDouble extends Parameter<Double> {


		@Override
		public Double convert(String string, ICommandSender sender, MinecraftServer server) {
			return Double.valueOf(string);
		}
	}
}
