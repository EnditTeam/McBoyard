package eu.octanne.mcboyard.modules.maitika;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_16_R3.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCaveSpider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import eu.octanne.mcboyard.McBoyard;

public class MaitikaCommand implements CommandExecutor, TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("mcboyard.coffrefort"))
            return null;
        if (args.length == 1) {
            return Arrays.asList("boss", "arena", "stuff", "start_battle", "stop_battle");
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "boss":
                    return Arrays.asList("spawn", "kill");
                case "arena":
                    return Arrays.asList("place", "remove");
                case "stuff":
                    return Arrays.asList("equip1", "equip2");
            }
        }

        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
            @NotNull String[] args) {

        switch (args.length > 0 ? args[0] : "") {
            case "boss":
                return onBossCommand(sender, args);
            case "arena":
                return onArenaCommand(sender, args);
            case "stuff":
                return onStuffCommand(sender, args);
            case "start_battle":
            case "stop_battle":
                return onBattleCommand(sender, args);
            default:
                sender.sendMessage("§cUsage: /maitika <boss|arena|stuff>");
                return false;
        }
    }

    private boolean onBossCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        Location loc;
        World w;
        if (sender instanceof Player) {
            loc = ((Player) sender).getLocation();
            w = loc.getWorld();
        } else if (sender instanceof CraftBlockCommandSender) {
            loc = ((CraftBlockCommandSender) sender).getBlock().getLocation();
            w = loc.getWorld();
        } else {
            loc = null;
            w = McBoyard.getWorld();
        }

        switch (args.length > 1 ? args[1] : "") {
            case "spawn": {
                if (loc == null) {
                    sender.sendMessage("§cVous devez être un joueur ou un command block.");
                    return false;
                }
                new MaitikaEntity(loc);
                sender.sendMessage("§aEntité Maïtika spawnée.");
                return true;
            }
            case "kill":
                List<CraftCaveSpider> entities = MaitikaEntity.getMaitikaEntities(w);
                entities.forEach(entity -> entity.remove());
                int count = entities.size();
                sender.sendMessage("§a" + count + " entité(s) Maïtika supprimée(s).");
                return true;
            default:
                sender.sendMessage("§cUsage: /maitika boss <spawn|kill>");
                return false;
        }
    }

    private boolean onArenaCommand(CommandSender sender, String[] args) {
        switch (args.length > 1 ? args[1] : "") {
            case "place":
                McBoyard.maitikaModule.placeArena();
                sender.sendMessage("§aArène Maïtika placée.");
                return true;
            case "remove":
                McBoyard.maitikaModule.removeArena();
                sender.sendMessage("§aArène Maïtika supprimée.");
                return true;
            default:
                sender.sendMessage("§cUsage: /maitika arena <place|remove>");
                return false;
        }
    }

    public boolean onStuffCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cVous devez être un joueur.");
            return false;
        }
        switch (args.length > 1 ? args[1] : "") {
            case "equip1":
                McBoyard.maitikaModule.equipGoodArmor((Player) sender);
                sender.sendMessage("§aStuff 1 équipé.");
                return true;
            case "equip2":
                McBoyard.maitikaModule.equipBadArmor((Player) sender);
                sender.sendMessage("§aStuff 2 équipé.");
                return true;
            default:
                sender.sendMessage("§cUsage: /maitika stuff <equip1|equip2>");
                return false;
        }
    }

    private boolean onBattleCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        switch (args.length > 0 ? args[0] : "") {
            case "start_battle":
                McBoyard.maitikaModule.startBattle();
                sender.sendMessage("§aCombat Maïtika démarré.");
                return true;
            case "stop_battle":
                McBoyard.maitikaModule.stopBattle();
                sender.sendMessage("§aCombat Maïtika arrêté.");
                return true;
            default:
                return false;
        }
    }
}
