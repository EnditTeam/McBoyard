package eu.octanne.mcboyard.modules;

import java.util.ArrayList;

import org.bukkit.plugin.java.JavaPlugin;

public class Module {
	
	JavaPlugin pl;
	
	public static ArrayList<Module> modules = new ArrayList<>();
	
	public Module(JavaPlugin instance) {
		this.pl = instance;
		modules.add(this);
		onEnable();
	}
	
	public void onEnable() {
		
	}
	
	public void onDisable() {
		
	}
}
