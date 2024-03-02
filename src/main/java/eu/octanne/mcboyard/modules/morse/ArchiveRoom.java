package eu.octanne.mcboyard.modules.morse;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.utils.LootTableBuilder;

public class ArchiveRoom {
    public static final String[] WORDS = {
        "Camion",
        "Maquette",
        "Jamy",
        "Sabine",
        "Marcel",
        "Sorcier",
        "Fred",
        "Science",
        "Expérience",
    };

    private ArchiveRoom() {
    }

    public static void clearChests() {
        List<Chest> chests = getChests();
        for (Chest c : chests) {
            c.getBlockInventory().clear();
        }
    }

    public static void fillChests() {
        List<Chest> chests = getChests();
        if (chests.isEmpty()) {
            McBoyard.instance.getLogger().warning("Not enough chests to fill the archive room.");
            return;
        }
        for (Chest c : chests) {
            c.getBlockInventory().clear();
        }

        // Add the word items
        for (int i = 0; i < WORDS.length; i++) {
            boolean added = false;
            int attempts = 0;
            ItemStack wordItem = MorseModule.createWordItem(WORDS[i]);
            do {
                Chest c = chests.get((int) (Math.random() * chests.size()));
                if (tryAddItem(c, wordItem)) {
                    added = true;
                } else if (attempts > 100) {
                    McBoyard.instance.getLogger().warning("Failed to add word item " + i + " (" + WORDS[i] + "). "
                                                          + "This append because there is not enough chests for the words.");
                    break;
                } else {
                    attempts++;
                }
            } while (!added);
        }

        for (Chest c : chests) {
            fillChest(c);
        }
    }

    private static void fillChest(@NotNull Chest chest) {

        LootTableBuilder lootTable = new LootTableBuilder().useWeight(true);

        do {
            if (Math.random() < 0.3) {
                // libraria
                lootTable.add(Material.AIR, 10, 1, 1)
                    .add(Material.BOOK, 1, 1, 3)
                    .add(Material.INK_SAC, 1, 1, 3)
                    .add(Material.WRITTEN_BOOK, 1, 1, 1);
            }
            if (Math.random() < 0.3) {
                // food
                lootTable.add(Material.AIR, 50, 1, 3)
                    .add(Material.APPLE, 1, 1, 3)
                    .add(Material.BREAD, 1, 1, 3)
                    .add(Material.COOKED_BEEF, 1, 1, 3)
                    .add(Material.COOKED_CHICKEN, 1, 1, 3)
                    .add(Material.COOKED_COD, 1, 1, 3)
                    .add(Material.COOKED_MUTTON, 1, 1, 3)
                    .add(Material.COOKED_PORKCHOP, 1, 1, 3)
                    .add(Material.COOKED_RABBIT, 1, 1, 3)
                    .add(Material.COOKED_SALMON, 1, 1, 3)
                    .add(Material.COOKIE, 1, 1, 3)
                    .add(Material.MELON_SLICE, 1, 1, 3)
                    .add(Material.PUMPKIN_PIE, 1, 1, 3)
                    .add(Material.BAKED_POTATO, 1, 1, 3)
                    .add(Material.CARROT, 1, 1, 3)
                    .add(Material.GOLDEN_APPLE, 1, 1, 3)
                    .add(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 3);
            }
            if (Math.random() < 0.2) {
                // tools
                lootTable.add(Material.AIR, 50, 1, 3)
                    .add(Material.WOODEN_AXE, 1, 1, 1)
                    .add(Material.WOODEN_HOE, 1, 1, 1)
                    .add(Material.WOODEN_PICKAXE, 1, 1, 1)
                    .add(Material.WOODEN_SHOVEL, 1, 1, 1)
                    .add(Material.WOODEN_SWORD, 1, 1, 1)
                    .add(Material.STONE_AXE, 1, 1, 1)
                    .add(Material.STONE_HOE, 1, 1, 1)
                    .add(Material.STONE_PICKAXE, 1, 1, 1)
                    .add(Material.STONE_SHOVEL, 1, 1, 1)
                    .add(Material.STONE_SWORD, 1, 1, 1)
                    .add(Material.IRON_AXE, 1, 1, 1)
                    .add(Material.IRON_HOE, 1, 1, 1)
                    .add(Material.IRON_PICKAXE, 1, 1, 1)
                    .add(Material.IRON_SHOVEL, 1, 1, 1)
                    .add(Material.IRON_SWORD, 1, 1, 1)
                    .add(Material.GOLDEN_AXE, 1, 1, 1)
                    .add(Material.GOLDEN_HOE, 1, 1, 1)
                    .add(Material.GOLDEN_PICKAXE, 1, 1, 1)
                    .add(Material.GOLDEN_SHOVEL, 1, 1, 1)
                    .add(Material.GOLDEN_SWORD, 1, 1, 1)
                    .add(Material.SHEARS, 1, 1, 1)
                    .add(Material.FLINT_AND_STEEL, 1, 1, 1);
            }
            if (Math.random() < 0.2) {
                // flowers
                lootTable.add(Material.AIR, 50, 1, 3)
                    .add(Material.DANDELION, 1, 1, 3)
                    .add(Material.POPPY, 1, 1, 3)
                    .add(Material.BLUE_ORCHID, 1, 1, 3)
                    .add(Material.ALLIUM, 1, 1, 3)
                    .add(Material.AZURE_BLUET, 1, 1, 3)
                    .add(Material.RED_TULIP, 1, 1, 3)
                    .add(Material.ORANGE_TULIP, 1, 1, 3)
                    .add(Material.WHITE_TULIP, 1, 1, 3)
                    .add(Material.PINK_TULIP, 1, 1, 3)
                    .add(Material.OXEYE_DAISY, 1, 1, 3)
                    .add(Material.CORNFLOWER, 1, 1, 3)
                    .add(Material.LILY_OF_THE_VALLEY, 1, 1, 3);
            }
        } while (lootTable.size() == 0);

        lootTable.fillInventory(chest.getBlockInventory(), false);
    }

    private static boolean tryAddItem(Chest chest, ItemStack item) {
        Inventory inv = chest.getBlockInventory();
        int slot = (int) (Math.random() * inv.getSize());
        while (slot < inv.getSize() && inv.getItem(slot) != null) {
            slot++;
        }
        if (slot < inv.getSize()) {
            inv.setItem(slot, item);
            return true;
        } else {
            return false;
        }
    }

    public static List<Chest> getChests() {
        World w = McBoyard.getWorld();
        List<Chest> chests = new ArrayList<>();
        // Ceiling
        addChests(chests, w, new Vector(-50, 83, 24), new Vector(11, 1, 1));
        addChests(chests, w, new Vector(-50, 83, 18), new Vector(22, 1, 1));
        addChests(chests, w, new Vector(-28, 83, 19), new Vector(1, 1, 2));
        addChests(chests, w, new Vector(-29, 83, 22), new Vector(2, 1, 1));
        // Floor
        addChests(chests, w, new Vector(-51, 87, 25), new Vector(2, 3, 1));
        addChests(chests, w, new Vector(-46, 87, 25), new Vector(2, 3, 1));
        addChests(chests, w, new Vector(-52, 87, 19), new Vector(1, 2, 2));
        addChests(chests, w, new Vector(-50, 87, 17), new Vector(22, 1, 1));
        addChests(chests, w, new Vector(-27, 87, 19), new Vector(1, 2, 2));
        addChests(chests, w, new Vector(-29, 87, 22), new Vector(1, 1, 1));
        return chests;
    }

    private static void addChest(List<Chest> chests, World world, int x, int y, int z) {
        Chest c = getChest(world, x, y, z);
        if (c != null) {
            chests.add(c);
        }
    }

    private static void addChests(List<Chest> chests, World world, Vector min, Vector delta) {
        for (int x2 = 0; x2 < delta.getBlockX(); x2++) {
            for (int y2 = 0; y2 < delta.getBlockY(); y2++) {
                for (int z2 = 0; z2 < delta.getBlockZ(); z2++) {
                    addChest(chests, world, min.getBlockX() + x2, min.getBlockY() + y2, min.getBlockZ() + z2);
                }
            }
        }
    }

    private static Chest getChest(World world, int x, int y, int z) {
        Block b = world.getBlockAt(x, y, z);
        if (b.getType() == Material.CHEST) {
            return (Chest) b.getState();
        }
        return null;
    }
}
