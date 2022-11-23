package eu.octanne.mcboyard.modules;

import eu.octanne.mcboyard.McBoyard;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClapModule extends PlugModule {

	public ClapModule(JavaPlugin instance) {
		super(instance);
	}

	protected boolean cancel;
	protected boolean isStart;
	protected int task;
	protected int secTotal;
	protected String tickSound = "block.note_block.xylophone";
	protected String finalSound = "block.note_block.xylophone";
	protected float tickSoundPitch = 1.0f;
	protected float finalSoundPitch = 2.0f;
	protected float volume = 0.5f;

	public void onEnable() {
		cancel = false;
		isStart = false;
		secTotal = 0;
		pl.getCommand("clap").setExecutor(new ClapCommand());

		// CREATE DEFAULT CONFIG
		boolean modified = false;
		if (!McBoyard.config.isSet("clap.tickSound")) {
			McBoyard.config.set("clap.tickSound", "block.note_block.xylophone");
			modified = true;
		}
		if (!McBoyard.config.isSet("clap.finalSound")) {
			McBoyard.config.set("clap.finalSound", "block.note_block.xylophone");
			modified = true;
		}
		if (!McBoyard.config.isSet("clap.tickSoundPitch")) {
			McBoyard.config.set("clap.tickSoundPitch", 1.0f);
			modified = true;
		}
		if (!McBoyard.config.isSet("clap.finalSoundPitch")) {
			McBoyard.config.set("clap.finalSoundPitch", 2.0f);
			modified = true;
		}
		if (!McBoyard.config.isSet("clap.volume")) {
			McBoyard.config.set("clap.volume", 0.5f);
			modified = true;
		}
		if (modified) {
			try {
				McBoyard.config.save(McBoyard.fileConfig);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// LOAD VAR
		tickSound = McBoyard.config.getString("clap.tickSound");
		finalSound = McBoyard.config.getString("clap.finalSound");
		tickSoundPitch = (float)McBoyard.config.getDouble("clap.tickSoundPitch");
		finalSoundPitch = (float)McBoyard.config.getDouble("clap.finalSoundPitch");
		volume = (float)McBoyard.config.getDouble("clap.volume");
	}

	public void onDisable() {
	}

	public void sendToEveryone(String msg, String sound, float pitch) {
		McBoyard.instance.getLogger().info(msg);
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(msg);
			if (sound != null && sound != "" && volume != 0 && pitch != 0) {
				p.playSound(p.getLocation(), sound, volume, pitch);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void clap(int secondes) {
		if (isStart)
			return;
		secTotal = secondes;
		task = Bukkit.getScheduler().scheduleAsyncRepeatingTask(McBoyard.instance, new Runnable() {
			@Override
			public void run() {
				if (cancel == true || secTotal <= 0) {
					if (cancel)
						sendToEveryone(ChatColor.GOLD + "Clap Annulé", null, 0);
					else
						sendToEveryone(ChatColor.GOLD + "C'est partit !", finalSound, finalSoundPitch);
					isStart = false;
					cancel = false;
					Bukkit.getScheduler().cancelTask(task);
					return;
				}

				if (secTotal == secondes) {
					sendToEveryone(ChatColor.GOLD + "Le tournage va commencer (" + ChatColor.GOLD + secTotal + ChatColor.GOLD + " secondes) !",
								   tickSound, tickSoundPitch);
				}
				else {
					sendToEveryone("Démarrage dans " + ChatColor.GOLD + secTotal + ChatColor.RESET + ".",
								   tickSound, tickSoundPitch);
				}
				secTotal--;
			}
		}, 0, 20);
	}

	public void cancel() {
		cancel = true;
	}

	class ClapCommand implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if (McBoyard.clapModule.isStart == false) {
				int sec;
				if (args.length >= 1) {
					try {
						sec = Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "Nombre de secondes invalide.");
						return false;
					}
				}
				else {
					sec = 5;
				}
				McBoyard.clapModule.clap(sec);
				return true;
			}
			else {
				// Arrêter le clap
				McBoyard.clapModule.cancel();
				sender.sendMessage(ChatColor.RED + "Clap arrêté.");
				return true;
			}
		}
	}
}
