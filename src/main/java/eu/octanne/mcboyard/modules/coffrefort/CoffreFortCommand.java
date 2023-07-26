package eu.octanne.mcboyard.modules.coffrefort;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import eu.octanne.mcboyard.McBoyard;

public class CoffreFortCommand implements CommandExecutor, TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("mcboyard.coffrefort"))
            return null;
        if (args.length == 1) {
            return Arrays.asList("infinit", "code", "start", "stop", "reset");
        }

        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
            @NotNull String[] args) {
        if (!sender.hasPermission("mcboyard.coffrefort"))
            return false;

        if (args.length == 0) {
            sender.sendMessage("§c/coffrefort <infinit|code|start|stop|reset>");
            return false;
        }

        switch (args[0]) {
            case "infinit":
            case "code": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cVous devez être un joueur pour exécuter cette commande.");
                    return false;
                }
                Player player = (Player) sender;
                if (args[0].equals("infinit"))
                    player.getInventory().addItem(CoffreCodeItem.createInfinitCode().getItem());
                else
                    player.getInventory().addItem(CoffreCodeItem.createCode().getItem());
                break;
            }
            case "start":
                McBoyard.coffreFortModule.startAnimation();
                sender.sendMessage("Salle Le Coffre démarrée.");
                break;
            case "stop":
                McBoyard.coffreFortModule.stopAnimation();
                sender.sendMessage("Salle Le Coffre terminée.");
                break;
            case "reset":
                McBoyard.coffreFortModule.reset();
                sender.sendMessage("Salle Le Coffre réinitialisée.");
                break;
            default:
                sender.sendMessage("§c/coffrefort <infinit|code|start|stop|reset>");
                return false;
        }

        return true;
    }

}
