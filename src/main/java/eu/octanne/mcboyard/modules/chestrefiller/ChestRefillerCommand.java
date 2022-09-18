package eu.octanne.mcboyard.modules.chestrefiller;

import eu.octanne.mcboyard.McBoyard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ChestRefillerCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("mcboyard.chestrefiller") && sender instanceof Player) {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /chestrefiller <add|list|enroll> []");
                return false;
            } else {
                switch (args[0]) {
                    case "generate":
                        McBoyard.chestFillerModule.generateLoots();
                        sender.sendMessage("§aLoots générés !");
                        return true;
                    case "add":
                        // Get max, min and chance from args
                        if (args.length == 4) {
                            // Check if args are int
                            try {
                                int max = Integer.parseInt(args[1]);
                                int min = Integer.parseInt(args[2]);
                                int chance = Integer.parseInt(args[3]);
                                // Check if max > min
                                if (max > min) {
                                    // Check if chance is between 0 and 100
                                    if (chance >= 0 && chance <= 100) {
                                        // Si le joueur a un item dans la main
                                        if (((Player) sender).getInventory().getItemInMainHand() != null && !((Player) sender).getInventory().getItemInMainHand().getType().isAir()) {
                                            // Confirmer la création
                                            McBoyard.chestFillerModule.getLootEditor().addLootableItem((Player) sender, new LootableItem(((Player) sender).getInventory().getItemInMainHand(), chance, min, max));
                                            sender.sendMessage("§aLoot ajouté !");
                                            return true;
                                        } else {
                                            sender.sendMessage("§cVous devez avoir un item dans la main !");
                                            return false;
                                        }
                                    } else {
                                        sender.sendMessage("§cChance doit être compris entre 0 & 100");
                                        return false;
                                    }
                                } else {
                                    sender.sendMessage("§cMax doit être plus grand que min.");
                                    return false;
                                }
                            } catch (NumberFormatException e) {
                                sender.sendMessage("§cMax, min & chance doivent être des nombres.");
                                return false;
                            }
                        } else {
                            sender.sendMessage("§cUsage: /chestrefiller add <max> <min> <chance>");
                            return false;
                        }
                    case "list":
                        // If enroll chest or lootable items
                        if (args.length == 2) {
                            switch (args[1]) {
                                case "chest":
                                    McBoyard.chestFillerModule.getLootEditor().showEnrollChests((Player) sender);
                                    return true;
                                case "item":
                                    McBoyard.chestFillerModule.getLootEditor().showLootableItems((Player) sender);
                                    return true;
                                default:
                                    sender.sendMessage("§cUsage: /chestrefiller list <chest|item>");
                                    return false;
                            }
                        } else {
                            sender.sendMessage("§cUsage: /chestrefiller list <chest|item>");
                            return false;
                        }
                    case "enroll":
                        McBoyard.chestFillerModule.getLootEditor().enrollChest((Player) sender);
                        return true;
                    default:
                        sender.sendMessage("§cUsage: /chestrefiller <add|list|enroll> []");
                        return false;
                }
            }
        } else {
            sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande !");
            return false;
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> cmds = new ArrayList<String>();
        if (args.length == 1) {
            cmds.add("add");
            cmds.add("list");
            cmds.add("enroll");
            cmds.add("generate");
        } else if (args.length == 2) {
            if (args[0].equals("list")) {
                cmds.add("chest");
                cmds.add("item");
            }
        }
        return cmds;
    }
}
