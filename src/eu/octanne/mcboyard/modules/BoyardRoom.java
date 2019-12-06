package eu.octanne.mcboyard.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.BoyardRoom.SecureCode.LetterStat;

public class BoyardRoom implements Listener{
	
	private HashMap<String, String> digiCodeCorrespondence = new HashMap<String, String>();
	
	protected int task;
	
	protected boolean passwordisValidate = false;
	
	protected SecureCode roomCode;
	
	public BoyardRoom() {
		onEnable();
		Bukkit.getPluginManager().registerEvents(this, McBoyard.instance);
	}
	
	public void onEnable() {
		digiCodeMapCorrespondence();
		if(!McBoyard.config.isSet("BoyardRoom.boyardPassword"))McBoyard.config.set("BoyardRoom.boyardPassword", "ABCDE");
		try {
			McBoyard.config.save(McBoyard.fileConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
		roomCode = new SecureCode(McBoyard.config.getString("BoyardRoom.boyardPassword", "ABCDE"));
	}
	
	public void onDisable() {
		
	}
	
	public void changePassword(String password) {
		McBoyard.config.set("BoyardRoom.boyardPassword", password);
		try {
			McBoyard.config.save(McBoyard.fileConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
		roomCode = new SecureCode(McBoyard.config.getString("BoyardRoom.boyardPassword", "ABCDE"));
		passwordisValidate = false;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(passwordisValidate) return;
		if(e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY()
				&& e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
			return;
		}
		for(LetterStat stat : roomCode.secretSentence) {
			Location locFrom = e.getFrom().clone();
			locFrom.setY(locFrom.getY()-1);
			@SuppressWarnings("deprecation")
			String blockID = locFrom.getBlock().getType().getId() + ":" + (int) locFrom.getBlock().getData();
			/*Bukkit.broadcastMessage("[Debug] (From) Block ID : " + blockID + ", stat = " + stat.active);*/
			if(blockID.equalsIgnoreCase(digiCodeCorrespondence.get(stat.letter))) {
				//Check if have more than 1 player on the block
				boolean otherPlayer = false;
				for(Entity entity : e.getPlayer().getNearbyEntities(2, 2, 2)) {
					if(entity instanceof Player) {
						Location locEntity = entity.getLocation().clone();
						locEntity.setY(locEntity.getY()-1);
						if(locEntity.getBlock().equals(locFrom.getBlock())) {
							otherPlayer = true;
							break;
						}
					}
				}
				if(!otherPlayer)stat.active = false;
				/*Bukkit.broadcastMessage("[Debug] (New) (From) Block ID : " + blockID + ", stat = " + stat.active);*/
			}
		}
		for(LetterStat stat : roomCode.secretSentence) {
			Location locTo = e.getTo().clone();
			locTo.setY(locTo.getY()-1);
			@SuppressWarnings("deprecation")
			String blockID = locTo.getBlock().getType().getId() + ":" + (int) locTo.getBlock().getData();
			/*Bukkit.broadcastMessage("[Debug] (To) Block ID : " + blockID + ", stat = " + stat.active);*/
			if(blockID.equalsIgnoreCase(digiCodeCorrespondence.get(stat.letter))) {
				stat.active = true;
				/*Bukkit.broadcastMessage("[Debug] (New) (To) Block ID : " + blockID + ", stat = " + stat.active);*/
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(McBoyard.instance, new Runnable() {

			@Override
			public void run() {
				if(roomCode.checkPassword()) launchAnimation();
			}
		}, 20);
	}
	
	/*
	 * Launch all Animation (Boyard "WaterFall" and Firework)
	 */
	protected void launchAnimation() {
		passwordisValidate = true;
		//Fireworks && Title
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(McBoyard.instance, new Runnable() {
			
			int round = 1;
			
			@Override
			public void run() {
				if(round == 1) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-242 114 -185" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-242 114 -165" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-268 114 -171" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-268 114 -179" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
					for(Player p : Bukkit.getOnlinePlayers()) {
						p.sendTitle("§aFélicitation !", "", 10, 70, 20);
					}
				}
				else if(round == 2) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-248 114 -185" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-248 114 -165" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-262 114 -182" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-262 114 -167" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
				}
				else if(round == 3) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-254 114 -185" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/summon fireworks_rocket " + "-254 114 -165" + " {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Flicker:1,Trail:1,Colors:[I;16411936],FadeColors:[I;16777118]}]}}}}");
				}
				else {
					Bukkit.getScheduler().cancelTask(task);
				}
				round++;
			}
		}, 0, 15);
		//Chrono de 1 minutes
		McBoyard.chronoModule.chrono(60);
		//Laché des Boyards
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(McBoyard.instance, new Runnable() {
			
			int round = 1;
			
			@Override
			public void run() {
				if(round == 1) {
					for(int xAdd = 0; xAdd < 9; xAdd++) {
						for(int zAdd = 0; zAdd < 7; zAdd++) {
							Bukkit.getWorld("world").dropItem(new Location(Bukkit.getWorld("world"), -298-xAdd, 120, -178+zAdd), new ItemStack(Material.GOLD_NUGGET, 10));
						}
					}
				}
				else if(round == 2) {
					for(int xAdd = 0; xAdd < 9; xAdd++) {
						for(int zAdd = 0; zAdd < 7; zAdd++) {
							Bukkit.getWorld("world").dropItem(new Location(Bukkit.getWorld("world"), -298-xAdd, 120, -178+zAdd), new ItemStack(Material.GOLD_INGOT, 2));
						}
					}
				}
				else if(round == 3) {
					for(int xAdd = 0; xAdd < 9; xAdd++) {
						for(int zAdd = 0; zAdd < 7; zAdd++) {
							Bukkit.getWorld("world").dropItem(new Location(Bukkit.getWorld("world"), -298-xAdd, 120, -178+zAdd), new ItemStack(Material.GOLD_NUGGET, 10));
						}
					}
				}
				else if(round == 4) {
					for(int xAdd = 0; xAdd < 9; xAdd++) {
						for(int zAdd = 0; zAdd < 7; zAdd++) {
							Bukkit.getWorld("world").dropItem(new Location(Bukkit.getWorld("world"), -298-xAdd, 120, -178+zAdd), new ItemStack(Material.GOLD_INGOT, 2));
						}
					}
				}
				else {
					Bukkit.getScheduler().cancelTask(task);
				}
				round++;
			}
		}, 0, 15);
	}
	
	protected class SecureCode {
		
		public ArrayList<LetterStat> secretSentence = new ArrayList<LetterStat>();
		
		public SecureCode(String code) {
			for(int i  = 0; i < code.length(); i++) {
				secretSentence.add(new LetterStat(""+code.charAt(i)));
			}
		}
		
		public boolean checkPassword() {
			/*Bukkit.broadcastMessage("[Debug] Check password :");*/
			for(LetterStat stat : secretSentence) {
				/*Bukkit.broadcastMessage("[Debug] Letter : " + stat.letter + ", BlockID : " + digiCodeCorrespondence.get(stat.letter) + ", stat = " + stat.active);*/
				if(stat.active == false) return false;
			}
			return true;
		}
		
 		protected class LetterStat {
 			
 			protected String letter;
 			protected boolean active;
 			
 			public LetterStat(String letter) {
 				this.active = false;
 				this.letter = letter;
 			}
 		}
	}
	
	/*
	 * Mapping Correspondence Code
	 */
	private void digiCodeMapCorrespondence() {
		digiCodeCorrespondence.put("A", "251:0"); digiCodeCorrespondence.put("B", "251:9");
		digiCodeCorrespondence.put("C", "251:2"); digiCodeCorrespondence.put("D", "251:3");
		digiCodeCorrespondence.put("E", "251:4"); digiCodeCorrespondence.put("F", "251:5");
		digiCodeCorrespondence.put("G", "251:6"); digiCodeCorrespondence.put("H", "251:7");
		digiCodeCorrespondence.put("I", "251:8"); digiCodeCorrespondence.put("J", "251:10");
		digiCodeCorrespondence.put("K", "251:11"); digiCodeCorrespondence.put("L", "251:12");
		digiCodeCorrespondence.put("M", "251:13"); digiCodeCorrespondence.put("N", "251:14");
		digiCodeCorrespondence.put("O", "251:15"); digiCodeCorrespondence.put("P", "159:0");
		digiCodeCorrespondence.put("Q", "159:1"); digiCodeCorrespondence.put("R", "159:2");
		digiCodeCorrespondence.put("S", "159:3"); digiCodeCorrespondence.put("T", "159:4");
		digiCodeCorrespondence.put("U", "159:5"); digiCodeCorrespondence.put("V", "159:6");
		digiCodeCorrespondence.put("W", "159:7"); digiCodeCorrespondence.put("X", "159:9");
		digiCodeCorrespondence.put("Y", "159:11"); digiCodeCorrespondence.put("Z", "159:10");
	}
	
	static public class BoyardPasswordConfigCommand implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(sender.hasPermission("boyardRoom.changepassword")) {
				if(args.length > 0) {
					String newPass = args[0].toUpperCase();
					ArrayList<String> tabCompare = new ArrayList<String>();
					for(int i = 0; i < newPass.length(); i++) {
						if(    newPass.charAt(i) != 'A' && newPass.charAt(i) != 'B' && newPass.charAt(i) != 'C' && newPass.charAt(i) != 'D'
							&& newPass.charAt(i) != 'E' && newPass.charAt(i) != 'F' && newPass.charAt(i) != 'G' && newPass.charAt(i) != 'H'
							&& newPass.charAt(i) != 'I' && newPass.charAt(i) != 'J' && newPass.charAt(i) != 'K' && newPass.charAt(i) != 'L'
							&& newPass.charAt(i) != 'M' && newPass.charAt(i) != 'N' && newPass.charAt(i) != 'O' && newPass.charAt(i) != 'P'
							&& newPass.charAt(i) != 'Q' && newPass.charAt(i) != 'R' && newPass.charAt(i) != 'S' && newPass.charAt(i) != 'T'
							&& newPass.charAt(i) != 'U' && newPass.charAt(i) != 'V' && newPass.charAt(i) != 'W' && newPass.charAt(i) != 'X'
							&& newPass.charAt(i) != 'Y' && newPass.charAt(i) != 'Z'
						){
							sender.sendMessage("§cErreur: Le mot de passe doit contenir uniquement des lettres et sans répétition.");
							return false;
						}
						if(tabCompare.contains(newPass.charAt(i)+"")) {
							sender.sendMessage("§cErreur: Le mot de passe doit contenir uniquement des lettres et sans répétition.");
							return false;
						}
						tabCompare.add(newPass.charAt(i)+"");
					}
					McBoyard.boyardRoomModule.changePassword(newPass);
					sender.sendMessage("§aLe mot de passe vient d'être changer pour : §c"+newPass);
					return true;
				}else {
					sender.sendMessage("§cErreur : /boyardpassword <password>");
					return false;
				}
			}else {
				sender.sendMessage("§cVous n'avez pas la permission d'éxecuter cette commande.");
				return false;
			}
		}
	}
}
