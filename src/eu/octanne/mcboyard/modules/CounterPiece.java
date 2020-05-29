package eu.octanne.mcboyard.modules;

import java.io.IOException;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;

import eu.octanne.mcboyard.McBoyard;

public class CounterPiece implements Listener {

	/*
	 * INIT VAR
	 */
	protected int task, seconds = 65, piece = 0, secondsInit = 65;
	protected boolean isEnable = false;
	protected ArmorStand holoCounter;
	protected ArmorStand holoTitle;
	protected Location locTitle;
	protected Location locCounter;
	
	/*
	 * WORLDGUARD AND REGIOMANAGER
	 */
	public WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	public RegionManager getRegionManager() {
		return getWorldGuard().getRegionManager(Bukkit.getWorld("world"));
	}
	
	/*
	 * CONTRUCTOR
	 */
	public CounterPiece() {
		onEnable();
		Bukkit.getPluginManager().registerEvents(this, McBoyard.instance);
	}
	
	public void onEnable() {
		//CREATE DEFAULT CONFIG
		if(!McBoyard.config.isSet("CounterPiece.counterLocation"))McBoyard.config.set("CounterPiece.counterLocation", new Location(Bukkit.getWorlds().get(0), 0, 64, 0));
		if(!McBoyard.config.isSet("CounterPiece.cooldown"))McBoyard.config.set("CounterPiece.cooldown", 65);
		try {
			McBoyard.config.save(McBoyard.fileConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//LOAD VAR
		locTitle = (Location) McBoyard.config.get("CounterPiece.counterLocation");
		locCounter = locTitle.clone();
		secondsInit = seconds = McBoyard.config.getInt("CounterPiece.cooldown");
		/*
		 * ARMORSTAND
		 */
		//TITLE
		holoTitle = (ArmorStand) locTitle.getWorld().spawnEntity(locTitle, EntityType.ARMOR_STAND);
		holoTitle.setCustomNameVisible(true);
		holoTitle.setCustomName(ChatColor.GOLD+"Compteur de Boyards");
		holoTitle.setGravity(false);
		holoTitle.setInvulnerable(true);
		holoTitle.setVisible(false);
		holoTitle.setBasePlate(false);
		holoTitle.setSmall(true);
		//COUNTER
		locCounter.setY(locTitle.getY()-0.22);
		holoCounter = (ArmorStand) locCounter.getWorld().spawnEntity(locCounter, EntityType.ARMOR_STAND);
		holoCounter.setCustomNameVisible(true);
		holoCounter.setCustomName("00000");
		holoCounter.setGravity(false);
		holoCounter.setInvulnerable(true);
		holoCounter.setVisible(false);
		holoCounter.setBasePlate(false);
		holoCounter.setSmall(true);
	}
	public void onDisable() {
		holoCounter.remove();
		holoTitle.remove();
		holoTitle.setHealth(0);
		holoCounter.setHealth(0);
	}
	
	public void changeHoloCooldown(String string) {
		holoCounter.setCustomName(string);
	}
	
	/*
	 * EVENT LISTENER
	 */
	@EventHandler
	public void onHoloDeath(EntityDeathEvent e) {
		if(e.getEntity().equals(holoCounter) || e.getEntity().equals(holoTitle)) {
			/*
			 * ARMORSTAND
			 */
			//TITLE
			holoTitle = (ArmorStand) locTitle.getWorld().spawnEntity(locTitle, EntityType.ARMOR_STAND);
			holoTitle.setCustomNameVisible(true);
			holoTitle.setCustomName(ChatColor.GOLD+"Compteur de Boyards");
			holoTitle.setGravity(false);
			holoTitle.setInvulnerable(true);
			holoTitle.setVisible(false);
			holoTitle.setBasePlate(false);
			holoTitle.setSmall(true);
			//COUNTER
			locCounter.setY(locTitle.getY()-0.22);
			holoCounter = (ArmorStand) locCounter.getWorld().spawnEntity(locCounter, EntityType.ARMOR_STAND);
			holoCounter.setCustomNameVisible(true);
			holoCounter.setCustomName("00000");
			holoCounter.setGravity(false);
			holoCounter.setInvulnerable(true);
			holoCounter.setVisible(false);
			holoCounter.setBasePlate(false);
			holoCounter.setSmall(true);
		}
	}
	
	@EventHandler
	public void onPlayerPickupBoyard(EntityPickupItemEvent e) {
		if(e.getItem().getItemStack().getType().equals(Material.GOLD_NUGGET) && isEnable) {
			Location locDown = e.getItem().getLocation().clone();
			locDown.setY(locDown.getY()-1);

			if(locDown.getBlock().getType().equals(Material.HOPPER) || e.getItem().getLocation().getBlock().getType().equals(Material.HOPPER)) { //getRegionManager().getRegion("compteur-boyards").contains(e.getItem().getLocation().getBlockX(), e.getItem().getLocation().getBlockY(), e.getItem().getLocation().getBlockZ())
				if(locDown.getBlock().getType().equals(Material.HOPPER) && ((Hopper) locDown.getBlock().getState()).getInventory().getName().equals("Compteur Boyard")) {
					e.setCancelled(true);
				}else if(e.getItem().getLocation().getBlock().getType().equals(Material.HOPPER) && ((Hopper) e.getItem().getLocation().getBlock().getState()).getInventory().getName().equals("Compteur Boyard")) {
					e.setCancelled(true);
				}else return;
			}else return;
		}else return;
	}
	
	@EventHandler
	public void onDropperPickupBoyard(InventoryPickupItemEvent e) {
		if(e.getInventory().getName().equals("Compteur Boyard")) {
			e.setCancelled(true);
			if(e.getItem().getItemStack().getType().equals(Material.GOLD_NUGGET) && piece == 0) {
				if(isEnable == false) {
					isEnable = true;
					piece = new Random().nextInt((35142 - 10451) + 1) + 10451;
					changeHoloCooldown(ChatColor.GRAY+"§k00000");
					task = Bukkit.getScheduler().scheduleSyncRepeatingTask(McBoyard.instance, new Runnable() {

						@Override
						public void run() {
							if(seconds <= 0) {
								if(piece < 10) {
									changeHoloCooldown(ChatColor.GRAY+""+"0000"+piece);
								}
								if(piece < 100) {
									changeHoloCooldown(ChatColor.GRAY+""+"000"+piece);
								}
								if(piece < 1000) {
									changeHoloCooldown(ChatColor.GRAY+""+"00"+piece);
								}
								if(piece < 10000) {
									changeHoloCooldown(ChatColor.GRAY+""+"0"+piece);
								}else {
									changeHoloCooldown(ChatColor.GRAY+""+piece);
								}
								Bukkit.broadcastMessage(ChatColor.GOLD+"Vous venez de remporter la somme de "+ChatColor.GRAY+""+piece+" §6Boyards");
								isEnable = false;
								seconds = secondsInit;
								for(Entity en : locTitle.getWorld().getEntities()) {
									if(en.getType().equals(EntityType.DROPPED_ITEM)){
										Item item = (Item) en;
										if(item.getItemStack().getType().equals(Material.GOLD_NUGGET)) {
											en.remove();
										}
									}
								}
								Bukkit.getScheduler().cancelTask(task);
							}else {
								seconds--;
							}
						}
					}, 0, 20);
				}
			}
		}
	}
	
	/*
	 * COMMAND RSTBOYARD
	 */
	static public class ResetBoyardCommand implements CommandExecutor{

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(sender.hasPermission("boyard.reset")) {
				McBoyard.counterPieceModule.piece = 0;
				McBoyard.counterPieceModule.seconds = McBoyard.counterPieceModule.secondsInit;
				McBoyard.counterPieceModule.isEnable = false;
				McBoyard.counterPieceModule.changeHoloCooldown("00000");
				Bukkit.getScheduler().cancelTask(McBoyard.counterPieceModule.task);
				for(Entity en : McBoyard.counterPieceModule.locTitle.getWorld().getEntities()) {
					if(en.getType().equals(EntityType.DROPPED_ITEM)){
						Item item = (Item) en;
						if(item.getItemStack().getType().equals(Material.GOLD_NUGGET)) {
							en.remove();
						}
					}
				}
				sender.sendMessage(ChatColor.GOLD+"Reset du compteur de boyard.");
			}
			return false;
		}
	}
	
}
