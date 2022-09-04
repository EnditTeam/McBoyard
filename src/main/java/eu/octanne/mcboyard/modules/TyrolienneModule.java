package eu.octanne.mcboyard.modules;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_16_R3.Vec3D;
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
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
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
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.PacketPlayOutAttachEntity;

public class TyrolienneModule extends PlugModule implements Listener {

	static ItemStack wandItem = Utils.createItemStack("§6Tyrolienne Wrench", Material.STICK, 1, null, 0, true, false); 

	static private boolean debug = false;

	public TyrolienneModule(JavaPlugin pl) {
		super(pl);
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, super.pl);
		pl.getCommand("tyro").setExecutor(new TyrolienneCommand());
		pl.getCommand("tyro").setTabCompleter(new TyrolienneTabCompleter());
		Tyrolienne.loadTyros();
	}

	public void onDisable() {
		HandlerList.unregisterAll(this);
		TyroTemp.unloadTyros();
		TyroTemp.fenceBlock.clear();
		Tyrolienne.unloadTyros();

		TyroSeatEntity.killAll();
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
		if(e.getPlayer().getScoreboardTags().contains("onTyro")) {
			e.getPlayer().getScoreboardTags().remove("onTyro");
			e.getPlayer().leaveVehicle();
		}
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
				if(packet.toString().contains("PacketPlayInSteerVehicle")) {
					Field field4 = packet.getClass().getDeclaredField("d");
					field4.setAccessible(true);// allows us to access the field
					boolean dismount = field4.getBoolean(packet);
					if(player.getScoreboardTags().contains("onTyro") && dismount) {
						return;
					}
				}
				super.channelRead(channelHandlerContext, packet);
				//Bukkit.getServer().getConsoleSender().sendMessage("§ePacket READ : §c" + packet.toString());
			}

			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
				super.write(channelHandlerContext, packet, channelPromise);
				if(packet.toString().contains("PacketPlayOutSpawnEntity") || packet.toString().contains("PacketPlayOutSpawnEntity")) {
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
						System.out.println("ConcurrentModif on TyroEntity attachEntity");
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
				en.die();
			}
			for(Entity en : hitchEntities) {
				((TyroHitchEntity)en).needToDie = true;
				TyroTemp.removeLocBlock(en.getBukkitEntity().getLocation().getBlock());
				en.die();
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

		/*private class VectResult {

			Vector vect;
			int nbItr;

			public VectResult(Vector v,int nbItr) {
				this.vect = v;
				this.nbItr = nbItr;
			}

		}

		private VectResult modifyVect(Vector vec) {
			int nbTurn = 1;
			while(vec.length() > 0.70 0.35) {
				//Bukkit.broadcastMessage("length="+vec.length());
				vec = Utils.divideVect(vec, 2);
				nbTurn *=2;
			}
			return new VectResult(vec,nbTurn);
		}*/

		private class PolyCable {

			private double a,b,c,tMax;
			private double deltaX, deltaZ;

			//private static final double pas = 0.5;

			private Location locStart;
			private Location locArrive;

			public PolyCable(Location locStart, Location locArrive) {
				deltaX = locArrive.getX()-locStart.getX();
				deltaZ = locArrive.getZ()-locStart.getZ();

				this.locStart = locStart;
				this.locArrive = locArrive;

				double xB = Math.sqrt(deltaX*deltaX+deltaZ*deltaZ); // Point 2D d'arrivé (MAX)
				double yB = locArrive.getY()-locStart.getY();

				tMax = xB;

				double deltaC = Math.log10(Math.sqrt(xB*xB+yB*yB))*2; // Coef rectif position yC suivant la droite

				//double xC = xB/2; // Point 2D du milieu
				double yC = yB/2d-deltaC;

				a = 2d*(yB-2d*yC)/(xB*xB);
				b = (yB-a*(xB*xB))/xB;
				c = 0;
			}

			public Vector getVector(double t, double delta) {
				Vector vec = Utils.calcVect(getLoc(t), getLoc(t+delta));
				//Vector vec = new Vector((pas*deltaX)/tMax,-(getY2D(t+pas)-getY2D(t)),(pas*deltaZ)/tMax);
				return vec;
			}
			
            public double getDeriveeAt(double t) {
                return 2*a*t + b;
            }
			
			private double getY2D(double x) {
				return a*(x*x)+b*x+c;
			}

			private Location getLoc(double t) {
				double x = locStart.getX()+t/tMax*(locArrive.getX()-locStart.getX());
				double y = getY2D(t)+locStart.getY();
				double z = locStart.getZ()+t/tMax*(locArrive.getZ()-locStart.getZ());
				return new Location(locStart.getWorld(),x,y,z);
			}
		}

		private Location rectifyLoc(Location location, double yMin) {
			location.setY(location.getY()-yMin);
			return location;
		}

		private Location getLocLeash(int i) {
			return rectifyLoc(this.hitchEntities.get(i).getLoc(),2.65);
		}

		public void useTyro(Player p) {
			if(!p.getScoreboardTags().contains("onTyro")) {
				p.addScoreboardTag("onTyro");
				TyroSeatEntity en = new TyroSeatEntity(p.getWorld());
				EntityCustom.spawnEntity(en, getLocLeash(0)); // 3.35 // 2.35
				en.putOnSeat(p);
				Tyrolienne tyroS = this;

				// JOUER SON DURANT LA DESCENTE (EYLTRA FLYING)
				p.playSound(p.getLocation(), Sound.ITEM_ELYTRA_FLYING, 0.8f, 1.0f);
				SchedulerTask task = new SchedulerTask(0,1){

					int idxTyro = -1;
					double idx = 0;
					double motion = 0.5;
					
					Tyrolienne tyro = tyroS;

					Location locStart; // 3.35 // 2.35
					Location locArrive; // 5.35 // 0

					PolyCable poly;

					
					@Override
					public void run() {
						if(p == null || en.getBukkitEntity().getPassenger() == null || !en.getBukkitEntity().getPassenger().equals(p)) {
							if(p != null) p.getScoreboardTags().remove("onTyro");
							en.needToDie = true;
							en.die();
							cancelTask();
							return;
						}
						if((idxTyro == -1) || idx >= poly.tMax) {
							if(idxTyro < tyro.hitchEntities.size()-2) {
								idxTyro++;
								idx = 0;

								locStart = getLocLeash(idxTyro); // 3.35 // 0
								locArrive = getLocLeash(idxTyro+1); // 5.35 // 0
								poly = new PolyCable(locStart, locArrive);
								
								
								Vector toLocStart = Utils.calcVect(en.getBukkitEntity().getLocation(), locStart);
								en.move(EnumMoveType.SELF, new Vec3D(toLocStart.getX(), toLocStart.getY(), toLocStart.getZ()));
								
								if(debug) {
									Bukkit.broadcastMessage("Poteau : "+idxTyro+" passé");
									Bukkit.broadcastMessage("locStart :"+locStart.toString());
									Bukkit.broadcastMessage("locArrive :"+locArrive.toString());
								}
							}else {
								// ACTION DE FIN DE COURSE
								p.stopSound(Sound.ITEM_ELYTRA_FLYING);
								p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 1.0f);		
								Location loc = en.getBukkitEntity().getLocation().clone();
								loc.setY(loc.getY()-2);
								if(!loc.getBlock().getType().equals(Material.AIR)) {
									en.move(EnumMoveType.SELF, new Vec3D(0, 2, 0));
								}
								p.leaveVehicle();
								p.getScoreboardTags().remove("onTyro");
								en.needToDie = true;
								en.die();
								cancelTask();
							}
						}else {
						double derive = poly.getDeriveeAt(idx);
						motion -= 0.01*derive; //descendre == accélére
						if (motion < 0.1) motion = 0.1;
						Vector vec = poly.getVector(idx, motion);
						en.move(EnumMoveType.SELF, new Vec3D(vec.getX(), vec.getY(), vec.getZ()));
						idx += motion;
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
