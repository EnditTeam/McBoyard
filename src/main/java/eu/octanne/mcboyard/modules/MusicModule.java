package eu.octanne.mcboyard.modules;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.netty.buffer.Unpooled;

public class MusicModule extends Module {

	public MusicModule(JavaPlugin instance) {
		super(instance);
	}

	public void onEnable() {
		pl.getCommand("music").setExecutor(new MusicCommand());
	}

	public void onDisable() {

	}

	class MusicCommand implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(sender.hasPermission("mcboyard.music")) {
				if(args.length > 1 && args[0].equalsIgnoreCase("start")) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						PacketPlayOutCustomSoundEffect packet = 
								new PacketPlayOutCustomSoundEffect(new MinecraftKey(args[1]), SoundCategory.VOICE, new Vec3D(1000, 64, 1000), 500.0f, 1);
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
								new PacketPlayOutCustomPayload(new MinecraftKey("MC|StopSound"), (PacketDataSerializer)localObject));
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
