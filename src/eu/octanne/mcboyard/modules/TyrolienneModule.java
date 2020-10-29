package eu.octanne.mcboyard.modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.Utils;
import eu.octanne.mcboyard.entity.EntityCustom;
import eu.octanne.mcboyard.entity.TyroEntity;
import eu.octanne.mcboyard.entity.TyroHitchEntity;
import net.minecraft.server.v1_12_R1.Entity;

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

	static class Tyrolienne {

		static private ArrayList<Tyrolienne> loadedInstances = new ArrayList<Tyrolienne>();

		private UUID id;

		private ArrayList<TyroHitchEntity> hitchEntities;
		private ArrayList<TyroEntity> tailEntities;

		private Tyrolienne(TyroTemp temp) {
			this.hitchEntities = temp.leashHitch;
			this.tailEntities = temp.tyroEntities;
			this.id = temp.id;
		}

		private Tyrolienne(UUID id, ArrayList<Location> hitchEntities, ArrayList<Location> tailEntities) {
			this.id = id;

			for(int i = 0; i < hitchEntities.size(); i++) {
				// CREATE HITCH
				TyroHitchEntity leashE = new TyroHitchEntity(hitchEntities.get(i).getWorld());
				this.hitchEntities.add(leashE);
				EntityCustom.spawnEntity(leashE, hitchEntities.get(i));

				// CREATE TAIL
				TyroEntity tyroEn = new TyroEntity(tailEntities.get(i).getWorld());
				this.tailEntities.add(tyroEn);
				EntityCustom.spawnEntity(tyroEn, tailEntities.get(i));
				tyroEn.leashedTo(leashE);

				TyroTemp.fenceBlock.add(hitchEntities.get(i).getBlock().getLocation());
			}
		}

		public static void createTyro(TyroTemp temp) {
			loadedInstances.add(new Tyrolienne(temp));
		}

		@SuppressWarnings("unchecked")
		public static void loadTyros() {
			for(File file : new File(McBoyard.folderPath+"/tyros").listFiles()) {
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				loadedInstances.add(new Tyrolienne(UUID.fromString(file.getName().split(".")[0]), 
						(ArrayList<Location>)config.get("Hitch"), (ArrayList<Location>)config.get("Tail")));
			}
		}

		public UUID getID() {
			return id;
		}

		public void useTyro(Player p) {
			// TODO MAKE USABLE THE TYRO

		}

	}

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
					// TODO FINISH COMMAND
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
						// ADD POINT
						TyroTemp.getPCreation(e.getPlayer()).addPoint(e.getClickedBlock());
					}else {
						// ADD first POINT
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
	public void onClick(PlayerInteractAtEntityEvent e) {
		if(((CraftEntity) e.getRightClicked()).getHandle() instanceof TyroHitchEntity) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onUnleash(EntityUnleashEvent e) {
		if(e.getEntity() instanceof TyroEntity) {
			//Bukkit.broadcastMessage("Reason :" + e.getReason().name());
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
		private static ArrayList<Location> fenceBlock = new ArrayList<>();


		private ArrayList<TyroEntity> tyroEntities = new ArrayList<>();
		private ArrayList<TyroHitchEntity> leashHitch = new ArrayList<>();

		private UUID id;

		private Player creator;

		public TyroTemp(Player p, Block fceBlockStrt) {
			instances.add(this);

			creator = p;
			id = UUID.randomUUID();

			fenceBlock.add(fceBlockStrt.getLocation());

			// CREATE FIRST HITCH
			TyroHitchEntity leashE = new TyroHitchEntity(creator.getWorld());
			EntityCustom.spawnEntity(leashE, fceBlockStrt.getLocation());
			leashHitch.add(leashE);

			// CREATE FIRST TAIL
			TyroEntity tyroEn = new TyroEntity(p.getWorld());
			EntityCustom.spawnEntity(tyroEn, p.getLocation());
			tyroEntities.add(tyroEn);
			tyroEn.leashedTo(leashE);
		}

		public TyroEntity getLastTyroEntity() {
			return tyroEntities.get(tyroEntities.size()-1);
		}

		public boolean addPoint(Block b) {
			if(isCorrect(b.getLocation())) {
				fenceBlock.add(b.getLocation());

				// CREATE NEW HITCH
				TyroHitchEntity leashE = new TyroHitchEntity(creator.getWorld());
				EntityCustom.spawnEntity(leashE, b.getLocation());
				leashHitch.add(leashE);

				// CREATE NEW TAIL
				TyroEntity tyroEn = new TyroEntity(creator.getWorld());
				EntityCustom.spawnEntity(tyroEn, creator.getLocation());

				tyroEn.leashedTo(leashE);
				tyroEntities.add(tyroEn);

				// TP OLD ARMOR ON NEW HITCH
				Location loc = leashE.getBukkitEntity().getLocation().clone(); loc.setY(loc.getY()-1.05);
				tyroEntities.get(tyroEntities.size()-2).getBukkitEntity().teleport(loc);

				return true;
			} else return false;
		}

		public boolean validateCreation(Block b) {
			if(isCorrect(b.getLocation())) {
				fenceBlock.add(b.getLocation());

				// CREATE LAST HITCH
				TyroHitchEntity leashE = new TyroHitchEntity(creator.getWorld());
				EntityCustom.spawnEntity(leashE, b.getLocation());
				leashHitch.add(leashE);

				// TP LAST TAIL ON LAST HITCH
				Location loc = leashE.getBukkitEntity().getLocation().clone(); loc.setY(loc.getY()-1.05);
				tyroEntities.get(tyroEntities.size()-1).getBukkitEntity().teleport(loc);
				creator = null;

				saveTyolienne();

				instances.remove(this);

				return true;
			}else return false;
		}

		private static boolean isCorrect(Location loc) {
			if(fenceBlock.contains(loc)) return false;
			else return true;
		}

		private void saveTyolienne() {
			// SAVE DATA IN FILE
			File file = new File(McBoyard.folderPath+"/tyros/"+id+".yml");
			if(!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			config.set("Hitch", getLocList(this.leashHitch));
			config.set("Tail", getLocList(this.tyroEntities));
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// CREATE TYROLIENNE OBJECT
			Tyrolienne.createTyro(this);
		}

		public static TyroTemp startNewCreation(Player p, Block fceBlockStrt) {
			if(isCorrect(fceBlockStrt.getLocation())) {
				return new TyroTemp(p, fceBlockStrt);
			}else return null;
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

		private ArrayList<Location> getLocList(ArrayList<? extends Entity> enList) {
			ArrayList<Location> locList = new ArrayList<>();
			for(Entity e : enList) {
				locList.add(e.getBukkitEntity().getLocation());
			}
			return locList;
		}
	}
}
