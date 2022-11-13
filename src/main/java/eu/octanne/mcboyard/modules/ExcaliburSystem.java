package eu.octanne.mcboyard.modules;

import eu.octanne.mcboyard.entity.ExcaliburStand;
import eu.octanne.mcboyard.modules.excalibur.ExcaliburCommand;
import eu.octanne.mcboyard.modules.excalibur.ExcaliburListener;
import eu.octanne.mcboyard.modules.excalibur.StandKey;
import eu.octanne.mcboyard.modules.excalibur.StandKeyCommand;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumCreatureType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class ExcaliburSystem extends PlugModule {

	private static List<ExcaliburStand> excaliburStands = new ArrayList<>();
	public static int firstUpdate = -1;

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
		StandKey.clearStandKeys();
		firstUpdate = -1;
	}
	
	public void onDisable() {
		StandKey.markAllStandToUpdate();
	}

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