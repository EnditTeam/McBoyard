package eu.octanne.mcboyard.modules.coffrefort;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.PlugModule;
import eu.octanne.mcboyard.utils.LootTableBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CoffreFortModule extends PlugModule {
    private CoffreFortAnimation animation = null;

    public CoffreFortModule(JavaPlugin instance) {
        super(instance);
    }

    @Override
    public void onEnable() {
        CoffreFortCommand coffrefortCommand = new CoffreFortCommand();
        pl.getCommand("coffrefort").setExecutor(coffrefortCommand);
        pl.getCommand("coffrefort").setTabCompleter(coffrefortCommand);
        Bukkit.getPluginManager().registerEvents(new CoffreFortListener(), pl);
    }

    @Override
    public void onDisable() {
        stopAnimation();
    }

    protected void startAnimation() {
        if (animation != null)
            return;
        animation = new CoffreFortAnimation();
        animation.start();
    }

    protected void stopAnimation() {
        if (animation != null) {
            animation.stop();
            animation = null;
        }
    }

    public void reset() {
        if (animation == null) {
            // Create a new animation to reset the grids
            // The grids doesn't spawn, they are juste placed in open position
            animation = new CoffreFortAnimation();
        }
        animation.reset();
        animation = null;
        clearCoffres();
    }

    public @Nullable Chest getCoffre(@NotNull Block block) {
        if (block.getType() != Material.CHEST)
            return null;

        // Salle : 42 73 18 => 50 79 35
        Vector min = new Vector(42, 73, 18);
        Vector max = new Vector(50, 79, 35);
        if (!block.getLocation().toVector().isInAABB(min, max))
            return null;

        BlockState state = block.getState();
        if (!(state instanceof Chest))
            return null;
        return (Chest) state;
    }

    public boolean openCoffre(@NotNull Chest coffre, @NotNull Player player) {
        CoffreCodeItem codeItem = CoffreCodeItem.fromItem(player.getInventory().getItemInMainHand());
        if (codeItem == null) {
            player.sendActionBar(
                    Component.text("§lCoffre verrouillé, un code est nécessaire"));
            return false;
        }

        if (!codeItem.canUse()) {
            player.sendActionBar(Component.text("§k!§r §lCode déjà utilisé §k!§r"));
            return false;
        }

        if (!codeItem.canOpen(coffre)) {
            player.sendActionBar(Component.text("§lEchec de l'ouverture du coffre"));
            return false;
        }

        fillCoffre(coffre);
        codeItem.consume();
        player.sendActionBar(Component.text("§lCoffre déverrouillé !").color(NamedTextColor.GOLD));
        return true;
    }

    private void fillCoffre(@NotNull Chest coffre) {
        /**
         * 3 different loot tables :
         */

        LootTableBuilder lootTable = new LootTableBuilder();
        switch (LootTableBuilder.solvePie(20, 50, 30)) {
            case 0: // 20 % rich chest
                lootTable
                        .usePourcentage(true)
                        .add(Material.GOLD_INGOT, 0.3, 1, 8)
                        .add(Material.GOLD_BLOCK, 0.1, 1, 3)
                        .add(Material.NETHERITE_SCRAP, 0.01, 1, 1)
                        .add(Material.EMERALD, 0.1, 1, 8)
                        .add(Material.DIAMOND, 0.1, 1, 8)
                        .add(Material.TOTEM_OF_UNDYING, 0.01, 1, 1, "§rCanard");
                break;
            case 1: // 50 % normal gold
                lootTable
                        .usePourcentage(true)
                        .add(Material.GOLD_INGOT, 0.1, 1, 8)
                        .add(Material.EMERALD, 0.05, 1, 8)
                        .add(Material.DIAMOND, 0.05, 1, 8)
                        .add(Material.COBWEB, 0.05, 1, 1);
                break;
            case 2: // 30 % poor gold
                lootTable
                        .usePourcentage(true)
                        .add(Material.GOLD_INGOT, 0.2, 1, 2)
                        .add(Material.COBWEB, 0.1, 1, 1);
                break;
        }

        lootTable.fillInventory(coffre.getBlockInventory(), false);
    }

    public void clearCoffres() {
        // Salle : 42 73 18 => 50 79 35
        Vector min = new Vector(42, 73, 18);
        Vector max = new Vector(50, 79, 35);
        World w = McBoyard.getWorld();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Block block = w.getBlockAt(x, y, z);
                    if (block.getType() == Material.CHEST) {
                        Chest chest = (Chest) block.getState();
                        chest.getBlockInventory().clear();
                    }
                }
            }
        }
    }
}
