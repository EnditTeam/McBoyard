package eu.octanne.mcboyard.modules;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import eu.octanne.mcboyard.Utils;
import eu.octanne.mcboyard.entity.TyroEntity;

public class TyrolienneModule implements Listener {
	
	static ItemStack wandItem = Utils.createItemStack("§6Tyrolienne Wrench", Material.STICK, 1, null, 0, true, false); 
	
	JavaPlugin pl;
	
	public TyrolienneModule(JavaPlugin pl) {
		this.pl = pl;
		onEnable();
	}
	
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, pl);
		pl.getCommand("tyro").setExecutor(new TyrolienneCommand());
	}
	
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}
	
	/*static private class Tyrolienne {
		
		static private ArrayList<Tyrolienne> loadedTyros = new ArrayList<Tyrolienne>();
		
		
	}*/
	
	private class TyrolienneCommand implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(sender.hasPermission("tyrolienne.wrench") && sender instanceof Player) {
				if(args.length > 0) {
					if(args[0].equalsIgnoreCase("wrench")) {
						((Player) sender).getInventory().addItem(wandItem);
						return true;
					}
					if(args[0].equalsIgnoreCase("help")) {
						
					}
					
					// TODO
					return false;
				}else {
					sender.sendMessage("§9Tyro §8|§c Usage : §e/tyro <subcmd> [args] §c- §b/tyro help §cpour l'aide.");
					return false;
				}
			}else {
				sender.sendMessage("Vous n'avez pas la permission pour ça.");
				return false;
			}
		}
		
	}
	
	@EventHandler
	public void onUseWand(PlayerInteractEvent e) {
		if(e.getItem() != null && e.getItem().equals(wandItem)) {
			String matName = e.getClickedBlock() == null ? "" : e.getClickedBlock().getType().name();
			if(e.getClickedBlock() != null && matName.contains("FENCE") && !matName.contains("FENCE_GATE")) {
				// Ajouter un point
				if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					if(TyroTemp.isOnCreation(e.getPlayer())) {
						// TODO ADD POINT
						TyroTemp.getPCreation(e.getPlayer()).addPoint(e.getClickedBlock());
					}else {
						// TODO ADD first POINT
						TyroTemp.startNewCreation(e.getPlayer(), e.getClickedBlock());
					}
				}
				// Arreter Construction
				if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
					if(TyroTemp.isOnCreation(e.getPlayer())) {
						// VALIDATE CREATION
						TyroTemp.getPCreation(e.getPlayer()).validateCreation(e.getClickedBlock());
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onUnleash(EntityUnleashEvent e) {
		if(e.getEntity() instanceof TyroEntity) {
			Bukkit.broadcastMessage("Reason :" + e.getReason().name());
		}
	}
	
	@EventHandler
	public void onCreatorMove(PlayerMoveEvent e) {
		if(TyroTemp.isOnCreation(e.getPlayer())) {
			TyroTemp tt = TyroTemp.getPCreation(e.getPlayer());
			if(tt.getLastTyroEntity().getBukkitEntity().getLocation().distance(e.getPlayer().getLocation()) >= 0.2) {
				tt.getLastTyroEntity().getBukkitEntity().teleport(e.getPlayer());
				
			}
		}
	}
	
	static class TyroTemp {
		
		private static ArrayList<TyroTemp> instances = new ArrayList<>();
		
		private ArrayList<TyroEntity> tyroEntities = new ArrayList<>();
		private ArrayList<Location> fenceBlock = new ArrayList<>();
		private ArrayList<LeashHitch> leashHitch = new ArrayList<>();
		
		private Player creator;
		
		
		public TyroTemp(Player p, Block fceBlockStrt) {
			instances.add(this);
			
			creator = p;
			
			// CREATE FIRST HITCH
			fenceBlock.add(fceBlockStrt.getLocation());
			LeashHitch leashE = (LeashHitch) p.getWorld().spawnEntity(fceBlockStrt.getLocation(), EntityType.LEASH_HITCH);
			leashHitch.add(leashE);
			
			// CREATE FIRST ARMOR
			TyroEntity tyroEn = new TyroEntity(p.getWorld());
			eu.octanne.mcboyard.entity.EntityType.spawnEntity(tyroEn, p.getLocation());
			tyroEn.leashedTo(leashE);
			tyroEntities.add(tyroEn);
		}
		
		public TyroEntity getLastTyroEntity() {
			return tyroEntities.get(tyroEntities.size()-1);
		}
		
		public boolean addPoint(Block b) {
			// CREATE NEW HITCH
			fenceBlock.add(b.getLocation());
			LeashHitch leashE = (LeashHitch) b.getWorld().spawnEntity(b.getLocation(), EntityType.LEASH_HITCH);
			leashHitch.add(leashE);
			
			// CREATE NEW ARMOR
			TyroEntity tyroEn = new TyroEntity(creator.getWorld());
			eu.octanne.mcboyard.entity.EntityType.spawnEntity(tyroEn, creator.getLocation());
			tyroEn.leashedTo(leashE);
			tyroEntities.add(tyroEn);
			
			// TP OLD ARMOR ON NEW HITCH
			Location loc = leashE.getLocation().clone(); loc.setY(loc.getY()-0.1);
			tyroEntities.get(tyroEntities.size()-2).getBukkitEntity().teleport(loc);
			
			return true;
		}
		
		public boolean validateCreation(Block b) {
			// TODO
			
			// CREATE LAST HITCH
			fenceBlock.add(b.getLocation());
			LeashHitch leashE = (LeashHitch) b.getWorld().spawnEntity(b.getLocation(), EntityType.LEASH_HITCH);
			leashHitch.add(leashE);
			
			// TP LAST ARMOR ON LAST HITCH
			Location loc = leashE.getLocation().clone(); loc.setY(loc.getY()-0.1);
			tyroEntities.get(tyroEntities.size()-1).getBukkitEntity().teleport(loc);
			creator = null;
			
			return true;
		}
		
		public static TyroTemp startNewCreation(Player p, Block fceBlockStrt) {
			return new TyroTemp(p, fceBlockStrt);
		}
		
		public static boolean isOnCreation(Player p) {
			for	(TyroTemp tt : instances) {
				if (tt.creator != null && tt.creator.equals(p)) return true;
			}
			return false;
		}
		
		public static TyroTemp getPCreation(Player p) {
			for	(TyroTemp tt : instances) {
				if (tt.creator != null && tt.creator.equals(p)) return tt;
			}
			return null;
		}
	}
}
