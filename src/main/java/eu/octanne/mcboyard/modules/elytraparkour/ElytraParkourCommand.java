package eu.octanne.mcboyard.modules.elytraparkour;

import eu.octanne.mcboyard.modules.ElytraParkourModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ElytraParkourCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!commandSender.hasPermission("mcboyard.elytraparkour")) {
            commandSender.sendMessage("§cVous n'avez pas la permission.");
            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage("Liste des sous commandes :");
            commandSender.sendMessage("§c/elytraparkour join [player]");
            commandSender.sendMessage("§c/elytraparkour leave [player]");
            commandSender.sendMessage("§c/elytraparkour resetPlayer [player]");
            commandSender.sendMessage("§c/elytraparkour config");
            commandSender.sendMessage("§c/elytraparkour list");
            commandSender.sendMessage("§c/elytraparkour add <duration>");
            commandSender.sendMessage("§c/elytraparkour remove <num>");
            commandSender.sendMessage("§c/elytraparkour setStartLocation");
            commandSender.sendMessage("§c/elytraparkour setDefaultDuration <duration>");
            commandSender.sendMessage("§c/elytraparkour setDistanceFromRing <distance>");
            commandSender.sendMessage("§c/elytraparkour setDistanceFromRingY <distance>");
            commandSender.sendMessage("§c/elytraparkour setDistanceFromStart <distance>");
            return false;
        }
        else {
            if (args[0].equalsIgnoreCase("setDistanceFromRingY")) {
                if (args.length >= 2) {
                    try {
                        ElytraParkourModule.distanceFromRingY = Double.parseDouble(args[1]);
                        commandSender.sendMessage("§cLa distance par défaut en Y a été modifiée.");
                        return true;
                    }
                    catch (Exception e) {
                        commandSender.sendMessage("§cLa distance n'est pas valide.");
                        return false;
                    }
                }
                else {
                    commandSender.sendMessage("§c/elytraparkour setDistanceFromRingY <distance>");
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("setDistanceFromStart")) {
                if (args.length >= 2) {
                    try {
                        ElytraParkourModule.distanceFromStart = Double.parseDouble(args[1]);
                        commandSender.sendMessage("§cLa distance du départ a été modifiée.");
                        return true;
                    }
                    catch (Exception e) {
                        commandSender.sendMessage("§cLa distance n'est pas valide.");
                        return false;
                    }
                }
                else {
                    commandSender.sendMessage("§c/elytraparkour setDistanceFromStart <distance>");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("setDistanceFromRing")) {
                if (args.length >= 2) {
                    try {
                        ElytraParkourModule.distanceFromRing = Double.parseDouble(args[1]);
                        commandSender.sendMessage("§cLa distance par défaut a été modifiée.");
                        return true;
                    }
                    catch (Exception e) {
                        commandSender.sendMessage("§cLa distance n'est pas valide.");
                        return false;
                    }
                }
                else {
                    commandSender.sendMessage("§c/elytraparkour setDistanceFromRing <distance>");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("setDefaultDuration")) {
                if (args.length >= 2) {
                    try {
                        int duration = Integer.parseInt(args[1]);
                        ElytraParkourModule.defaultDuration = duration;
                        commandSender.sendMessage("§cLa durée par défaut a été modifiée.");
                        return true;
                    }
                    catch (Exception e) {
                        commandSender.sendMessage("§cLa durée n'est pas valide.");
                        return false;
                    }
                }
                else {
                    commandSender.sendMessage("§c/elytraparkour setDefaultDuration <duration>");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("setStartLocation")) {
                // check if player
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    ElytraParkourModule.startLocation = player.getLocation();
                    commandSender.sendMessage("§cLa position de départ a été modifiée.");
                    return true;
                }
                else {
                    commandSender.sendMessage("§cVous devez être un joueur pour utiliser cette commande.");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("list")) {
                commandSender.sendMessage("Liste des anneaux :");
                for (int i = 0; i < ElytraParkourModule.ringsLocation.size(); i++) {
                    commandSender.sendMessage("§c" + i + " : " + ElytraParkourModule.ringsLocation.get(i).toString());
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("config")) {
                commandSender.sendMessage("§cConfiguration du module ElytraParkour :");
                commandSender.sendMessage("§cdefaultDuration : " + ElytraParkourModule.defaultDuration);
                commandSender.sendMessage("§cdistanceFromRing : " + ElytraParkourModule.distanceFromRing);
                commandSender.sendMessage("§cdistanceFromRingY : " + ElytraParkourModule.distanceFromRingY);
                commandSender.sendMessage("§cdistanceFromStart : " + ElytraParkourModule.distanceFromStart);
                Location locStart = ElytraParkourModule.startLocation;
                String startLocStr = locStart.getWorld().getName() + " " + locStart.getX() + " " + locStart.getY() + " " + locStart.getZ();
                commandSender.sendMessage("§cstartLocation : " + startLocStr);
                return true;
            }
            else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length >= 2) {
                    try {
                        int num = Integer.parseInt(args[1]);
                        if (num < ElytraParkourModule.ringsLocation.size()) {
                            ElytraParkourModule.ringsLocation.remove(num);
                            commandSender.sendMessage("§cL'anneau n°" + num + " a été supprimé.");
                            return true;
                        }
                        else {
                            commandSender.sendMessage("§cLe numéro d'anneau n'est pas valide.");
                            return false;
                        }
                    }
                    catch (Exception e) {
                        commandSender.sendMessage("§cLe numéro d'anneau n'est pas valide.");
                        return false;
                    }
                }
                else {
                    commandSender.sendMessage("§c/elytraparkour remove <num>");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("add")) {
                // check if player
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    if (args.length >= 2) {
                        // Check block target by player
                        if (player.getTargetBlock(null, 10).getType().name().contains("AIR")) {
                            commandSender.sendMessage("§cVous devez regarder un bloc.");
                            return false;
                        }
                        else {
                            // Check if block is not already a ring
                            for (ElytraRing ring : ElytraParkourModule.ringsLocation) {
                                if (ring.getLocation().equals(player.getTargetBlock(null, 10).getLocation())) {
                                    commandSender.sendMessage("§cCe bloc est déjà un anneau.");
                                    return false;
                                }
                            }
                            // check if duration is a number
                            try {
                                int duration = Integer.parseInt(args[1]);
                                Location loc = player.getTargetBlock(null, 10).getLocation();
                                // center location in the middle of the block
                                loc.add(0.5, 0.5, 0.5);
                                ElytraParkourModule.ringsLocation.add(new ElytraRing(loc, duration));
                                commandSender.sendMessage("§cL'anneau a été ajouté.");
                                return true;
                            }
                            catch (Exception e) {
                                commandSender.sendMessage("§cLa durée n'est pas valide.");
                                return false;
                            }
                        }
                    }
                    else {
                        commandSender.sendMessage("§c/elytraparkour add <duration>");
                        return false;
                    }
                }
                else {
                    commandSender.sendMessage("§cVous devez être un joueur pour utiliser cette commande.");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("resetPlayer")) {
                // check if a player is specified or if sender is a player
                Player player = getPlayer(commandSender, args);

                if (player != null) {
                    if (ElytraParkourModule.playersInParkour.contains(player)) {
                        ElytraParkourModule.resetPlayer(player, commandSender);
                        return true;
                    }
                    else {
                        commandSender.sendMessage("§cLe joueur " + player.getName() + " n'est pas dans le parcours.");
                        return false;
                    }
                }
                else {
                    commandSender.sendMessage("§c/elytraparkour resetPlayer <player>");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("join")) {
                // check if a player is specified or if sender is a player
                Player player = getPlayer(commandSender, args);

                if (player != null) {
                    if (!ElytraParkourModule.playersInParkour.contains(player)) {
                        ElytraParkourModule.playersInParkour.add(player);
                        // add elytra with default duration to player
                        ItemStack elytra = new ItemStack(Material.ELYTRA);
                        Damageable meta = (Damageable) elytra.getItemMeta();
                        meta.setDamage(432-ElytraParkourModule.defaultDuration);
                        elytra.setItemMeta((ItemMeta) meta);
                        player.getInventory().setChestplate(elytra);

                        commandSender.sendMessage("§cLe joueur " + player.getName() + " a été ajouté au parcours.");
                        return true;
                    }
                    else {
                        commandSender.sendMessage("§cLe joueur " + player.getName() + " est déjà dans le parcours.");
                        return false;
                    }
                }
                else {
                    commandSender.sendMessage("§c/elytraparkour join <player>");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("leave")) {
                // check if a player is specified or if sender is a player
                Player player = getPlayer(commandSender, args);

                // check if a player is specified or if sender is a player
                if (player != null) {
                    if (ElytraParkourModule.playersInParkour.contains(player)) {
                        ElytraParkourModule.playersInParkour.remove(player);
                        // remove elytra from player
                        player.getInventory().setChestplate(null);
                        commandSender.sendMessage("§cLe joueur " + player.getName() + " a été retiré du parcours.");
                        return true;
                    }
                    else {
                        commandSender.sendMessage("§cLe joueur " + player.getName() + " n'est pas dans le parcours.");
                        return false;
                    }
                }
                else {
                    commandSender.sendMessage("§c/elytraparkour leave <player>");
                    return false;
                }
            }
            else {
                // Ecrire que la sous commande n'existe pas
                commandSender.sendMessage("§cLa sous commande exécutée n'existe pas.");
                return false;
            }
        }
    }

    @Nullable
    private static Player getPlayer(@NotNull CommandSender commandSender, @NotNull String @NotNull [] args) {
        Player player = null;
        if (args.length >= 2) {
            player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                commandSender.sendMessage("§cLe joueur " + args[1] + " n'est pas connecté.");
                return null;
            }
        }
        else if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        return player;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<String> tabComplete = new ArrayList<>();
        if (args.length == 1) {
            tabComplete.add("join");
            tabComplete.add("leave");
            tabComplete.add("resetPlayer");
            tabComplete.add("list");
            tabComplete.add("config");
            tabComplete.add("add");
            tabComplete.add("remove");
            tabComplete.add("setDefaultDuration");
            tabComplete.add("setDistanceFromRing");
            tabComplete.add("setDistanceFromRingY");
            tabComplete.add("setDistanceFromStart");
            tabComplete.add("setStartLocation");
            return tabComplete;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setDistanceFromRing") || args[0].equalsIgnoreCase("setDistanceFromRingY") || args[0].equalsIgnoreCase("setDistanceFromStart")) {
                tabComplete.add("<distance>");
                return tabComplete;
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("setDefaultDuration")) {
                tabComplete.add("<duration>");
                return tabComplete;
            } else if (args[0].equalsIgnoreCase("remove")) {
                tabComplete.add("<num>");
                return tabComplete;
            } else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("resetPlayer")) {
                // Add players list
                for (Player player : ElytraParkourModule.playersInParkour) {
                    tabComplete.add(player.getName());
                }
                return tabComplete;
            } else  if (args[0].equalsIgnoreCase("join")) {
                // Add players list
                for (Player player : Bukkit.getOnlinePlayers()) {
                    tabComplete.add(player.getName());
                }
                return tabComplete;
            }
        }
        return null;
    }

}
