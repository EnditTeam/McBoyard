package eu.octanne.mcboyard.modules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_12_R1.PacketPlayOutGameStateChange;

public class CreditModule {
	public CreditModule() {
		onEnable();
	}

	public void onEnable() {

	}

	public void onDisable() {

	}

	static public class CreditCommand implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(sender.hasPermission("mcboyard.credit")) {
				if(args.length >= 1) {
					if(args[0].equalsIgnoreCase("all")) {
						for(Player p : Bukkit.getOnlinePlayers()) {
							((CraftPlayer) p).getHandle().playerConnection.sendPacket(
									new PacketPlayOutGameStateChange(4, 1));
						}
						sender.sendMessage("§6Lancement des crédits pour tous.");
						return true;
					}else {
						Player p = Bukkit.getPlayer(args[0]);
						if(p != null) {
							((CraftPlayer) p).getHandle().playerConnection.sendPacket(
									new PacketPlayOutGameStateChange(4, 1));
							sender.sendMessage("§6Lancement des crédits pour "+args[0]);
							return true;
						}else {
							sender.sendMessage("§cErreur : le joueur "+args[0]+" est introuvable.");
							return false;
						}
					}
				}else {
					sender.sendMessage("§cUsage : /credit <player or all>");
					return false;
				}
			}else {
				sender.sendMessage("Vous n'avez pas la permission d'exécuter cette commande.");
				return false;
			}
		}
	}
}
