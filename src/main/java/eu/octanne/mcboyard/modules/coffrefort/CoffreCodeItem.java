package eu.octanne.mcboyard.modules.coffrefort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CoffreCodeItem {

    private static final Random random = new Random();
    private final @NotNull ItemStack item;
    private final int code;
    private final boolean canUse;

    public @NotNull ItemStack getItem() {
        return item;
    }

    public int getCode() {
        return code;
    }

    public boolean isInfinit() {
        return code == -1;
    }

    public boolean canUse() {
        return canUse;
    }

    public boolean canOpen(@NotNull Chest coffre) {
        if (!(coffre instanceof Chest)) // Unused (for now)
            return false;
        return canUse && random.nextFloat() < 0.1f;
    }

    public void consume() {
        if (isInfinit())
            return;
        // Code with line-through
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§m" + code).color(NamedTextColor.RED));
        item.setItemMeta(meta);
    }

    private CoffreCodeItem(@NotNull ItemStack item, int code, boolean canUse) {
        this.item = item;
        this.code = code;
        this.canUse = canUse;
    }

    private static ItemStack createItem(int code, @Nullable List<Component> lore) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(code).color(NamedTextColor.RED));

        List<Component> lore2 = new ArrayList<>();
        lore2.add(Component.text("Code du coffre").color(NamedTextColor.GOLD));
        if (lore != null)
            lore2.addAll(lore);

        meta.lore(lore2);
        item.setItemMeta(meta);
        return item;
    }

    public static CoffreCodeItem createCode() {
        int code = random.nextInt(10000);
        ItemStack item = createItem(code, null);
        return new CoffreCodeItem(item, code, true);
    }

    public static CoffreCodeItem createInfinitCode() {
        int code = -1;
        ItemStack item = createItem(code, Arrays.asList(Component.text("§c§lAccès infini")));
        return new CoffreCodeItem(item, code, true);
    }

    public static CoffreCodeItem fromItem(@NotNull ItemStack item) {
        if (item.getType() != Material.PAPER)
            return null;
        if (!item.hasItemMeta())
            return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore() || meta.lore().isEmpty() || !meta.hasDisplayName())
            return null;
        if (!(meta.lore().get(0) instanceof TextComponent) || !(meta.displayName() instanceof TextComponent))
            return null;
        TextComponent lore0 = (TextComponent) meta.lore().get(0);
        if (!lore0.content().contains("Code du coffre"))
            return null;

        TextComponent displayName = (TextComponent) meta.displayName();
        String codeStr = displayName.content();

        if (!codeStr.matches("-?\\d{1,4}"))
            return new CoffreCodeItem(item, 0, false);

        int code = Integer.parseInt(displayName.content());

        return new CoffreCodeItem(item, code, true);
    }
}
