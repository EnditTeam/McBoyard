package eu.octanne.mcboyard.modules;

import java.util.ArrayList;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class PlugModule {
	
	JavaPlugin pl;
	
	public static ArrayList<PlugModule> modules = new ArrayList<>();
	
	public PlugModule(JavaPlugin instance) {
		this.pl = instance;
		modules.add(this);
		onEnable();
	}
	
	public void onEnable() {
		
	}
	
	public void onDisable() {
		
	}
}
