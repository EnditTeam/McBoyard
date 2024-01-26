package eu.octanne.mcboyard.modules.telephone;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.telephone.Activity.RingType;

public class TelephoneCommand implements CommandExecutor, TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("mcboyard.telephone"))
            return null;
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "restart", "ring");
        }
        if (args[0].equalsIgnoreCase("ring")) {
            if (args.length == 2) {
                return Arrays.asList("pling", "banjo", "stop");
            }
        }

        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {

        switch (args.length > 0 ? args[0] : "") {
            case "start":
            case "stop":
            case "restart":
                return onActivityCommand(sender, args);
            case "ring":
                return onRingCommand(sender, args);
            default:
                sender.sendMessage("§cUsage: /telephone <start|stop|reset|room|ring>");
                return false;
        }
    }

    private boolean onActivityCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        switch (args[0]) {
            case "start":
                McBoyard.telephoneModule.start();
                sender.sendMessage("§aSalle Téléphone démarrée.");
                return true;
            case "stop":
                McBoyard.telephoneModule.stop();
                sender.sendMessage("§aSalle Téléphone arrêtée.");
                return true;
            case "restart":
                McBoyard.telephoneModule.restart();
                sender.sendMessage("§aSalle Téléphone redémarrée.");
                return true;
            default:
                return false;
        }
    }

    public boolean onRingCommand(CommandSender sender, String[] args) {
        Activity activity = McBoyard.telephoneModule.getActivity();
        if (activity == null) {
            sender.sendMessage("§cLa salle Téléphone n'est pas démarrée.");
            return false;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /telephone ring <pling|banjo|stop> (phoneId)");
            return false;
        }
        RingType ringType;
        switch (args[1].toLowerCase()) {
            case "pling":
                ringType = RingType.PLING;
                break;
            case "banjo":
                ringType = RingType.BANJO;
                break;
            case "stop":
                activity.setRingingPhone(-1, null);
                sender.sendMessage("§aTéléphone arrêté.");
                return true;
            default:
                sender.sendMessage("§cUsage: /telephone ring <pling|banjo|stop> (phoneId)");
                return false;
        }

        int phoneId = -1;
        if (args.length >= 3) {
            try {
                phoneId = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cUsage: /telephone ring <pling|banjo|stop> (phoneId)");
                return false;
            }
        } else {
            phoneId = activity.getRandomPhoneId();
        }

        activity.setRingingPhone(phoneId, ringType);
        return true;
    }
}
