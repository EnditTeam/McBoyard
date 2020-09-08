package eu.octanne.mcboyard.modules;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_12_R1.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.SoundCategory;

public class MusicModule implements Module {

	public MusicModule() {
		onEnable();
	}

	public void onEnable() {

	}

	public void onDisable() {

	}

	static public class MusicCommand implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(sender.hasPermission("mcboyard.music")) {
				if(args.length > 1 && args[0].equalsIgnoreCase("start")) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						PacketPlayOutCustomSoundEffect packet = 
								new PacketPlayOutCustomSoundEffect(args[1], SoundCategory.MASTER, 1000, 64, 1000, 500.0f, 1);
						PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
						connection.sendPacket(packet);
					}
					sender.sendMessage("§aLancement de la musique "+args[1]+" !");
					return true;
				}else if(args.length > 1 && args[0].equalsIgnoreCase("stop")){
					/*ByteBuf buf = Unpooled.buffer(256);
					PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|StopSound", new PacketDataSerializer(buf));
					for(Player p : Bukkit.getOnlinePlayers()) {
						PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
						connection.sendPacket(packet);
						//p.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stopsound @a");
					}*/
					for(Player p : Bukkit.getOnlinePlayers()) {
						String sound = args[1];
						if(sound.equals("all")) {
							for(Sound soundS : Sound.values()) {
								p.stopSound(soundS);
							}
							sender.sendMessage("§9Arret de tous les sons en cours !");
						}else {
							sender.sendMessage("§9Arret du son : §e"+sound);
							p.stopSound(sound);
						}
					}
					return true;
				}else {
					sender.sendMessage("§cUsage : /music <stop|start> [<music>]");
					return false;
				}
			}else {
				sender.sendMessage("Tu n'as pas le drois d'éxecuter cette commande.");
				return false;
			}
		}
	}
}
