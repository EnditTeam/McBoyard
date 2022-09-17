package eu.octanne.mcboyard.modules;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.chestrefiller.LootEditor;
import eu.octanne.mcboyard.modules.chestrefiller.LootableItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ChestRefiller extends PlugModule {

	private List<LootableItem> lootableItems = new ArrayList<LootableItem>();
	private List<Location> enrollChest = new ArrayList<Location>();
	private LootEditor lootEditor;

	public ChestRefiller(JavaPlugin instance) {
		super(instance);
	}
	
	public void onEnable() {
		lootEditor = new LootEditor();
		Bukkit.getPluginManager().registerEvents(lootEditor, McBoyard.instance);
		loadLootableItems();
		loadEnrollChest();
	}
	
	public void onDisable() {
		HandlerList.unregisterAll(lootEditor);
		saveLootableItems();
		saveEnrollChest();
	}

	public void loadLootableItems() {
		// TODO load lootable items from config
	}

	public void saveLootableItems() {
		// TODO save lootable items to config
	}

	public void loadEnrollChest() {
		// TODO load enroll chest from config
	}

	public void saveEnrollChest() {
		// TODO save enroll chest to config
	}

	public void enrollChest(Location loc) {
		enrollChest.add(loc);
		saveEnrollChest();
	}

	public void unenrollChest(Location loc) {
		enrollChest.remove(loc);
		saveEnrollChest();
	}

	public void addLootableItem(LootableItem lootableItem) {
		lootableItems.add(lootableItem);
		saveLootableItems();
	}

	public void removeLootableItem(LootableItem lootableItem) {
		lootableItems.remove(lootableItem);
		saveLootableItems();
	}

	public final List<LootableItem> getLootableItems() {
		return lootableItems;
	}

	public final List<Location> getEnrollChest() {
		return enrollChest;
	}

	public LootEditor getLootEditor() {
		return lootEditor;
	}

}
