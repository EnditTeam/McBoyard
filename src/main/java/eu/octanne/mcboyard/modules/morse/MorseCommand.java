package eu.octanne.mcboyard.modules.morse;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
                return startComputerAnimation(sender);
            case "translate":
                return translateWord(sender, args);
            default:
                sender.sendMessage("§cUsage: /morse <start|stop|give_words|translate|computer_animation>");
                return false;
        }
    }

    private ItemStack getItemStack(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player player = (Player) sender;
        return player.getInventory().getItemInMainHand();
    }

    private boolean startComputerAnimation(CommandSender sender) {
        Player player = null;
        ItemStack item = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            item = player.getInventory().getItemInMainHand();
        }
        McBoyard.morseModule.computerInteraction(player, item);
        return true;
    }

    private boolean translateWord(CommandSender sender, String[] args) {
        ItemStack item = getItemStack(sender);
        if (args.length < 2 && (item == null || item.getType().isAir())) {
            sender.sendMessage("§cVous devez tenir un item ou utiliser /morse translate <mot>");
            return false;
        }

        String word;
        if (args.length >= 2) {
            word = args[1];
        } else {
            word = MorseTranslator.getItemName(item);
        }

        McBoyard.morseModule.spawnTranslatedBook(word);
        return true;
    }
}
