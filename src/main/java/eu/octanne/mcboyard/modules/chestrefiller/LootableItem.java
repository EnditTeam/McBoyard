package eu.octanne.mcboyard.modules.chestrefiller;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class LootableItem {

    private UUID id;
    private ItemStack item;
    private int chance;
    private int min;
    private int max;

    /**
     * Create new LootableItem
     * @param item
     * @param chance
     * @param min
     * @param max
     */
    public LootableItem(ItemStack item, int chance, int min, int max) {
        this.item = item;
        this.chance = chance;
        this.min = min;
        this.max = max;
        this.id = UUID.randomUUID();
    }

    /**
     * Recreate from config
     * @param id
     * @param item
     * @param chance
     * @param min
     * @param max
     */
    public LootableItem(UUID id, ItemStack item, int chance, int min, int max) {
        this.id = id;
        this.item = item;
        this.chance = chance;
        this.min = min;
        this.max = max;
    }

    /**
     * Get item with random amount
     * @return
     */
    public ItemStack getLoot() {
        ItemStack loot = item.clone();
        loot.setAmount((int) (Math.random() * (getMax() - getMin()) + getMin()));
        return loot;
    }

    /**
     * Get Loot chance
     * @return
     */
    public int getChance() {
        return chance;
    }

    /**
     * Get min amount
     * @return
     */
    public int getMin() {
        return min;
    }

    /**
     * Get max amount
     * @return
     */
    public int getMax() {
        return max;
    }
}
