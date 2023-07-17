package eu.octanne.mcboyard.modules;

import java.util.ArrayList;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class PlugModule {
	
	public final JavaPlugin pl;
	
	public static ArrayList<PlugModule> modules = new ArrayList<>();
	
	protected PlugModule(JavaPlugin instance) {
		this.pl = instance;
		modules.add(this);
		onEnable();
	}
	
	public abstract void onEnable();

	public abstract void onDisable();
}
