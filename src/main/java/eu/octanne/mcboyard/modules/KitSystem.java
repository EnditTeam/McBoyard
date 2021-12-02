package eu.octanne.mcboyard.modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;

import eu.octanne.mcboyard.McBoyard;

public class KitSystem extends Module {
	
	public KitSystem(JavaPlugin instance) {
		super(instance);
	}

	public static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public static RegionManager getRegionManager() {
		WorldGuardPlugin worldguard = getWorldGuard();
		if(worldguard == null)
			return null;
		return worldguard.getRegionContainer().getLoaded().get(0);
	}
	
	static int task;
	static ArrayList<Kit> kits;
	static ArrayList<KPlayer> kPlayers;
	static Listener listener;
	
	public void onEnable() {
		
		kits = new ArrayList<Kit>();
		kPlayers = new ArrayList<KPlayer>();
		
		kits = Kit.load();
		listener = new KitListener();
		Bukkit.getPluginManager().registerEvents(listener, McBoyard.instance);
		for(OfflinePlayer op : Bukkit.getOfflinePlayers()) {
			kPlayers.add(new KPlayer(op));
		}

		getWorldGuard();
		//FONCTIONNEMENT DU COOLDOWN
		launchCooldowns();
		
		pl.getCommand("kitreload").setExecutor(new KitReloadCommand());
		pl.getCommand("kitcreate").setExecutor(new KitCreateCommand());
		pl.getCommand("kit").setExecutor(new KitCommand());
	}
	
	public void onDisable() {
		Kit.save();
		KPlayer.save();
		
		HandlerList.unregisterAll(listener);
	}
	
	private void launchCooldowns() {
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(McBoyard.instance, new Runnable() {

			@Override
			public void run() {
				for(KPlayer kp : kPlayers) {
					for(Kit kit : kits) {
						if(kp.getCooldown(kit) != 0) {
							kp.setCooldown(kit, kp.getCooldown(kit) - 1);
						}
					}
				}
				
			}
			
		}, 20, 20);
	}
	
	/*
	 * COMMANDS
	 */
	class KitCommand implements CommandExecutor{

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				MenuKit.open(p);
				sender.sendMessage(ChatColor.GOLD+"Ouverture du menu kit...");
				return true;
			}else {
				sender.sendMessage(ChatColor.RED+"Ouverture impossible car console...");
				return false;
			}
		}

	}
	class KitCreateCommand implements CommandExecutor{

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			
			if(args.length >= 1 ) {
				Player p = (Player) sender;
				File fileM = new File(McBoyard.folderPath+"/KitSystem/config.yml");
				YamlConfiguration configM = YamlConfiguration.loadConfiguration(fileM);
				@SuppressWarnings("unchecked")
				ArrayList<String> kitNames = (ArrayList<String>) configM.get("Kits", new ArrayList<String>());
				if(!kitNames.contains(args[0])) {
					Kit kit = new Kit(args[0], args[0], "kit."+args[0], 0, 0, Material.STONE, (byte) 0, new ArrayList<String>());
					for(ItemStack item : p.getInventory().getContents()) {
						kit.addItem(item);
					}
					kitNames.add(kit.getName());
					configM.set("Kits", kitNames);
					try {
						configM.save(fileM);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					kits.add(kit);
					sender.sendMessage(ChatColor.BLUE+"[Kit] Le kit "+args[0]+" a étè ajouté");
					Kit.save();
					return true;
				}else {
					sender.sendMessage(ChatColor.BLUE+"[Kit] "+ChatColor.RED+"Kit déjè existant !");
					return false;
				}

			}else {
				sender.sendMessage(ChatColor.BLUE+"[Kit] "+ChatColor.RED+"Précisé un nom !");
				return false;
			}
		}

	}
	class KitReloadCommand implements CommandExecutor{

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			kits = Kit.load();
			sender.sendMessage(ChatColor.BLUE+"[Kit] §6rechargement des fichiers terminé !");
			return true;
		}
		
	}
	
	/*
	 * STATUTTYPE
	 */
	public enum StatutType {
		SUCCESS(), COOLDOWN_DENIED(), PERMISSION_DENIED();
	}
	
	/*
	 * MenuKit
	 */
	static public class MenuKit {
		
		static public void open(Player p) {
			Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY+""+ChatColor.BOLD+"Kits");
			for(Kit kit : kits) {
				inv.setItem(kit.getPosition(), kit.getKitItem());
			}
			p.openInventory(inv);
		}
		
		static public StatutType giveKit(Kit kit, Player p) {
			KPlayer kp = new KPlayer(p);
			for(KPlayer kPlayer : kPlayers) {
				if(kPlayer.getOfflinePlayer().getName() == p.getName()) {
					kp = kPlayer;
				}
			}
			if(kp.getCooldown(kit) == 0 && p.hasPermission(kit.getPermission())) {
				if(!getRegionManager().hasRegion(kit.getName())) {
					for(ItemStack item : kit.getContents()) {
						if(item != null) {
							if(p.getInventory().firstEmpty() == -1) {
								p.getWorld().dropItem(p.getLocation(), item);
							}else {
								p.getInventory().addItem(item);
							}
						}
					}
					kp.setCooldown(kit, kit.getCooldown());
					return StatutType.SUCCESS;
				}else if(getRegionManager().getRegion(kit.getName()).contains(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ())){
					for(ItemStack item : kit.getContents()) {
						if(item != null) {
							if(p.getInventory().firstEmpty() == -1) {
								p.getWorld().dropItem(p.getLocation(), item);
							}else {
								p.getInventory().addItem(item);
							}
						}
					}
					kp.setCooldown(kit, kit.getCooldown());
					return StatutType.SUCCESS;
				}else {
					return StatutType.PERMISSION_DENIED;
				}
			}else if(!p.hasPermission(kit.getPermission())){
				return StatutType.PERMISSION_DENIED;
			}else if(kp.getCooldown(kit) != 0){
				return StatutType.COOLDOWN_DENIED;
			}else {
				return null;
			}
		}
	}
	
	/*
	 * LISTENER
	 */
	public class KitListener implements Listener{
		
		@SuppressWarnings("deprecation")
		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent e) {
			File file = new File(McBoyard.folderPath+"/KitSystem/Players/"+e.getPlayer().getUniqueId()+".yml");
			if(!file.exists()) {
				kPlayers.add(new KPlayer(Bukkit.getOfflinePlayer(e.getPlayer().getName())));
			}
		}
		
		@EventHandler
		public void onClickInventory(InventoryClickEvent e) {
			if(e.getClickedInventory() != null && e.getClickedInventory().getName().equalsIgnoreCase(ChatColor.DARK_GRAY+""+ChatColor.BOLD+"Kits")) {
				e.setCancelled(true);
				if(e.getCurrentItem() != null && !e.getClick().isShiftClick() && (e.getClick().isRightClick() || e.getClick().isLeftClick())) {
					for(Kit kit : kits) {
						if(e.getCurrentItem().isSimilar(kit.getKitItem())) {
							Player p = (Player) e.getWhoClicked();
							StatutType statut = MenuKit.giveKit(kit, p);
							if(statut.equals(StatutType.SUCCESS)) {
								p.sendMessage(ChatColor.BLUE+"[Kit] "+ChatColor.YELLOW+"Vous venez de recevoir le kit "+kit.getDisplayName());
							}else if(statut.equals(StatutType.COOLDOWN_DENIED)) {
								int secondes = 0, minutes, heures;
								for(KPlayer kp: kPlayers) {
									if(kp.getOfflinePlayer().getName() == p.getName()) {
										secondes = kp.getCooldown(kit);
									}
								}
								heures = secondes/3600;
								secondes = secondes%3600;
								minutes = secondes/60;
								secondes = secondes%60;
								
								p.sendMessage(ChatColor.BLUE+"[Kit] "+ChatColor.RED+"Vous avez déjà récupéré ce kit, attendez "+heures+" heures "+minutes+" minutes et "+secondes+" secondes.");
							}else if(statut.equals(StatutType.PERMISSION_DENIED)) {
								p.sendMessage(ChatColor.BLUE+"[Kit] "+ChatColor.RED+"Vous ne pouvez disposer de ce kit ici ("+kit.getDisplayName()+ChatColor.RED+")");
							}
							p.closeInventory();
						}
					}
				}
				if(e.getCurrentItem() != null && e.getClick().isShiftClick()) {
					for(Kit kit : kits) {
						if(e.getCurrentItem().isSimilar(kit.getKitItem())) {
							Player p = (Player) e.getWhoClicked();
							Inventory inv = Bukkit.createInventory(null, 27, kit.getDisplayName());
							for(ItemStack item : kit.getContents()) {
								if(item != null) {
									inv.addItem(item);
								}
							}
							p.openInventory(inv);
						}
					}
				}
			}
			for(Kit kit : kits) {
				if(e.getClickedInventory() != null && e.getClickedInventory().getName().equalsIgnoreCase(kit.getDisplayName())) {
					Player p = (Player) e.getWhoClicked();
					if(!p.hasPermission("kit.admin")) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
		
	/*
	 * KIT
	 */
	static public class Kit {
		
		private String permission;
		private String displayName;
		private String name;
		private int position;
		private ArrayList<String> desc = new ArrayList<String>();
		private Material itemRepresent;
		private int itemRepresentData;
		private int cooldown;
		private ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		
		public Kit(String name, String displayName, String permission, int cooldown, int position, Material itemR, int itemRdata, ArrayList<String> desc) {
			this.name = name;
			this.displayName = displayName;
			this.permission = permission;
			this.position = position;
			this.cooldown = cooldown;
			this.itemRepresent = itemR;
			this.itemRepresentData = itemRdata;
			this.desc = desc;
		}
		
		public Kit(String name, String displayName, String permission, int cooldown,  int position, Material itemR, int itemRdata) {
			this.name = name;
			this.displayName = displayName;
			this.permission = permission;
			this.position = position;
			this.cooldown = cooldown;
			this.itemRepresent = itemR;
			this.itemRepresentData = itemRdata;
		}
		
		public String getPermission() {
			return this.permission;
		}
		
		public int getPosition() {
			return this.position;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getDisplayName() {
			return this.displayName;
		}
		
		public ArrayList<ItemStack> getContents() {
			return this.items;
		}
		
		public int getCooldown() {
			return this.cooldown;
		}
		
		public ArrayList<String> getDesc() {
			return this.desc;
		}
		
		@SuppressWarnings("deprecation")
		public ItemStack getKitItem() {
			ItemStack item = new ItemStack(itemRepresent, 1, (short) 0, (byte) this.itemRepresentData);
			ItemMeta itemM = item.getItemMeta();
			itemM.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			itemM.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemM.addItemFlags(ItemFlag.HIDE_DESTROYS);
			itemM.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			itemM.addItemFlags(ItemFlag.HIDE_PLACED_ON);
			itemM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			itemM.setDisplayName(displayName);
			itemM.setLore(desc);
			item.setItemMeta(itemM);
			return item;
		}
		
		public void addItem(ItemStack item) {
			this.items.add(item);
		}
		
		public void removeItem(ItemStack item) {
			this.items.add(item);
		}
		
		public void setContents(ArrayList<ItemStack> items) {
			this.items = items;
		}
		
		public static ArrayList<Kit> load() {
			ArrayList<Kit> kits = new ArrayList<Kit>();
			File fileM = new File(McBoyard.folderPath+"/KitSystem/config.yml");
			YamlConfiguration configM = YamlConfiguration.loadConfiguration(fileM);
			@SuppressWarnings("unchecked")
			ArrayList<String> kitNames = (ArrayList<String>) configM.get("Kits", new ArrayList<String>());
			
			if(!kitNames.isEmpty()) {
				for(String nKit : kitNames) {
					kits.add(getKit(nKit));
				}
			}
			
			return kits;
		}
		
		public static Kit getKit(String nKit) {
			File file = new File(McBoyard.folderPath+"/KitSystem/Kits/"+nKit+".yml");
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			@SuppressWarnings({ "deprecation", "unchecked" })
			Kit kit = new Kit(config.getString("name"), config.getString("displayname"), config.getString("permission"), config.getInt("cooldown"), config.getInt("position"), Material.getMaterial(config.getInt("logo-id")), ((byte)config.getInt("logo-data")), (ArrayList<String>)config.get("description"));
			@SuppressWarnings("unchecked")
			ArrayList<ItemStack> content = ((ArrayList<ItemStack>) config.get("Items"));
			kit.setContents(content);
			return kit;
		}
		
		@SuppressWarnings("deprecation")
		public static void save() {
			File fileM = new File(McBoyard.folderPath+"/KitSystem/config.yml");
			YamlConfiguration configM = YamlConfiguration.loadConfiguration(fileM);
			
			ArrayList<String> kitNames = new ArrayList<String>();
			
			for(Kit kit : kits) {
				kitNames.add(kit.getName());
				File file = new File(McBoyard.folderPath+"/KitSystem/Kits/"+kit.getName()+".yml");
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				config.set("name", kit.getName());
				config.set("displayname", kit.getDisplayName());
				config.set("description", kit.getDesc());
				config.set("permission", kit.getPermission());
				config.set("position", kit.getPosition());
				config.set("cooldown", kit.getCooldown());
				config.set("logo-id", kit.getKitItem().getTypeId());
				config.set("logo-data", (int)kit.getKitItem().getData().getData());
				
				config.set("Items", kit.getContents());
				
				try {
					config.save(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				configM.save(fileM);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * KPLAYER
	 */
	static public class KPlayer {
		
		private OfflinePlayer offlinePlayer;
		private HashMap<String, Integer> cooldownKits = new HashMap<String, Integer>();
		
		KPlayer(OfflinePlayer op){
			File file = new File(McBoyard.folderPath+"/KitSystem/Players/"+op.getUniqueId()+".yml");
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			this.offlinePlayer = op;
			if(file.exists()) {
				for(Kit kit : kits) {
					cooldownKits.put(kit.getName(), config.getInt(kit.getName(), 0));
				}
			}else {
				for(Kit kit : kits) {
					cooldownKits.put(kit.getName(), config.getInt(kit.getName(), 0));
				}
			}
		}
		
		@SuppressWarnings("deprecation")
		KPlayer(Player op){
			File file = new File(McBoyard.folderPath+"/KitSystem/Players/"+op.getUniqueId()+".yml");
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			this.offlinePlayer = Bukkit.getOfflinePlayer(op.getName());
			if(file.exists()) {
				for(Kit kit : kits) {
					cooldownKits.put(kit.getName(), config.getInt(kit.getName(), 0));
				}
			}else {
				for(Kit kit : kits) {
					cooldownKits.put(kit.getName(), config.getInt(kit.getName(), 0));
				}
			}
		}
		
		public int getCooldown(Kit kit) {
			if(cooldownKits.containsKey(kit.getName())) {
				return cooldownKits.get(kit.getName());
			}else {
				return 0;
			}
			
		}
		
		public OfflinePlayer getOfflinePlayer() {
			return this.offlinePlayer;
		}
		
		public void setCooldown(Kit kit, int cooldown) {
			cooldownKits.remove(kit.getName());
			cooldownKits.put(kit.getName(), cooldown);
		}
		
		public static void save() {
			for(KPlayer kp : kPlayers) {
				File file = new File(McBoyard.folderPath+"/KitSystem/Players/"+kp.getOfflinePlayer().getUniqueId()+".yml");
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				for(Kit kit : kits) {
					config.set(kit.getName(), kp.getCooldown(kit));
				}
				try {
					config.save(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
