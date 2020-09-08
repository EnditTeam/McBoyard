package eu.octanne.mcboyard.modules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.SoundCategory;
import net.minecraft.server.v1_12_R1.SoundEffect;

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
				}else if(args.length > 0 && args[0].equalsIgnoreCase("stop")){
					for(Player p : Bukkit.getOnlinePlayers()) {
						p.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stopsound @a");
					}
					sender.sendMessage("§aArret des sons en cours !");
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
