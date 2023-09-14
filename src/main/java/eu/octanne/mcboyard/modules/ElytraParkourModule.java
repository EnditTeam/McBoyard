package eu.octanne.mcboyard.modules;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.elytraparkour.ElytraParkourCommand;
import eu.octanne.mcboyard.modules.elytraparkour.ElytraRing;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;

public class ElytraParkourModule extends PlugModule implements Listener {

    static {
        ConfigurationSerialization.registerClass(ElytraRing.class, "ElytraRing");
    }

    // Config variables saved in config file
    public static double distanceFromRing = 0.5;
    public static double distanceFromRingY = 0.75;
    public static double distanceFromStart = 4.0;
    public static Location startLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
    public static int defaultDuration = 3;
    public static ArrayList<ElytraRing> ringsLocation = new ArrayList<>();

    // Temporary variable
    public static ArrayList<Player> playersInParkour = new ArrayList<>();

    public ElytraParkourModule(JavaPlugin instance) {
        super(instance);
    }

    public void saveElytraRings() {
        File configFile = new File(McBoyard.folderPath + "/elytra_rings.yml");
        if(!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        // Save Elytra Rings to config
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.set("rings", ringsLocation);
        config.set("defaultDuration", defaultDuration);
        config.set("distanceFromRing", distanceFromRing);
        config.set("distanceFromRingY", distanceFromRingY);
        config.set("distanceFromStart", distanceFromStart);
        config.set("startLocation", startLocation);
        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadElytraRings() {
        File configFile = new File(McBoyard.folderPath + "/elytra_rings.yml");
        if(configFile.exists()) {
            // Load Elytra Rings from config
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ringsLocation = (ArrayList<ElytraRing>) config.get("rings", new ArrayList<ElytraRing>());
            // load default duration
            defaultDuration = config.getInt("defaultDuration", 3);
            // load distance from ring
            distanceFromRing = config.getDouble("distanceFromRing", 0.5);
            // load distance from ring Y
            distanceFromRingY = config.getDouble("distanceFromRingY", 0.75);
            // load distance from start
            distanceFromStart = config.getDouble("distanceFromStart", 4.0);
            // load start location
            startLocation = config.getLocation("startLocation", Bukkit.getWorlds().get(0).getSpawnLocation());
        }
    }

    @Override
    public void onEnable() {
        // Load Elytra Rings from config
        loadElytraRings();

        ElytraParkourCommand cmd = new ElytraParkourCommand();
        pl.getCommand("elytraparkour").setExecutor(cmd);
        pl.getCommand("elytraparkour").setTabCompleter(cmd);
        Bukkit.getPluginManager().registerEvents(this, McBoyard.instance);
    }

    @Override
    public void onDisable() {
        // Save Elytra Rings to config
        saveElytraRings();
    }

    private double distanceWithoutY(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc1.getX()-loc2.getX(), 2) + Math.pow(loc1.getZ()-loc2.getZ(), 2));
    }

    private double distanceBetweenY(Location loc1, Location loc2) {
        return Math.abs(loc1.getY()-loc2.getY());
    }

    public static void resetPlayer(Player player, CommandSender commandSender) {
        // add elytra with default duration to player
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        Damageable meta = (Damageable) elytra.getItemMeta();
        meta.setDamage(432-ElytraParkourModule.defaultDuration);
        elytra.setItemMeta((ItemMeta) meta);
        player.getInventory().setChestplate(elytra);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage("§aVotre elytra a été réparé, vous pouvez recommencer le parcours.");
        if(commandSender != null) commandSender.sendMessage("§cLe joueur " + player.getName() + " a été reinisialisé.");

        // Check if we need to add a boat
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().contains("BOAT")) {
                return;
            }
        }

        ItemStack boat = new ItemStack(Material.ACACIA_BOAT);
        player.getInventory().addItem(boat);
    }

    @EventHandler
    public void onEnterRing(PlayerMoveEvent e) {
        if (playersInParkour.contains(e.getPlayer())) {
            // check if is near the start location
            if (e.getTo().distance(startLocation) <= distanceFromStart && e.getFrom().distance(startLocation) > distanceFromStart) {
                // reset player
                resetPlayer(e.getPlayer(), null);
            }

            // check if player has elytra for rings detection
            if (e.getPlayer().getInventory()
                    .getChestplate() != null && e.getPlayer().getInventory().getChestplate().getType().name().contains("ELYTRA")) {
                for (ElytraRing ring : ringsLocation) {
                    if (distanceWithoutY(e.getTo(),ring.getLocation()) <= distanceFromRing && distanceBetweenY(e.getTo(), ring.getLocation()) <= distanceFromRingY
                            && distanceWithoutY(e.getFrom(), ring.getLocation()) > distanceFromRing) {
                        // give back elytra durability
                        Damageable itemMeta = (Damageable) e.getPlayer().getInventory().getChestplate().getItemMeta();
                        // set durability to ring.durability
                        itemMeta.setDamage(432-ring.getNbDuraGiveback());
                        e.getPlayer().getInventory().getChestplate().setItemMeta((org.bukkit.inventory.meta.ItemMeta) itemMeta);
                        // play a sound to the player to notify him
                        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        e.getPlayer().sendMessage("§aVous avez récupéré §6" + ring.getNbDuraGiveback() + " §adurabilité à votre elytra !");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLeavingServer(PlayerQuitEvent e) {
        playersInParkour.remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerKickFromServer(PlayerKickEvent e) {
        playersInParkour.remove(e.getPlayer());
    }
}
