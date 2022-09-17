package eu.octanne.mcboyard;

import java.io.File;
import java.io.IOException;

import eu.octanne.mcboyard.modules.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class McBoyard extends JavaPlugin {
	
	static public Plugin instance;
	
	//FOLDER PATH
	static public String folderPath = "plugins/McBoyard";
	static public File fileConfig = new File(folderPath+"/config.yml");
	static public YamlConfiguration config;
	
	/*
	 * MODULES
	 */
	public static CounterPiece counterPieceModule;
	public static Chrono chronoModule;
	public static KitSystem kitModule;
	public static NoChangeWeather noChangeWeatherModule;
	public static AutoMessage autoMessageModule;
	public static Maitre maitreModule;
	public static BoyardRoom boyardRoomModule;
	public static StaffChat staffChatModule;
	public static MusicModule musicModule;
	public static CreditModule creditModule;

	public static ChestRefiller chestFillerModule;
	
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
		instanceModules();
	}
	
	@Override
	public void onDisable() {
		unloadModules();
	}
	
	public void instanceModules() {
		counterPieceModule = new CounterPiece(this);
		chronoModule = new Chrono(this);
		kitModule = new KitSystem(this);
		noChangeWeatherModule = new NoChangeWeather(this);
		autoMessageModule = new AutoMessage(this);
		maitreModule = new Maitre(this);
		staffChatModule = new StaffChat(this);
		musicModule = new MusicModule(this);
		creditModule = new CreditModule(this);
		boyardRoomModule = new BoyardRoom(this);
		chestFillerModule = new ChestRefiller(this);
	}
	
	/*
	 * MODULES
	 * LOAD / UNLOAD
	 */
	public void loadModules() {
		for (PlugModule mod : PlugModule.modules) {
			mod.onEnable();
		}
	}
	public void unloadModules() {
		for (PlugModule mod : PlugModule.modules) {
			mod.onDisable();
		}
	}
	
}
