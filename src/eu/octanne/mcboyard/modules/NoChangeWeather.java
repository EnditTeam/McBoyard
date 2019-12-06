package eu.octanne.mcboyard.modules;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

import eu.octanne.mcboyard.McBoyard;

public class NoChangeWeather implements Listener {
	
	public NoChangeWeather() {
		onEnable();
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		e.setCancelled(true);
	}
	
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, McBoyard.instance);
	}
	
	public void onDisable() {
		
	}
}
