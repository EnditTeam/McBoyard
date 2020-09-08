package eu.octanne.mcboyard.modules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

public class Maitre implements Module {
	
	static public Scoreboard scoreboard;
	
	public Maitre() {
		onEnable();
	}
	
	public void onEnable() {
		scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		if(scoreboard.getTeam("Maitre") != null) {
			scoreboard.getTeam("Maitre").unregister();
		}
		scoreboard.registerNewTeam("Maitre");
		scoreboard.getTeam("Maitre").setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
	}
	public void onDisable() {
		scoreboard.getTeam("Maitre").unregister();
	}
	
	static public class MaitreCommand implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(args.length < 1 && !(sender instanceof Player)) {
				sender.sendMessage("Veulliez préciser un joueur : /Maitre <joueur>");
				return false;
			}else if(sender instanceof Player && sender.hasPermission("fortboyard.maitre") && args.length < 1){
				if(scoreboard.getTeam("Maitre").hasEntry(sender.getName())) {
					scoreboard.getTeam("Maitre").removeEntry(sender.getName());
					sender.sendMessage("§8Vous venez de quitter les maîtres du temps.");
					return true;
				}else {
					scoreboard.getTeam("Maitre").addEntry(sender.getName());
					sender.sendMessage("§8Vous êtes desormais maître du temps.");
					return true;
				}
			}else if(args.length > 0 && sender.hasPermission("fortboyard.maitre")){
				if(Bukkit.getPlayer(args[0]) != null) {
					if(scoreboard.getTeam("Maitre").hasEntry(args[0])) {
						scoreboard.getTeam("Maitre").removeEntry(args[0]);
						Bukkit.getPlayer(args[0]).sendMessage("§8Vous venez de quitter les maîtres du temps.");
						sender.sendMessage("§e"+args[0]+" §8viens de quitter les maîtres du temps.");
						return true;
					}else {
						scoreboard.getTeam("Maitre").addEntry(args[0]);
						Bukkit.getPlayer(args[0]).sendMessage("§8Vous êtes desormais maître du temps.");
						sender.sendMessage("§e"+args[0]+" §8est desormais un maître du temps.");
						return true;
					}
				}else {
					sender.sendMessage("§cVeulliez préciser un joueur valide !");
					return false;
				}
			}else {
				sender.sendMessage("Vous n'avez pas la permission d'éxecuter cette commande.");
				return false;
			}
		}

	}
}
