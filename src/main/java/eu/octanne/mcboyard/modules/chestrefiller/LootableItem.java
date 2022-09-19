package eu.octanne.mcboyard.modules.chestrefiller;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class LootableItem implements ConfigurationSerializable {

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
        this.item = item.clone();
        this.chance = chance;
        this.min = min;
        this.max = max;
        this.id = UUID.randomUUID();
    }

    public LootableItem(Map<String, Object> map) {
        this.id = UUID.fromString((String) map.get("id"));
        this.item = (ItemStack) map.get("item");
        this.chance = (int) map.get("chance");
        this.min = (int) map.get("min");
        this.max = (int) map.get("max");
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "id", id.toString(),
                "item", item,
                "chance", chance,
                "min", min,
                "max", max
        );
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
