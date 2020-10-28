package eu.octanne.mcboyard;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import eu.octanne.mcboyard.entity.EntityCustom;
import eu.octanne.mcboyard.modules.AutoMessage;
import eu.octanne.mcboyard.modules.BoyardRoom;
import eu.octanne.mcboyard.modules.Chairs;
import eu.octanne.mcboyard.modules.Chrono;
import eu.octanne.mcboyard.modules.CounterPiece;
import eu.octanne.mcboyard.modules.CreditModule;
import eu.octanne.mcboyard.modules.KitSystem;
import eu.octanne.mcboyard.modules.Maitre;
import eu.octanne.mcboyard.modules.Module;
import eu.octanne.mcboyard.modules.MusicModule;
import eu.octanne.mcboyard.modules.NoChangeWeather;
import eu.octanne.mcboyard.modules.StaffChat;
import eu.octanne.mcboyard.modules.TyrolienneModule;

public class McBoyard extends JavaPlugin{
	
	static public Plugin instance;
	
	static public EntityCustom entityTyro = EntityCustom.TYRO_TAIL;
	static public EntityCustom entityTyroHitch = EntityCustom.TYRO_HITCH;
	
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
	static public StaffChat staffChatModule;
	static public MusicModule musicModule;
	static public CreditModule creditModule;
	static public TyrolienneModule tyroModule;
	
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
		chairsModule = new Chairs(this);
		boyardRoomModule = new BoyardRoom(this);
		tyroModule = new TyrolienneModule(this);
	}
	
	/*
	 * MODULES
	 * LOAD / UNLOAD
	 */
	public void loadModules() {
		for (Module mod : Module.modules) {
			mod.onEnable();
		}
	}
	public void unloadModules() {
		for (Module mod : Module.modules) {
			mod.onDisable();
		}
	}
	
}
