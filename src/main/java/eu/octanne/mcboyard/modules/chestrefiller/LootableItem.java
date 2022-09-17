package eu.octanne.mcboyard.modules.chestrefiller;

import org.bukkit.inventory.ItemStack;

public class LootableItem {
    private ItemStack item;
    private int chance;
    private int min;
    private int max;

    public LootableItem(ItemStack item, int chance, int min, int max) {
        this.item = item;
        this.chance = chance;
        this.min = min;
        this.max = max;
    }

    public ItemStack getLoot() {
        ItemStack loot = item.clone();
        loot.setAmount((int) (Math.random() * (getMax() - getMin()) + getMin()));
        return loot;
    }

    public int getChance() {
        return chance;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
