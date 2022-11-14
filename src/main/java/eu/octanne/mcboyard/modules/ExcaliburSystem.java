package eu.octanne.mcboyard.modules;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.entity.ExcaliburStand;
import eu.octanne.mcboyard.entity.standkey.CrochetEntity;
import eu.octanne.mcboyard.entity.standkey.KeyEntity;
import eu.octanne.mcboyard.entity.standkey.MiddleEntity;
import eu.octanne.mcboyard.modules.excalibur.ExcaliburCommand;
import eu.octanne.mcboyard.modules.excalibur.ExcaliburListener;
import eu.octanne.mcboyard.modules.excalibur.StandKey;
import eu.octanne.mcboyard.modules.excalibur.StandKeyCommand;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntitySlime;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ExcaliburSystem extends PlugModule {

	private static List<ExcaliburStand> excaliburStands = new ArrayList<>();

	public static int excaliburUpdate = -1;

	public ExcaliburSystem(JavaPlugin instance) {
		super(instance);
	}

	public void onEnable() {
		ExcaliburCommand excaliburCommand = new ExcaliburCommand();
		pl.getCommand("excalibur").setExecutor(excaliburCommand);
		pl.getCommand("excalibur").setTabCompleter(excaliburCommand);
		StandKeyCommand standKeyCommand = new StandKeyCommand();
		pl.getCommand("standkey").setExecutor(standKeyCommand);
		pl.getCommand("standkey").setTabCompleter(standKeyCommand);
		Bukkit.getPluginManager().registerEvents(new ExcaliburListener(), pl);
		excaliburStands.clear();
		excaliburUpdate = -1;
		StandKey.clearStandKeys();

		// reload StandKey already load in world
		reloadStandKeyAlreadySpawn();
	}

	public UUID getStandIDFromReloadedEntity(net.minecraft.server.v1_16_R3.Entity reloadedEntity) {
		try {
			return (UUID) reloadedEntity.getClass().getMethod("getStandKeyID").invoke(reloadedEntity);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public void despawnPreReloadEntity(net.minecraft.server.v1_16_R3.Entity reloadedEntity) {
		try {
			reloadedEntity.getClass().getMethod("despawn").invoke(reloadedEntity);
			// log remove old entity
			McBoyard.instance.getLogger().log(Level.INFO, "Remove old entity : " + reloadedEntity.getClass().getSimpleName());
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public void reloadStandKeyAlreadySpawn() {
		for (World world : Bukkit.getWorlds()) {
			for (Entity en : world.getEntities()) {
				CraftEntity entity = (CraftEntity) en;
				String entityClassName = entity.getHandle().getClass().getSimpleName();
				Location loc = entity.getLocation();
				if (entity.getType().equals(EntityType.ARMOR_STAND)) {
					if (entityClassName.equals("MiddleEntity")) {
						UUID standID = getStandIDFromReloadedEntity(entity.getHandle());
						McBoyard.instance.getLogger().info("StandKey prepare to reload ! (Attach MiddleEntity) {" + standID + "}");
						StandKey standKey = StandKey.getStandKeyRegenIfNotLoad(standID);
						boolean success = standKey.attachMiddleEntity(new MiddleEntity(((CraftWorld)loc.getWorld()).getHandle(),loc,standKey));
						despawnPreReloadEntity(entity.getHandle());
						if (success) {
							McBoyard.instance.getLogger().info("StandKey reloaded ! (Attach MiddleEntity) {" + standID + "}");
							if (standKey.isComplete()) McBoyard.instance.getLogger().info("StandKey is complete ! (" + standKey.getID() + ") ");
						}
						else {
							McBoyard.instance.getLogger().log(Level.WARNING,
									"An error occured when loading StandKey ! (Attach MiddleEntity) {" + standID + "}");
						}
					} else if (entityClassName.equals("KeyEntity")) {
						UUID standID = getStandIDFromReloadedEntity(entity.getHandle());
						McBoyard.instance.getLogger().info("StandKey prepare to reload ! (Attach KeyEntity) {" + standID + "}");
						StandKey standKey = StandKey.getStandKeyRegenIfNotLoad(standID);
						boolean success = standKey.attachKeyEntity(new KeyEntity(((CraftWorld)loc.getWorld()).getHandle(),loc,standKey));
						despawnPreReloadEntity(entity.getHandle());
						if (success) {
							McBoyard.instance.getLogger().info("StandKey reloaded ! (Attach KeyEntity) {" + standID + "}");
							if (standKey.isComplete()) McBoyard.instance.getLogger().info("StandKey is complete ! (" + standKey.getID() + ") ");
						}
						else {
							McBoyard.instance.getLogger().log(Level.WARNING,
									"An error occured when loading StandKey ! (Attach KeyEntity) {" + standID + "}");
						}
					}
				}
				if (entity.getType().equals(EntityType.SLIME)) {
					if (entityClassName.equals("CrochetEntity")) {
						UUID standID = getStandIDFromReloadedEntity(entity.getHandle());
						McBoyard.instance.getLogger().info("StandKey prepare to reload ! (Attach CrochetEntity) {" + standID + "}");
						StandKey standKey = StandKey.getStandKeyRegenIfNotLoad(standID);
						boolean success = standKey.attachCrochetEntity(new CrochetEntity(((CraftWorld)loc.getWorld()).getHandle(),loc,standKey));
						despawnPreReloadEntity(entity.getHandle());
						if (success) {
							McBoyard.instance.getLogger().info("StandKey reloaded ! (Attach CrochetEntity) {" + standID + "}");
							if (standKey.isComplete()) McBoyard.instance.getLogger().info("StandKey is complete ! (" + standKey.getID() + ") ");
						}
						else {
							McBoyard.instance.getLogger().log(Level.WARNING,
									"An error occured when loading StandKey ! (Attach CrochetEntity) {" + standID + "}");
						}
					}
				}
			}
		}
	}
	
	public void onDisable() {}

	public static List<ExcaliburStand> getExcaliburStands() {
		return excaliburStands;
	}

	public static void addExcaliburStand(ExcaliburStand stand) {
		excaliburStands.add(stand);
	}

	public static void removeExcaliburStand(ExcaliburStand stand) {
		excaliburStands.remove(stand);
	}

	public static boolean hadExcaliburStand(ExcaliburStand stand) {
		return excaliburStands.contains(stand);
	}

	public static void putBackAllExcalibur() {
		for(ExcaliburStand stand : excaliburStands) {
			stand.putBackSword();
		}
	}
}
