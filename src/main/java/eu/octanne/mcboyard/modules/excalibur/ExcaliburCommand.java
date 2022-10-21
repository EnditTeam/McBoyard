package eu.octanne.mcboyard.modules.excalibur;

import eu.octanne.mcboyard.entity.ExcaliburStand;
import eu.octanne.mcboyard.modules.ExcaliburSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ExcaliburCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission("mcboyard.excalibur")) {
            // if args is empty help
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /excalibur help, pour l'aide.");
                return true;
            }
            if (args[0].equalsIgnoreCase("spawn")) {
                if (args.length == 2) {
                    try {
                        int nbDurability = Integer.parseInt(args[1]);
                        // spawn excalibur
                        ExcaliburStand.spawn(((Player) sender).getLocation(), nbDurability);
                        sender.sendMessage("§aExcalibur spawn avec succès !");
                        return true;
                    } catch (NumberFormatException e) {}
                }

                sender.sendMessage("§cUsage: /excalibur spawn <nbDurability>, pour spawn un stand d'excalibur.");
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                sender.sendMessage("§cListe des stands chargées d'excalibur:");
                for (ExcaliburStand stand : ExcaliburSystem.getExcaliburStands()) {
                    // Afficher infos du stand : Nom, Durabilité, Position
                    sender.sendMessage("§c" + stand.getStandName() + "(" + stand.getStandId() + ")");
                    sender.sendMessage("   §cDurabilité: " + stand.getNbSwordDurability());
                    sender.sendMessage("   §cPosition: " + stand.getStandLocation());
                }
                return true;
            } else if (args[0].equalsIgnoreCase("despawn")) {
                // check if args[1] is a number
                if (args.length == 2) {
                    try {
                        int numStand = Integer.parseInt(args[1]);
                        // despawn excalibur
                        if (ExcaliburSystem.getExcaliburStands().size() > numStand) {
                            ExcaliburStand.despawn(numStand);
                            sender.sendMessage("§aExcalibur despawn avec succès !");
                            return true;
                        } else {
                            sender.sendMessage("§cCe stand n'existe pas !");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Le numéro du stand doit être un nombre !");
                        return true;
                    }
                }

                sender.sendMessage("§cUsage: /excalibur despawn <numStand>, pour despawn un stand d'excalibur.");
                return true;
            } else if (args[0].equalsIgnoreCase("put")) {
                //  check if args[1] is a number
                if (args.length == 2) {
                    try {
                        int numStand = Integer.parseInt(args[1]);
                        // put sword in excalibur
                        if (ExcaliburSystem.getExcaliburStands().size() > numStand) {
                            ExcaliburStand stand = ExcaliburSystem.getExcaliburStands().get(numStand);
                            stand.putBackSword();
                            sender.sendMessage("§aExcalibur remis sur " + stand.getStandName() + " avec succès !");
                            return true;
                        } else {
                            sender.sendMessage("§cCe stand n'existe pas !");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Le numéro du stand doit être un nombre !");
                        return true;
                    }
                }

                sender.sendMessage("§cUsage: /excalibur put <numStand>, pour remettre l'épée sur un stand d'excalibur.");
                return true;
            } else if (args[0].equalsIgnoreCase("putall")) {
                // put all sword in excalibur
                for (ExcaliburStand stand : ExcaliburSystem.getExcaliburStands()) {
                    stand.putBackSword();
                }
                sender.sendMessage("§aExcalibur remis sur tous les stands avec succès !");
                return true;
            } else if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("§cListe des commandes:");
                sender.sendMessage("§c/excalibur spawn <nbDurability>, pour spawn un stand d'excalibur.");
                sender.sendMessage("§c/excalibur list, pour lister les stands d'excalibur.");
                sender.sendMessage("§c/excalibur despawn <numStand>, pour despawn un stand d'excalibur.");
                sender.sendMessage("§c/excalibur put <numStand>, pour remettre l'épée sur un stand d'excalibur.");
                sender.sendMessage("§c/excalibur putall, pour remettre l'épée sur tous les stands d'excalibur.");
                return true;
            } else {
                sender.sendMessage("§cUsage: /excalibur help, pour l'aide.");
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
            cmds.add("list");
            cmds.add("despawn");
            cmds.add("help");
        } else if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("spawn")) {
                cmds.add("<nbDurability>");
            } else if (strings[0].equalsIgnoreCase("despawn")) {
                cmds.add("<numStand>");
            }
        }

        return cmds;
    }
}