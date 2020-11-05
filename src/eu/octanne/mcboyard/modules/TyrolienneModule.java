package eu.octanne.mcboyard.modules;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.Utils;
import eu.octanne.mcboyard.Utils.SchedulerTask;
import eu.octanne.mcboyard.entity.EntityCustom;
import eu.octanne.mcboyard.entity.TyroEntity;
import eu.octanne.mcboyard.entity.TyroHitchEntity;
import eu.octanne.mcboyard.entity.TyroSeatEntity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EnumMoveType;
import net.minecraft.server.v1_12_R1.PacketPlayOutAttachEntity;

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
		pl.getCommand("tyro").setTabCompleter(new TyrolienneTabCompleter());
		Tyrolienne.loadTyros();
	}

	public void onDisable() {
		HandlerList.unregisterAll(this);
		TyroTemp.unloadTyros();
		TyroTemp.fenceBlock.clear();
		Tyrolienne.unloadTyros();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		injectPlayer(e.getPlayer());
		if(e.getPlayer().getScoreboardTags().contains("onTyro")) {
			e.getPlayer().getScoreboardTags().remove("onTyro");
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		removePlayer(e.getPlayer());
	}

	private void removePlayer(Player player) {
		Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
		channel.eventLoop().submit(()->{
			channel.pipeline().remove(player.getName()+"-3");
			return null;
		});
	}

	private void injectPlayer(Player player) {
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

			@Override
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
				super.channelRead(channelHandlerContext, packet);
				//Bukkit.getServer().getConsoleSender().sendMessage("§ePacket READ : §c" + packet.toString());
			}

			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
				super.write(channelHandlerContext, packet, channelPromise);
				if(packet.toString().contains("PacketPlayOutSpawnEntity") || packet.toString().contains("PacketPlayOutSpawnEntityLiving")) {
					Field aF = packet.getClass().getDeclaredField("b");
					aF.setAccessible(true);
					UUID id = (UUID) aF.get(packet);
					try{
						TyroEntity it = TyroEntity.getTyroEntity(id);
						if(it != null) {
							// SEND PACKET TO ATTACH
							PacketPlayOutAttachEntity packetS = new PacketPlayOutAttachEntity(it, it.getLeashHolder());
							((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetS);
						}
					}catch (ConcurrentModificationException e){
						return;
					}
				}
				//Bukkit.getServer().getConsoleSender().sendMessage("§bPacket READ : §c" + packet.toString());
			}

		};

		ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
		pipeline.addBefore("packet_handler", player.getName()+"-3", channelDuplexHandler);
	}


	static class Tyrolienne {

		static private ArrayList<Tyrolienne> loadedInstances = new ArrayList<>();

		private UUID id;

		private ArrayList<TyroHitchEntity> hitchEntities = new ArrayList<>();
		private ArrayList<TyroEntity> tailEntities = new ArrayList<>();

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
				if(i < tailEntities.size()) {
					TyroEntity tyroEn = new TyroEntity(tailEntities.get(i).getWorld());
					this.tailEntities.add(tyroEn);
					EntityCustom.spawnEntity(tyroEn, tailEntities.get(i));
					tyroEn.leashedTo(leashE);
				}

				TyroTemp.fenceBlock.add(hitchEntities.get(i).getBlock().getLocation());
			}
		}

		public void removeEntities() {
			for(Entity en : tailEntities) {
				((TyroEntity)en).needToDie = true;
				en.getBukkitEntity().remove();
			}
			for(Entity en : hitchEntities) {
				((TyroHitchEntity)en).needToDie = true;
				TyroTemp.removeLocBlock(en.getBukkitEntity().getLocation().getBlock());
				en.getBukkitEntity().remove();
			}
		}

		public static void unloadTyros() {
			for(Tyrolienne tp : loadedInstances) {
				tp.removeEntities();
			}
			loadedInstances.clear();
		}

		public static void createTyro(TyroTemp temp) {
			loadedInstances.add(new Tyrolienne(temp));
		}

		@SuppressWarnings("unchecked")
		public static void loadTyros() {
			File fileP = new File(McBoyard.folderPath+"/tyros");
			if(fileP.exists()) {
				for(File file : new File(McBoyard.folderPath+"/tyros").listFiles()) {
					YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
					loadedInstances.add(new Tyrolienne(UUID.fromString(file.getName().substring(0,file.getName().length()-5)), 
							(ArrayList<Location>)config.get("Hitch"), 
							(ArrayList<Location>)config.get("Tail")));
				}
			}
		}

		public UUID getID() {
			return id;
		}
		
		private class VectResult {
			
			Vector vect;
			int nbItr;
			
			public VectResult(Vector v,int nbItr) {
				this.vect = v;
				this.nbItr = nbItr;
			}
			
		}
		
		private VectResult modifyVect(Vector vec) {
			int nbTurn = 1;
			while(vec.length() > 0.30) {
				//Bukkit.broadcastMessage("length="+vec.length());
				vec = Utils.divideVect(vec, 2);
				nbTurn *=2;
			}
			return new VectResult(vec,nbTurn);
		}
		
		public void useTyro(Player p) {
			if(!p.getScoreboardTags().contains("onTyro")) {
				p.addScoreboardTag("onTyro");
				TyroSeatEntity en = new TyroSeatEntity(p.getWorld());
				EntityCustom.spawnEntity(en, this.hitchEntities.get(0).getBukkitEntity().getLocation());
				en.putOnSeat(p);
				Tyrolienne tyroS = this;
				
				// JOUER SON DURANT LA DESCENTE (EYLTRA FLYING)
				p.playSound(p.getLocation(), Sound.ITEM_ELYTRA_FLYING, 0.5f, 1.0f);

				// en.getBukkitEntity().getLocation().distance(loc2) > 0.5
				SchedulerTask task = new SchedulerTask(0,1){

					int idxTyro = 0;
					
					Tyrolienne tyro = tyroS;
					
					Location locStart = tyro.hitchEntities.get(0).getBukkitEntity().getLocation();
					Location locArrive = tyro.hitchEntities.get(1).getBukkitEntity().getLocation();

					VectResult vec = modifyVect(Utils.calcVect(locStart, locArrive));
					int idx = 0;
					
					@Override
					public void run() {
						if(idx < vec.nbItr && en.getBukkitEntity().getLocation().distance(locArrive) > vec.vect.length()) {
							//Bukkit.broadcastMessage("Vector : "+vec.vect.toString()+" id= "+idx);
							en.move(EnumMoveType.SELF, vec.vect.getX(), vec.vect.getY(), vec.vect.getZ());
							idx++;
						}else if(idx < vec.nbItr) {
							vec = modifyVect(Utils.calcVect(locStart, locArrive));
							idx = 0;
						}else {
							if(idxTyro < tyro.hitchEntities.size()-2) {
								idxTyro++;
								locStart = en.getBukkitEntity().getLocation();
								locArrive = tyro.hitchEntities.get(idxTyro+1).getBukkitEntity().getLocation();
								vec = modifyVect(Utils.calcVect(locStart, locArrive));
								idx = 0;
							}else {
								// ACTION DE FIN DE COURSE
								if(p != null){
									p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 1.0f);
									p.getScoreboardTags().remove("onTyro");
								}
								en.needToDie = true;
								en.killEntity();
								cancelTask();
							}
						}
					}
				};
				task.startTask();
			}
		}

		public void removeTyro() {
			this.removeEntities();
			this.hitchEntities.clear();
			this.tailEntities.clear();
			new File(McBoyard.folderPath+"/tyros/"+this.id.toString()+".yml").delete();
			loadedInstances.remove(this);
		}

		public static Tyrolienne getTyro(UUID id) {
			for(Tyrolienne tyro : loadedInstances) {
				if(tyro.id.equals(id)) return tyro;
			}
			return null;
		}

		public static ArrayList<Tyrolienne> getTyros() {
			return loadedInstances;
		}

		public String getStartLocStr() {
			Location loc = hitchEntities.get(0).getBukkitEntity().getLocation();
			String locStr = "X : " + loc.getBlockX() + " Y : " + loc.getBlockY() + " Z : " + loc.getBlockZ() + " (" + loc.getWorld().getName() + ")";
			return locStr;
		}

		public TyroHitchEntity getFirstTyroEntity() {
			return hitchEntities.get(0);
		}

		public String getEndLocStr() {
			Location loc = hitchEntities.get(hitchEntities.size()-1).getBukkitEntity().getLocation();
			String locStr = "X : " + loc.getBlockX() + " Y : " + loc.getBlockY() + " Z : " + loc.getBlockZ() + " (" + loc.getWorld().getName() + ")";
			return locStr;
		}
	}

	private class TyrolienneTabCompleter implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
			if(args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info"))) {
				List<String> tab = new ArrayList<>();
				for(Tyrolienne tyro : Tyrolienne.loadedInstances) {
					tab.add(tyro.getID().toString());
				}
				return tab;
			} else if(args.length == 1) {
				List<String> tab = new ArrayList<>();
				tab.add("help"); tab.add("list");
				tab.add("remove"); tab.add("info");
				tab.add("wrench"); tab.add("reload");
				return tab;
			}
			return null;
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
					} else if(args[0].equalsIgnoreCase("reload")) {
						sender.sendMessage("§9Tyro §8|§a Rechargement des tyroliennes...");
						McBoyard.tyroModule.onDisable();
						McBoyard.tyroModule.onEnable();
						sender.sendMessage("§9Tyro §8|§a Rechargement terminé!");
						return true;
					} else if(args[0].equalsIgnoreCase("help")) {
						sender.sendMessage("§8<-- §cModule Tyrolienne §8- §aPage d'aide §8-->");
						sender.sendMessage("§8- §9/tyro help §8: §baffiche cette aide.");
						sender.sendMessage("§8- §9/tyro reload §8: §brecharge le module.");
						sender.sendMessage("§8- §9/tyro wrench §8: §bdonne la wrench de pose.");
						sender.sendMessage("§8- §9/tyro list §8: §baffiche la listes des tyros.");
						sender.sendMessage("§8- §9/tyro remove <ID> §8: §bsupprime la tyrolienne.");
						sender.sendMessage("§8- §9/tyro info <ID> §8: §baffiche les spécificitées de la tyro.");
						return true;
					} else if(args[0].equalsIgnoreCase("list")) {
						sender.sendMessage("§8<-- §cModule Tyrolienne §8- §aListe des Tyros §8-->");
						for(Tyrolienne tyro : Tyrolienne.getTyros()) {
							sender.sendMessage("§8- §aID §8: §9"+tyro.getID());
							sender.sendMessage("  §aLoc : §e"+tyro.getStartLocStr());
						}
						return true;
					} else if(args[0].equalsIgnoreCase("remove")) {
						if(args.length > 1) {
							try{
								Tyrolienne tyro = Tyrolienne.getTyro(UUID.fromString(args[1]));
								if(tyro != null) {
									sender.sendMessage("§9Tyro §8|§a La tyro (ID) : §9"+tyro.getID()+"§a, est supprimé!");
									tyro.removeTyro();
									return true;
								} else {
									sender.sendMessage("§9Tyro §8|§c Erreur : L'ID spécifié n'a pas été reconnu.");
									return false;
								}
							}catch(IllegalArgumentException e) {
								sender.sendMessage("§9Tyro §8|§c Erreur : L'ID spécifié n'a pas été reconnu.");
								return false;
							}
						} else {
							sender.sendMessage("§9Tyro §8|§c Usage : §e/tyro remove <ID>");
							return false;
						}
					} else if(args[0].equalsIgnoreCase("info")) {
						if(args.length > 1) {
							try {
								Tyrolienne tyro = Tyrolienne.getTyro(UUID.fromString(args[1]));
								if(tyro != null) {
									sender.sendMessage("§8<-- §cModule Tyrolienne §8- §aInfo Tyro §8-->");
									sender.sendMessage("§8- §aID §8: §9"+tyro.getID());
									sender.sendMessage("§8- §aLocation Begin : §e"+tyro.getStartLocStr());
									sender.sendMessage("§8- §aLocation End : §e"+tyro.getEndLocStr());
									sender.sendMessage("§8<-- §cModule Tyrolienne §8- §aInfo Tyro §8-->");
									return true;
								} else {
									sender.sendMessage("§9Tyro §8|§c Erreur : L'ID spécifié n'a pas été reconnu.");
									return false;
								}
							}catch(IllegalArgumentException e) {
								sender.sendMessage("§9Tyro §8|§c Erreur : L'ID spécifié n'a pas été reconnu.");
								return false;
							}
						} else {
							sender.sendMessage("§9Tyro §8|§c Usage : §e/tyro info <ID>");
							return false;
						}
					} else {
						sender.sendMessage("§9Tyro §8|§c Usage : §e/tyro <subcmd> [args] §c- §b/tyro help §cpour l'aide.");
						return false;
					}
				} else {
					sender.sendMessage("§9Tyro §8|§c Usage : §e/tyro <subcmd> [args] §c- §b/tyro help §cpour l'aide.");
					return false;
				}
			} else {
				sender.sendMessage("Vous n'avez pas la permission pour cette commande.");
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
			for(Tyrolienne tyro : Tyrolienne.getTyros()) {
				if(tyro.getFirstTyroEntity().getUniqueID().equals(e.getRightClicked().getUniqueId())) {
					tyro.useTyro(e.getPlayer());
				}
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onClick(EntityDamageEvent e) {
		if(((CraftEntity) e.getEntity()).getHandle() instanceof TyroHitchEntity) {
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

		public static void removeLocBlock(Block block) {
			Location locS = null;
			for(Location loc1 : fenceBlock) {
				if(loc1.getBlock().equals(block))
					locS = loc1;
			}
			if(locS != null)
				fenceBlock.remove(locS);
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

		public void removeEntities() {
			for(Entity en : leashHitch) {
				en.getBukkitEntity().remove();
			}
			for(Entity en : tyroEntities) {
				en.getBukkitEntity().remove();
			}
		}

		public static void unloadTyros() {
			for(TyroTemp tp : instances) {
				tp.removeEntities();
			}
		}

		private static boolean isCorrect(Location loc) {
			for(Location loc1 : fenceBlock) {
				if(loc1.getBlock().equals(loc.getBlock()))
					return false;
			}
			return true;
		}

		private void saveTyolienne() {
			// SAVE DATA IN FILE
			File file = new File(McBoyard.folderPath+"/tyros/"+id.toString()+".yml");
			new File(McBoyard.folderPath+"/tyros").mkdirs();
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
