package eu.octanne.mcboyard.modules;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import eu.octanne.mcboyard.McBoyard;

public class NoChangeWeather extends Module implements Listener {
	
	
	public NoChangeWeather(JavaPlugin instance) {
		super(instance);
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		e.setCancelled(true);
	}
	
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, McBoyard.instance);
	}
	
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}
}
