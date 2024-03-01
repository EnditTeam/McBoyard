package eu.octanne.mcboyard.modules.morse;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import eu.octanne.mcboyard.McBoyard;

public class MorseCommand implements CommandExecutor, TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("mcboyard.morse"))
            return null;
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "give_words", "translate", "computer_animation");
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        switch (args.length > 0 ? args[0] : "") {
            case "computer_animation":
                startComputerAnimation();
                return true;
            default:
                sender.sendMessage("§cUsage: /morse <start|stop|give_words|translate|computer_animation>");
                return false;
        }
    }

    private void startComputerAnimation() {
        McBoyard.morseModule.startComputerAnimation();
    }
}
