package eu.octanne.mcboyard.modules;

import eu.octanne.mcboyard.McBoyard;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class McBoyardModule extends PlugModule implements CommandExecutor, TabCompleter {
	public McBoyardModule(JavaPlugin instance) {
		super(instance);
	}

	public void onEnable() {
		pl.getCommand("mcboyard").setExecutor(this);
		pl.getCommand("mcboyard").setTabCompleter(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("mcboyard.mcboyard")) {
			sender.sendMessage("Vous n'avez pas la permission d'utiliser cette commande.");
			return false;
		}

		if (args.length > 0) {
			String action = args[0];
			if (action.equalsIgnoreCase("reload")) {

				String moduleName = args.length > 1 ? args[1] : null;
				PlugModule plugModule = moduleName != null ? getModule(moduleName) : null;
				if (moduleName == null) {
					pl.reloadConfig();
					onDisable();
					onEnable();
					sender.sendMessage("§aMcBoyard rechargé.");
					return true;
				}
				else if (plugModule != null) {
					plugModule.onDisable();
					plugModule.onEnable();
					sender.sendMessage("§aModule §e" + moduleName + "§a rechargé.");
					return true;
				}
				else {
					sender.sendMessage("§cModule §e" + moduleName + "§c introuvable.");
					return false;
				}
			}
		}
		sender.sendMessage("§cUsage : /mcboyard <action> (...)");
		return false;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		if (!sender.hasPermission("mcboyard.mcboyard")) {
			return null;
		}

		if (args.length == 1) {
			return List.of("reload");
		}

		if (args[0].equalsIgnoreCase("reload")) {
			if (args.length == 2) {
				return List.of("counterPieceModule",
							   "chronoModule",
							   "kitModule",
							   "noChangeWeatherModule",
							   "autoMessageModule",
							   "maitreModule",
							   "boyardRoomModule",
							   "staffChatModule",
							   "musicModule",
							   "creditModule",
							   "tropheesModule",
							   "mcboyardModule",
							   "chestFillerModule",
							   "excaliburModule");
			}
		}
		return null;
	}

	public PlugModule getModule(String name) {
		switch (name) {
		case "counterPieceModule":
			return McBoyard.counterPieceModule;
		case "chronoModule":
			return McBoyard.chronoModule;
		case "kitModule":
			return McBoyard.kitModule;
		case "noChangeWeatherModule":
			return McBoyard.noChangeWeatherModule;
		case "autoMessageModule":
			return McBoyard.autoMessageModule;
		case "maitreModule":
			return McBoyard.maitreModule;
		case "boyardRoomModule":
			return McBoyard.boyardRoomModule;
		case "staffChatModule":
			return McBoyard.staffChatModule;
		case "musicModule":
			return McBoyard.musicModule;
		case "creditModule":
			return McBoyard.creditModule;
		case "tropheesModule":
			return McBoyard.tropheesModule;
		case "mcboyardModule":
			return McBoyard.mcboyardModule;
		case "chestFillerModule":
			return McBoyard.chestFillerModule;
		case "excaliburModule":
			return McBoyard.excaliburModule;
		default:
			return null;
		}
	}
}
