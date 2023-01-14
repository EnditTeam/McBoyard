package eu.octanne.mcboyard.modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import eu.octanne.mcboyard.McBoyard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoMessage extends PlugModule implements Listener {

	protected File fileMessage;
	protected static YamlConfiguration configMsg;
	
	public static ArrayList<String> messages;
	public static int interval;
	public static int minPlayers;

	public static String header, footer;
	
	static protected int task = -1;
	
	public AutoMessage(JavaPlugin instance) {
		super(instance);
	}
	
	@SuppressWarnings("unchecked")
	public void onEnable() {
		
		minPlayers = 8;
		interval = 300;
		messages = new ArrayList<String>();
		fileMessage = new File(McBoyard.folderPath+"/message.yml");
		
		/*
		 * GET MESSAGE ON FILE
		 */
		configMsg = YamlConfiguration.loadConfiguration(fileMessage);
		//IF MESSAGE DON'T EXIST
		if(!fileMessage.exists()) {
			try {
				fileMessage.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!configMsg.isSet("interval"))configMsg.set("interval", interval);
		if(!configMsg.isSet("min-players"))configMsg.set("min-players", minPlayers);
		if(!configMsg.isSet("messages"))configMsg.set("messages", new ArrayList<String>());
		if(!configMsg.isSet("tablist.header"))configMsg.set("tablist.header", "Welcome on McBoyard !");
		if(!configMsg.isSet("tablist.footer"))configMsg.set("tablist.footer", "Enjoy your stay !");

		try {
			configMsg.save(fileMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//GET MESSAGE AND PARA
		interval = configMsg.getInt("interval");
		minPlayers = configMsg.getInt("min-players");
		messages = (ArrayList<String>) configMsg.get("messages");
		header = configMsg.getString("tablist.header");
		footer = configMsg.getString("tablist.footer");
		//EVENT REGISTER
		Bukkit.getPluginManager().registerEvents(this, McBoyard.instance);
		if(canRun() && !isRunning()) {
			launchMessageScheduler();
		}
		
		pl.getCommand("amsgreload").setExecutor(new ReloadCommand());
	}

	public void onDisable() {
		stopMessageScheduler();
	}

	@SuppressWarnings("unchecked")
	static public void reloadMessage() {
		interval = configMsg.getInt("interval");
		minPlayers = configMsg.getInt("min-players");
		messages = (ArrayList<String>) configMsg.get("messages");
		if(canRun() && !isRunning()) {
			launchMessageScheduler();
		}
	}
	
	/*
	 * EVENTS LISTENER
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if(canRun() && !isRunning()) {
			launchMessageScheduler();
		}
		// SET TAB LIST HEADER AND FOOTER
		e.getPlayer().setPlayerListHeaderFooter(header, footer);
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(!canRun()) {
			stopMessageScheduler();
		}
	}
	
	public static boolean canRun() {
		return Bukkit.getOnlinePlayers().size() >= minPlayers;
	}

	public static boolean isRunning() {
		return task != -1;
	}
	
	/*
	 * MESSAGE SCHEDULER
	 */
	static public void launchMessageScheduler() {
		if(!messages.isEmpty() && !isRunning()) {
			Bukkit.getScheduler().cancelTask(task);
			task = Bukkit.getScheduler().scheduleSyncRepeatingTask(McBoyard.instance, new Runnable() {
				
				int msgNumber = 0;
				
				@Override
				public void run() {
					if(msgNumber >= messages.size() ) {
						msgNumber = 0;
						Bukkit.broadcastMessage(messages.get(msgNumber));
					}else {
						Bukkit.broadcastMessage(messages.get(msgNumber));
					}
					msgNumber++;
				}
				
			}, 0, 20*interval);
		}
	}

	static public void stopMessageScheduler() {
		Bukkit.getScheduler().cancelTask(task);
		task = -1;
	}
	
	/*
	 * COMMANDS
	 */
	class ReloadCommand implements CommandExecutor{
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			reloadMessage();
			sender.sendMessage(ChatColor.RED+"[AutoMessage] §6Rechargement des fichiers terminé !");
			return true;
		}
	}
}
