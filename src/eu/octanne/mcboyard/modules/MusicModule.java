package eu.octanne.mcboyard.modules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
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
				}else if(args.length >= 1 && args[0].equalsIgnoreCase("stop")){
					String sound = args.length > 1 ? args[1] : "all";
					if(sound.equals("all")) {
						for(Player p : Bukkit.getOnlinePlayers()) {
							Object localObject = new PacketDataSerializer(Unpooled.buffer());
							((PacketDataSerializer)localObject).a("");
							((PacketDataSerializer)localObject).a("");
							((CraftPlayer) p).getHandle().playerConnection.sendPacket(
								new PacketPlayOutCustomPayload("MC|StopSound", (PacketDataSerializer)localObject));
						}
						sender.sendMessage("§9Arret de tous les sons en cours !");
					}else {
						sender.sendMessage("§9Arret du son : §e"+sound);
						for(Player p : Bukkit.getOnlinePlayers()) {
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
