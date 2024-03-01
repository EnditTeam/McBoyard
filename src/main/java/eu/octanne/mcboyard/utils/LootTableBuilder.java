package eu.octanne.mcboyard.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import eu.octanne.mcboyard.modules.chestrefiller.LootableItem;
import net.kyori.adventure.text.Component;

public class LootTableBuilder {
    private static final Random random = new Random();
    private boolean useWeight = false;

    private final List<LootableItem> entries = new ArrayList<>();

    public LootTableBuilder usePourcentage(boolean usePourcentage) {
        this.useWeight = !usePourcentage;
        return this;
    }

    public LootTableBuilder useWeight(boolean useWeight) {
        this.useWeight = useWeight;
        return this;
    }

    public LootTableBuilder add(LootableItem item) {
        entries.add(item);
        return this;
    }

    public LootTableBuilder add(ItemStack item, double chance, int min, int max) {
        int chanceInt = (int) Math.floor(chance * 1000);
        if (chanceInt == 0)
            chanceInt = 1;
        entries.add(new LootableItem(item, chanceInt, min, max));
        return this;
    }

    public LootTableBuilder add(Material material, double chance, int min, int max) {
        return add(new ItemStack(material, 1), chance, min, max);
    }

    public LootTableBuilder add(Material material, double chance, int min, int max, String displayName) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(displayName));
        item.setItemMeta(meta);

        return add(item, chance, min, max);
    }

    public int size() {
        return entries.size();
    }

    /**
     * Fill an inventory (chest) with the items of this loot table
     * 
     * @param inventory
     * @param clear     If true, the inventory will be cleared before filling,
     *                  otherwise the items will be added to the existing
     *                  content without replacing anything
     */
    public void fillInventory(Inventory inventory, boolean clear) {
        if (clear)
            inventory.clear();

        int total = 0;
        int[] chances = new int[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            LootableItem entry = entries.get(i);
            chances[i] = entry.getChance();
            total += entry.getChance();
        }

        int slotsToFill = inventory.getSize();

        // Pourcentages
        if (!useWeight) {
            if (total > 1000) {
                throw new IllegalArgumentException("The total chance is greater than 1000 (100.0 %)");
            }
            slotsToFill *= total / 1000.0; // Remove air
        }

        for (int i = 0; i < slotsToFill; i++) {
            // Choose a slot
            int slot = random.nextInt(inventory.getSize());
            if (inventory.getItem(slot) != null)
                continue;

            // Choose an item
            int index = solvePie(chances);
            if (index == -1)
                break;

            // Create the item
            LootableItem entry = entries.get(index);
            ItemStack item = entry.getLoot();
            inventory.setItem(slot, item);
        }
    }

    /**
     * Solve a pie with the given weights
     * 
     * @param weights the weights of each part of the pie (ex: 20, 30, 50)
     * @return the index of the selected part of the pie, or -1 if the pie is empty
     */
    public static int solvePie(int... weights) {
        int total = 0;
        for (int proportion : weights) {
            total += proportion;
        }
        float r = total * random.nextFloat();
        for (int i = 0; i < weights.length; i++) {
            if (r < weights[i]) {
                return i;
            }
            r -= weights[i];
        }
        return -1;
    }
}
