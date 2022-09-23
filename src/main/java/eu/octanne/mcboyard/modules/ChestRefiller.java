package eu.octanne.mcboyard.modules;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.chestrefiller.ChestRefillerCommand;
import eu.octanne.mcboyard.modules.chestrefiller.LootEditor;
import eu.octanne.mcboyard.modules.chestrefiller.LootableItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChestRefiller extends PlugModule {

	static {
		ConfigurationSerialization.registerClass(LootableItem.class, "LootableItem");
	}

	private File configFile;
	private YamlConfiguration config;

	private List<LootableItem> lootableItems;
	private List<Location> enrollChest;
	private LootEditor lootEditor;
	private int minItemsPerChest, maxItemsPerChest;

	public ChestRefiller(JavaPlugin instance) {
		super(instance);
	}
	
	public void onEnable() {
		lootEditor = new LootEditor();
		Bukkit.getPluginManager().registerEvents(lootEditor, McBoyard.instance);
		var cmd = new ChestRefillerCommand();
		pl.getCommand("chestfiller").setExecutor(cmd);
		pl.getCommand("chestfiller").setTabCompleter(cmd);

		// load config
		configFile = new File(McBoyard.folderPath + "/chest_refiller.yml");
		if(!configFile.exists()) {
			try {
				configFile.getParentFile().mkdirs();
				configFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(configFile);

		// load itemperchest from config
		minItemsPerChest = config.getInt("minItemsPerChest", 3);
		maxItemsPerChest = config.getInt("maxItemsPerChest", 6);
		loadLootableItems();
		loadEnrollChest();
	}
	
	public void onDisable() {
		HandlerList.unregisterAll(lootEditor);
	}

	public void loadLootableItems() {
		// load lootable items from config
		// Message for debug
		lootableItems = new ArrayList<>();
		if(config.contains("lootableItems")) {
			lootableItems = config.getList("lootableItems").stream().map(o -> (LootableItem) o).collect(Collectors.toList());
		}
		pl.getLogger().info("Loaded " + lootableItems.size() + " lootable items");
	}

	public void saveLootableItems() {
		// save lootable items to config
		config.set("lootableItems", lootableItems);
		try {
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pl.getLogger().info("Lootable items saved !");
	}

	public void loadEnrollChest() {
		// load enroll chest from config
		enrollChest = new ArrayList<>();
		if(config.contains("enrollChest")) {
			enrollChest = config.getList("enrollChest").stream().map(o -> (Location) o).collect(Collectors.toList());
		}
		pl.getLogger().info("Enroll chest loaded : " + enrollChest.size());
	}

	public void saveEnrollChest() {
		// save enroll chest to config
		config.set("enrollChest", enrollChest);
		try {
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pl.getLogger().info("Enroll chest saved !");
	}

	public void generateLoots() {
		ArrayList<Location> toRemove = new ArrayList<Location>();
		// generate loots for all enroll chest
		for (Location loc : enrollChest) {
			if (loc.getBlock().getState() instanceof org.bukkit.block.Chest) {
				org.bukkit.block.Chest chest = (org.bukkit.block.Chest) loc.getBlock().getState();
				generateLoot(chest.getInventory());
			}
			// if shulker box
			else if (loc.getBlock().getState() instanceof org.bukkit.block.ShulkerBox) {
				org.bukkit.block.ShulkerBox shulker = (org.bukkit.block.ShulkerBox) loc.getBlock().getState();
				generateLoot(shulker.getInventory());
			}
			// if barrel
			else if (loc.getBlock().getState() instanceof org.bukkit.block.Barrel) {
				org.bukkit.block.Barrel barrel = (org.bukkit.block.Barrel) loc.getBlock().getState();
				generateLoot(barrel.getInventory());
			} else {
				toRemove.add(loc);
				saveEnrollChest();
				// send a message to the console to prevent about delete
				Bukkit.getConsoleSender().sendMessage("§c[§6ChestRefiller§c] §4The chest at " + loc.toString() + " has been deleted because it is not a chest, a shulker box or a barrel.");
			}
		}
		// remove chest from list toRemove
		for (Location loc : toRemove) {
			enrollChest.remove(loc);
		}
	}

	public void generateLoot(Inventory inventory) {
		if (lootableItems.size() == 0) {
			return;
		}
		inventory.clear();
		List<LootableItem> itemsSort = lootableItems.stream().collect(Collectors.toList());
		var nbItems = minItemsPerChest + (int) (Math.random() * (maxItemsPerChest - minItemsPerChest));
		var nbItemsLoot = 0;
		var slotChests = new ArrayList<Integer>();
		for (int i = 0; i < inventory.getSize(); i++) {
			slotChests.add(i);
		}
		while (nbItemsLoot < nbItems) {
			int idxItem = (int) (Math.random() * itemsSort.size());
			if (itemsSort.get(idxItem).getChance() > Math.random() * 100) {
				var slot = slotChests.get((int) (Math.random() * slotChests.size()));
				inventory.setItem(slot, itemsSort.get(idxItem).getLoot());
				slotChests.remove(slot);
				nbItemsLoot++;
			}
		}
	}

	public boolean enrollChest(Location loc) {
		if (!enrollChest.contains(loc)) {
			enrollChest.add(loc);
			saveEnrollChest();
			return true;
		} else return false;
	}

	public boolean unenrollChest(Location loc) {
		if (enrollChest.contains(loc)) {
			enrollChest.remove(loc);
			saveEnrollChest();
			return true;
		} else return false;
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

	public final List<Location> getEnrollChests() {
		return enrollChest;
	}

	public LootEditor getLootEditor() {
		return lootEditor;
	}

	public int getMinItemsPerChest() {
		return minItemsPerChest;
	}

	public void setMinItemsPerChest(int minItemsPerChest) {
		this.minItemsPerChest = minItemsPerChest;
		config.set("minItemsPerChest", minItemsPerChest);
		try {
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getMaxItemsPerChest() {
		return maxItemsPerChest;
	}

	public void setMaxItemsPerChest(int maxItemsPerChest) {
		this.maxItemsPerChest = maxItemsPerChest;
		// save to config
		config.set("maxItemsPerChest", maxItemsPerChest);
		try {
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
