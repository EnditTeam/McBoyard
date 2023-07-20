package eu.octanne.mcboyard.utils.doors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GhostBlockData {
    public @NotNull Vector offset;
    public @NotNull Material material;
    public int customModelData;
    public float offsetYaw;

    public GhostBlockData(@NotNull Vector offset, @NotNull Material material, int customModelData, float yaw) {
        this.offset = offset;
        this.material = material;
        this.customModelData = customModelData;
        this.offsetYaw = yaw;
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(customModelData);
        item.setItemMeta(meta);
        return item;
    }
}
