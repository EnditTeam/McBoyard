package eu.octanne.mcboyard.modules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChat implements Module {
	
	public StaffChat() {
		onEnable();
	}
	
	public void onEnable() {
		
	}
	
	public void onDisable() {
		
	}
	
	static public class SCCommand implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(sender.hasPermission("mcboyard.staffchat")) {
				if(args.length > 0) {
					String message = "";
					for(String str : args) {
						message += " "+str;
					}
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.hasPermission("mcboyard.staffchat"))
							p.sendMessage("§8[§e"+sender.getName()+"§8]§r"+message);
					}
					return true;
				}else {
					sender.sendMessage("§cUsage : /staffchat <message>");
					return false;
				}
			}else {
				sender.sendMessage("Cette commande est réservée au membre du staff.");
				return false;
			}
		}
	}
}
