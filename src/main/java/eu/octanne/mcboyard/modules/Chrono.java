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
import org.bukkit.plugin.java.JavaPlugin;

import eu.octanne.mcboyard.McBoyard;

public class Chrono extends Module{
	
	public Chrono(JavaPlugin instance) {
		super(instance);
	}
	
	protected BossBar bossBar;
	
	protected boolean cancel;
	protected boolean isStart;
	protected int task;
	protected double sec;
	protected int sec2;
	protected int min;
	protected float prctArround;
	
	public void onEnable() {
		cancel = false;
		isStart = false;
		sec = 0;
		sec2 = 0;
		min = 0;
		prctArround = 0;
		
		bossBar = Bukkit.createBossBar("Chronomètre", BarColor.YELLOW, BarStyle.SEGMENTED_10);
		
		pl.getCommand("chrono").setExecutor(new ChronoCommand());
		pl.getCommand("pchrono").setExecutor(new ChronoPersoCommand());
		pl.getCommand("chronostop").setExecutor(new ChronoStopCommand());
	}
	public void onDisable() {
		bossBar.removeAll();
	}
	
	@SuppressWarnings("deprecation")
	public void chrono(int secondes, boolean preventionTitle) {
		if(isStart)return;
		sec = secondes;
		isStart = true;
		task = Bukkit.getScheduler().scheduleAsyncRepeatingTask(McBoyard.instance, new Runnable() {

			@Override
			public void run() {
				if(sec == secondes) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						bossBar.addPlayer(p);
						if(preventionTitle)p.sendTitle(ChatColor.GREEN+"C'est parti", "", 3, 12, 3);
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
	class ChronoStopCommand implements CommandExecutor{

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
	class ChronoCommand implements CommandExecutor{

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(McBoyard.chronoModule.isStart == false) {
				if(args.length == 1) {
					try {
						int sec = Integer.parseInt(args[0]);
						McBoyard.chronoModule.chrono(sec, true);
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
	class ChronoPersoCommand implements CommandExecutor{

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(McBoyard.chronoModule.isStart == false) {
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("10min")) {
						sender.sendMessage(ChatColor.GREEN+"Lancement du chrono 10 minutes.");
						McBoyard.chronoModule.chrono(600, true);
						return true;
					}
					if(args[0].equalsIgnoreCase("5min")) {
						sender.sendMessage(ChatColor.GREEN+"Lancement du chrono 5 minutes.");
						McBoyard.chronoModule.chrono(300, true);
						return true;
					}
					if(args[0].equalsIgnoreCase("1min")) {
						sender.sendMessage(ChatColor.GREEN+"Lancement du chrono 1 minutes.");
						McBoyard.chronoModule.chrono(60, true);
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
