package eu.octanne.mcboyard.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import eu.octanne.mcboyard.McBoyard;

public class Chrono {
	
	protected BossBar bossBar;
	
	protected boolean cancel = false;
	protected boolean isStart = false;
	protected int task;
	protected double sec = 0;
	protected int sec2 = 0;
	protected int min = 0;
	protected float prctArround = 0;
	
	public Chrono() {
		onEnable();
	}
	
	public void onEnable() {
		bossBar = Bukkit.createBossBar("Chronomètre", BarColor.YELLOW, BarStyle.SEGMENTED_10);
	}
	public void onDisable() {
		bossBar.removeAll();
	}
	
	public void chrono(int secondes) {
		sec = secondes;
		isStart = true;
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(McBoyard.instance, new Runnable() {

			@Override
			public void run() {
				if(sec == secondes) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						bossBar.addPlayer(p);
						p.sendTitle(ChatColor.GREEN+"C'est parti", "", 3, 12, 3);
					}
				}
				if(sec == 0 || cancel == true) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						bossBar.setTitle("Temps: "+min+" minute(s) "+sec2+" seconde(s)");
						p.sendTitle(ChatColor.RED+"Fin du Temps", "", 5, 20, 5);
						bossBar.removePlayer(p);
					}
					isStart = false;
					cancel = false;
					Bukkit.getScheduler().cancelTask(task);
				}
				double prct = sec/secondes;
				min = (int)sec/60;
				sec2 = (int)sec%60;
				bossBar.setTitle("Temps: "+min+" minute(s) "+sec2+" seconde(s)");
				bossBar.setProgress(prct);
				sec--;
			}
		}, 0, 20);
	}
	
	/*
	 * COMMANDS
	 */
	static public class ChronoStopCommand implements CommandExecutor{

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(McBoyard.chronoModule.isStart != true) {
				sender.sendMessage(ChatColor.RED+"Il n'y a pas de chrononomètre en cours !");
				return false;
			}else {
				sender.sendMessage(ChatColor.GREEN+"Le chrononomètre est arrêté.");
				McBoyard.chronoModule.cancel = true;
				return true;
			}		
		}
	}
	static public class ChronoCommand implements CommandExecutor{

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(McBoyard.chronoModule.isStart == false) {
				if(args.length == 1) {
					try {
						int sec = Integer.parseInt(args[0]);
						McBoyard.chronoModule.chrono(sec);
						return true;
					}
					catch(NumberFormatException e){
						sender.sendMessage(ChatColor.RED+"Entrer un nombre de secondes valide !");
						return false;
					}
				}else {
					sender.sendMessage(ChatColor.RED+"Erreur syntaxe: /chrono <sec>");
					return false;
				}
			}else {
				sender.sendMessage(ChatColor.RED+"Chrono déjà lancé, faites \"/chronostop\" pour l'arrêter.");
				return false;
			}
		}
	}
	static public class ChronoPersoCommand implements CommandExecutor{

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(McBoyard.chronoModule.isStart == false) {
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("10min")) {
						sender.sendMessage(ChatColor.GREEN+"Lancement du chrono 10 minutes.");
						McBoyard.chronoModule.chrono(600);
						return true;
					}
					if(args[0].equalsIgnoreCase("5min")) {
						sender.sendMessage(ChatColor.GREEN+"Lancement du chrono 5 minutes.");
						McBoyard.chronoModule.chrono(300);
						return true;
					}
					if(args[0].equalsIgnoreCase("1min")) {
						sender.sendMessage(ChatColor.GREEN+"Lancement du chrono 1 minutes.");
						McBoyard.chronoModule.chrono(60);
						return true;
					}else {
						sender.sendMessage("Erreur syntaxe: /chronop <1min/5min/10min>");
						return false;
					}
				}else {
					sender.sendMessage("Erreur syntaxe: /chronop <1min/5min/10min>");
					return false;
				}
			}else {
				sender.sendMessage(ChatColor.RED+"Chrono déjà lancé, faites \"/chronostop\" pour l'arrêter.");
				return false;
			}
		}
	}
	
}
