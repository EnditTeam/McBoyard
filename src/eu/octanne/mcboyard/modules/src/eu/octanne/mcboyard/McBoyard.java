package eu.octanne.mcboyard;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import eu.octanne.mcboyard.modules.AutoMessage;
import eu.octanne.mcboyard.modules.BoyardRoom;
import eu.octanne.mcboyard.modules.Chairs;
import eu.octanne.mcboyard.modules.Chrono;
import eu.octanne.mcboyard.modules.CounterPiece;
import eu.octanne.mcboyard.modules.KitSystem;
import eu.octanne.mcboyard.modules.Maitre;
import eu.octanne.mcboyard.modules.NoChangeWeather;

public class McBoyard extends JavaPlugin{
	
	static public Plugin instance;
	
	//FOLDER PATH
	static public String folderPath = "plugins/McBoyard";
	static public File fileConfig = new File(folderPath+"/config.yml");
	static public YamlConfiguration config;
	
	/*
	 * MODULES
	 */
	static public CounterPiece counterPieceModule;
	static public Chrono chronoModule;
	static public KitSystem kitModule;
	static public NoChangeWeather noChangeWeatherModule;
	static public AutoMessage autoMessageModule;
	static public Maitre maitreModule;
	static public Chairs chairsModule;
	static public BoyardRoom boyardRoomModule;
	
	@Override
	public void onEnable() {
		instance = Bukkit.getPluginManager().getPlugin("McBoyard");
		config = YamlConfiguration.loadConfiguration(fileConfig);
		if(!fileConfig.exists()) {
			try {
				fileConfig.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		loadModules();
	}
	
	@Override
	public void onDisable() {
		unloadModules();
	}
	
	/*
	 * MODULES
	 * LOAD / UNLOAD
	 */
	public void loadModules() {
		/*
		 * Counter Piece Module
		 */
		counterPieceModule = new CounterPiece();
		getCommand("resetcounter").setExecutor(new CounterPiece.ResetBoyardCommand());
		/*
		 * Chrono Module
		 */
		chronoModule = new Chrono();
		getCommand("chrono").setExecutor(new Chrono.ChronoCommand());
		getCommand("pchrono").setExecutor(new Chrono.ChronoPersoCommand());
		getCommand("chronostop").setExecutor(new Chrono.ChronoStopCommand());
		/*
		 * Kits Module
		 */
		kitModule = new KitSystem();
		getCommand("kitreload").setExecutor(new KitSystem.KitReloadCommand());
		getCommand("kitcreate").setExecutor(new KitSystem.KitCreateCommand());
		getCommand("kit").setExecutor(new KitSystem.KitCommand());
		/*
		 * No Change Weather Module
		 */
		noChangeWeatherModule = new NoChangeWeather();
		/*
		 * Auto Message Module
		 */
		autoMessageModule = new AutoMessage();
		getCommand("amsgreload").setExecutor(new AutoMessage.ReloadCommand());
		/*
		 * Maitre Module
		 */
		maitreModule = new Maitre();
		getCommand("maitre").setExecutor(new Maitre.MaitreCommand());
		/*
		 * Chairs Module
		 */
		chairsModule = new Chairs();
		/*
		 * BoyardRoom Module
		 */
		boyardRoomModule = new BoyardRoom();
		getCommand("boyardpassword").setExecutor(new BoyardRoom.BoyardPasswordConfigCommand());
	}
	public void unloadModules() {
		counterPieceModule.onDisable();
		chronoModule.onDisable();
		kitModule.onDisable();
		maitreModule.onDisable();
		chairsModule.onDisable();
		boyardRoomModule.onDisable();
	}
	
}
