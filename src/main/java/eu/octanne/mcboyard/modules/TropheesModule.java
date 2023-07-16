package eu.octanne.mcboyard.modules;

import eu.octanne.mcboyard.McBoyard;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TropheesModule extends PlugModule implements Listener {
	private Map<Location, String> tropeesBlock;

	public TropheesModule(JavaPlugin instance) {
		super(instance);
	}

	public File getTrophesFile() {
		return new File(pl.getDataFolder(), "trophees.yml");
	}

	@Override
	public void onEnable() {
		// save trophees.yml if not exist
		if (!getTrophesFile().exists()) {
			pl.saveResource("trophees.yml", false);
		}
		// load trophees.yml
		tropeesBlock = new HashMap<>();
		YamlConfiguration config = YamlConfiguration.loadConfiguration(getTrophesFile());
		String worldName = config.getString("world", "world");
		Set<String> tropheesName = config.getConfigurationSection("trophees").getKeys(false);
		for (String tropheeName : tropheesName) {
			try {
				String location = config.getString("trophees." + tropheeName + ".location");
				if (location == null) {
					pl.getLogger().warning("Trophees " + tropheeName + " has no location");
					continue;
				}
				int x = Integer.parseInt(location.split(" ")[0]);
				int y = Integer.parseInt(location.split(" ")[1]);
				int z = Integer.parseInt(location.split(" ")[2]);
				Location loc = new Location(pl.getServer().getWorld(worldName), x, y, z);
				tropeesBlock.put(loc, tropheeName);
			} catch (Exception e) {
				pl.getLogger().warning("Error while loading location of trophee " + tropheeName);
				e.printStackTrace();
			}
		}

		Bukkit.getPluginManager().registerEvents(this, McBoyard.instance);
	}

	@Override
	public void onDisable() {
		// Nothing to do
	}

	public void collectTrophee(Player player, String tropheeName) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(getTrophesFile());
		String advancementName = config.getString("trophees." + tropheeName + ".advancement");
		String namespace = advancementName.split(":")[0];
		String key = advancementName.split(":")[1];
		@SuppressWarnings("deprecation")
		NamespacedKey namespacedkey = new NamespacedKey(namespace, key);
		Advancement advancement = Bukkit.getAdvancement(namespacedkey);

		// give the advancement to the player
		AdvancementProgress advancementProgrss = player.getAdvancementProgress(advancement);
		if (advancementProgrss.isDone()) {
			return;
		}
		pl.getLogger().info("Giving advancement " + tropheeName + " to " + player.getName());
		advancementProgrss.getRemainingCriteria().forEach(advancementProgrss::awardCriteria);
		boolean challengeSound = config.getBoolean("trophees." + tropheeName + ".challenge-sound", false);
		if (challengeSound) {
			player.playSound(player.getLocation(), "minecraft:ui.toast.challenge_complete", 1, 1);
		}
	}

	@EventHandler
	public void onBlockInteract(PlayerInteractEvent event) {
		// check if the player is right clicking a block
		if (event.getClickedBlock() == null)
			return;
		// check if the block is a trophee
		Location loc = event.getClickedBlock().getLocation();
		if (!tropeesBlock.containsKey(loc))
			return;
		collectTrophee(event.getPlayer(), tropeesBlock.get(loc));
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			event.setCancelled(true);
		}
	}
}