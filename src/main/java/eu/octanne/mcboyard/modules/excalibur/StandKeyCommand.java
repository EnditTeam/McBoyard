package eu.octanne.mcboyard.modules.excalibur;

import eu.octanne.mcboyard.entity.ExcaliburStand;
import eu.octanne.mcboyard.modules.ExcaliburSystem;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StandKeyCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission("mcboyard.excalibur")) {
            // if args is empty help
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /standkey help, pour l'aide.");
                return true;
            }
            if (args[0].equalsIgnoreCase("spawn")) {
                // apparition du standKey
                Location loc = ((Player) sender).getLocation();
                Optional<StandKey> standKey = StandKey.createStandKey(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
                if (standKey.isPresent()) {
                    // afficher l'ID et location
                    sender.sendMessage(String.format("§aStandKey spawn à §6%.2f %.2f %.2f §aID: §c%s", loc.getX(), loc.getY(), loc.getZ(), standKey.get().getID().toString()));
                } else {
                    sender.sendMessage("§cErreur lors de la création du StandKey !");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                sender.sendMessage("§cListe des StandKeys chargés:");
                for (StandKey stand : StandKey.getStandKeys()) {
                    // Afficher infos du stand : ID, Position
                    if (stand.isComplete()) {
                        sender.sendMessage("§6StandKey : §aComplet");
                        sender.sendMessage("§cID: " + stand.getID());
                        Location loc = stand.getBukkitLocation();
                        sender.sendMessage(String.format("§cPosition: %.2f %.2f %.2f", loc.getX(),loc.getY(),loc.getZ()));
                    } else {
                        sender.sendMessage("§6StandKey : §aIncomplet");
                        sender.sendMessage("§cID: " + stand.getID());
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("despawn")) {
                if (args.length > 1) {
                    try {
                        UUID id = UUID.fromString(args[1]);
                        if (StandKey.removeStandKey(id)) {
                            sender.sendMessage("§aStandKey supprimé !");
                        } else {
                            sender.sendMessage("§cStandKey introuvable !");
                        }

                        return true;
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("§cErreur l'ID est invalide !");
                        return true;
                    }
                }

                sender.sendMessage("§cUsage: /standkey despawn <ID>, pour despawn un StandKey.");
                return true;
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (args.length > 1) {
                    try {
                        UUID id = UUID.fromString(args[1]);
                        Optional<StandKey> standKey = StandKey.getStandKey(id);
                        if (standKey.isPresent()) {
                            standKey.get().reset();
                            sender.sendMessage("§aStandKey reset !");
                        } else {
                            sender.sendMessage("§cStandKey introuvable !");
                        }

                        return true;
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("§cErreur l'ID est invalide !");
                        return true;
                    }
                }

                // Message usage
                sender.sendMessage("§cUsage: /standkey reset <ID>, pour reset un StandKey.");
                return true;
            } else if (args[0].equalsIgnoreCase("resetall")) {
                // reset tous les standKeys
                for (StandKey stand : StandKey.getStandKeys()) {
                    stand.reset();
                }

                // Message success
                sender.sendMessage("§aStandKeys reset !");
                return true;
            } else if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("§cListe des commandes:");
                sender.sendMessage("§c/standkey spawn, pour spawn un StandKey.");
                sender.sendMessage("§c/standkey list, pour lister les StandKeys.");
                sender.sendMessage("§c/standkey despawn <ID>, pour despawn un StandKey.");
                sender.sendMessage("§c/standkey reset <ID>, pour reset un StandKey.");
                sender.sendMessage("§c/standkey resetall, pour reset tous les StandKeys.");
                return true;
            } else {
                sender.sendMessage("§cUsage: /standkey help, pour l'aide.");
                return true;
            }
        } else {
            sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> cmds = new ArrayList<>();
        if (strings.length == 1) {
            cmds.add("spawn");
            cmds.add("despawn");
            cmds.add("list");
            cmds.add("help");
            cmds.add("reset");
            cmds.add("resetall");
        } else if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("despawn") || strings[0].equalsIgnoreCase("reset")) {
                cmds.add("<numStand>");
                for (StandKey stand : StandKey.getStandKeys()) {
                    cmds.add(stand.getID().toString());
                }
            }
        }

        return cmds;
    }
}