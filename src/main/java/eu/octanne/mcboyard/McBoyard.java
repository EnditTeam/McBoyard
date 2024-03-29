package eu.octanne.mcboyard;

import java.io.File;
import java.io.IOException;

import eu.octanne.mcboyard.entity.CustomEntity;
import eu.octanne.mcboyard.modules.*;
import eu.octanne.mcboyard.modules.coffrefort.CoffreFortModule;
import eu.octanne.mcboyard.modules.grandeporte.GrandePorteModule;
import eu.octanne.mcboyard.modules.maitika.MaitikaModule;
import eu.octanne.mcboyard.modules.telephone.TelephoneModule;
import eu.octanne.mcboyard.modules.morse.MorseModule;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class McBoyard extends JavaPlugin {
	
	public static Plugin instance;
	
	//FOLDER PATH
	public static String folderPath = "plugins/McBoyard";
	public static File fileConfig = new File(folderPath+"/config.yml");
	public static YamlConfiguration config;
	public static World world;
	
	/*
	 * MODULES
	 */
	public static CounterPiece counterPieceModule;
	public static Chrono chronoModule;
	public static ClapModule clapModule;
	public static KitSystem kitModule;
	public static NoChangeWeather noChangeWeatherModule;
	public static AutoMessage autoMessageModule;
	public static Maitre maitreModule;
	public static BoyardRoom boyardRoomModule;
	public static StaffChat staffChatModule;
	public static MusicModule musicModule;
	public static CreditModule creditModule;
	public static TropheesModule tropheesModule;
	public static McBoyardModule mcboyardModule;
	public static ChestRefiller chestFillerModule;
	public static ExcaliburSystem excaliburModule;
	public static CoffreFortModule coffreFortModule;
	public static MaitikaModule maitikaModule;
	public static ElytraParkourModule elytraParkourModule;
    public static TelephoneModule telephoneModule;
    public static MorseModule morseModule;
    public static GrandePorteModule grandePorteModule;

    @Override
    public void onEnable() {
        instance = Bukkit.getPluginManager().getPlugin("McBoyard");
        Bukkit.getPluginManager().registerEvents(new CustomEntity(), this);
        preWorldModules();

        new BukkitRunnable() {
            @Override
			public void run() {
				config = YamlConfiguration.loadConfiguration(fileConfig);
				if(!fileConfig.exists()) {
					try {
						fileConfig.getParentFile().mkdirs();
						fileConfig.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				postWorldModules();
			}
        }.runTaskLater(this, 1);
    }

    @Override
    public void onDisable() {
        unloadModules();
    }

    public void postWorldModules() {
        counterPieceModule = new CounterPiece(this);
        chronoModule = new Chrono(this);
        clapModule = new ClapModule(this);
		kitModule = new KitSystem(this);
		noChangeWeatherModule = new NoChangeWeather(this);
		autoMessageModule = new AutoMessage(this);
		maitreModule = new Maitre(this);
		staffChatModule = new StaffChat(this);
		musicModule = new MusicModule(this);
		creditModule = new CreditModule(this);
		boyardRoomModule = new BoyardRoom(this);
		chestFillerModule = new ChestRefiller(this);
		tropheesModule = new TropheesModule(this);
		mcboyardModule = new McBoyardModule(this);
		coffreFortModule = new CoffreFortModule(this);
		maitikaModule = new MaitikaModule(this);
		elytraParkourModule = new ElytraParkourModule(this);
        telephoneModule = new TelephoneModule(this);
        morseModule = new MorseModule(this);
        grandePorteModule = new GrandePorteModule(this);
    }

    public void preWorldModules() {
        excaliburModule = new ExcaliburSystem(this);
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

    public static World getWorld() {
		if (world != null)
			return world;
		world = Bukkit.getWorld("FB");
		if (world == null)
			world = Bukkit.getWorld("world");
		if (world == null)
			world = Bukkit.getWorlds().get(0);
		if (world == null)
			throw new NullPointerException("No world loaded");
		instance.getLogger().info("Found world : " + world.getName());
		return world;
	}
}
