package eu.octanne.mcboyard.modules.chestrefiller;

import eu.octanne.mcboyard.McBoyard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LootEditor implements Listener {

    // TODO make Event Action for GUI

    private HashMap<Player, LootableItem> editingLoot = new HashMap<Player, LootableItem>();
    private HashMap<Player, LootableItem> removeLoot = new HashMap<Player, LootableItem>();
    private HashMap<Player, Location> editingChest = new HashMap<Player, Location>();
    private HashMap<Player, Location> removeChest = new HashMap<Player, Location>();

    private HashMap<Player, Integer> inChestShow = new HashMap<Player, Integer>();
    private HashMap<Player, Integer> inLootShow = new HashMap<Player, Integer>();

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        removeIfInGui(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        removeIfInGui(e.getPlayer());
    }

    @EventHandler
    public void onCloseInv(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player) removeIfInGui((Player) e.getPlayer());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (checkIfInGui((Player) e.getWhoClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMoveItem(InventoryMoveItemEvent e) {
        if (checkIfInGui((Player) e.getInitiator().getHolder())) e.setCancelled(true);
    }

    @EventHandler
    public void onInteractInv(InventoryInteractEvent e) {
        if (checkIfInGui((Player) e.getWhoClicked())) e.setCancelled(true);
    }

    @EventHandler
    public void onPickItem(InventoryPickupItemEvent e) {
        if (checkIfInGui((Player) e.getInventory().getHolder())) e.setCancelled(true);
    }

    @EventHandler
    public void onLoot(PlayerDropItemEvent e) {
        if (checkIfInGui(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (checkIfInGui((Player) e.getWhoClicked())) e.setCancelled(true);

        if (e.getCurrentItem() == null) return;
        if (e.getClickedInventory() == null) return;

        // Check if Validate
        if (e.getCurrentItem().getType() == Material.GREEN_WOOL) {
            // Check if remove
            if (removeLoot.containsKey((Player)e.getWhoClicked())) {
                // Remove Loot
                McBoyard.chestFillerModule.removeLootableItem(removeLoot.get((Player)e.getWhoClicked()));
                e.getWhoClicked().closeInventory();
                // Message de suppression
                e.getWhoClicked().sendMessage("§aLoot supprimé !");
                // Reopen Show Loot
                showLootableItems((Player)e.getWhoClicked());
                return;
            }
            // Check if edit
            if (editingLoot.containsKey((Player)e.getWhoClicked())) {
                // Save Loot
                McBoyard.chestFillerModule.addLootableItem(editingLoot.get((Player)e.getWhoClicked()));
                e.getWhoClicked().closeInventory();
                // Message d'ajout
                e.getWhoClicked().sendMessage("§aLoot ajouté !");
                // Reopen Show Loot
                showLootableItems((Player)e.getWhoClicked());
                return;
            }
            // Check if edit chest
            if (editingChest.containsKey((Player)e.getWhoClicked())) {
                // Save Chest
                McBoyard.chestFillerModule.enrollChest(editingChest.get((Player)e.getWhoClicked()));
                e.getWhoClicked().closeInventory();
                // Message d'ajout
                e.getWhoClicked().sendMessage("§aCoffre ajouté !");
                // Reopen Show Chest
                showEnrollChests((Player)e.getWhoClicked());
                return;
            }
            // Check if remove chest
            if (removeChest.containsKey((Player)e.getWhoClicked())) {
                // Remove Chest
                McBoyard.chestFillerModule.unenrollChest(removeChest.get((Player)e.getWhoClicked()));
                e.getWhoClicked().closeInventory();
                // Message de suppression
                e.getWhoClicked().sendMessage("§aCoffre supprimé !");
                // Reopen Show Chest
                showEnrollChests((Player)e.getWhoClicked());
                return;
            }
        }

        // Check if Cancel
        if (e.getCurrentItem().getType() == Material.RED_WOOL) {
            // Check if remove
            if (removeLoot.containsKey((Player)e.getWhoClicked())) {
                e.getWhoClicked().closeInventory();
                // Message d'annulation
                e.getWhoClicked().sendMessage("§cSuppression annulée !");
                return;
            }
            // Check if edit
            if (editingLoot.containsKey((Player)e.getWhoClicked())) {
                e.getWhoClicked().closeInventory();
                // Message d'annulation
                e.getWhoClicked().sendMessage("§cAjout annulé !");
                return;
            }
            // Check if edit chest
            if (editingChest.containsKey((Player)e.getWhoClicked())) {
                e.getWhoClicked().closeInventory();
                // Message d'annulation
                e.getWhoClicked().sendMessage("§cAjout annulé !");
                return;
            }
            // Check if remove chest
            if (removeChest.containsKey((Player)e.getWhoClicked())) {
                e.getWhoClicked().closeInventory();
                // Message d'annulation
                e.getWhoClicked().sendMessage("§cSuppression annulée !");
                return;
            }
        }

        // Get index of clicked item
        int index = e.getSlot() - 9;
        if (index >= 0 && index < 27) {
            // Check if in Loot Show
            if (inLootShow.containsKey((Player)e.getWhoClicked())) {
                // Get Loot
                LootableItem loot = McBoyard.chestFillerModule.getLootableItems()
                        .get(index + (inLootShow.get((Player)e.getWhoClicked()) - 1) * 27);

                // Open Delete Loot GUI
                e.getWhoClicked().closeInventory();
                removeLootableItem((Player)e.getWhoClicked(), loot);
                return;
            }
            // Check if in Chest Show
            if (inChestShow.containsKey((Player)e.getWhoClicked())) {
                // Get Chest
                Location chest = McBoyard.chestFillerModule.getEnrollChests()
                        .get(index + (inChestShow.get((Player)e.getWhoClicked()) - 1) * 27);

                // If Left Click
                if (e.getClick() == ClickType.LEFT) {
                    // Teleport player to chest
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().teleport(chest);
                    // Message to player
                    e.getWhoClicked().sendMessage("§aTéléportation au coffre...");
                    return;
                } else if (e.getClick() == ClickType.RIGHT) {
                    // Open Delete Chest GUI
                    e.getWhoClicked().closeInventory();
                    unrollChest((Player)e.getWhoClicked(), chest);
                    return;
                }
            }
        }

        // Page précédente
        if (e.getSlot() == 1 || e.getSlot() == 37) {
            // Check if in Loot Show
            if (inLootShow.containsKey((Player)e.getWhoClicked())) {
                // Get Page
                int page = inLootShow.get((Player)e.getWhoClicked());
                if (page > 1) {
                    // Open Previous Page
                    e.getWhoClicked().closeInventory();
                    showLootableItems((Player)e.getWhoClicked(), page - 1);
                }
                return;
            }
            // Check if in Chest Show
            if (inChestShow.containsKey((Player)e.getWhoClicked())) {
                // Get Page
                int page = inChestShow.get((Player)e.getWhoClicked());
                if (page > 1) {
                    // Open Previous Page
                    e.getWhoClicked().closeInventory();
                    showEnrollChests((Player)e.getWhoClicked(), page - 1);
                }
                return;
            }
        }

        // Page suivante
        if (e.getSlot() == 7 || e.getSlot() == 43) {
            // Check if in Loot Show
            if (inLootShow.containsKey((Player)e.getWhoClicked())) {
                // Get Page
                int page = inLootShow.get((Player)e.getWhoClicked());
                if (page < McBoyard.chestFillerModule.getLootableItems().size() / 27 + 1) {
                    // Open Next Page
                    e.getWhoClicked().closeInventory();
                    showLootableItems((Player)e.getWhoClicked(), page + 1);
                }
                return;
            }
            // Check if in Chest Show
            if (inChestShow.containsKey((Player)e.getWhoClicked())) {
                // Get Page
                int page = inChestShow.get((Player)e.getWhoClicked());
                if (page < McBoyard.chestFillerModule.getEnrollChests().size() / 27 + 1) {
                    // Open Next Page
                    e.getWhoClicked().closeInventory();
                    showEnrollChests((Player)e.getWhoClicked(), page + 1);
                }
                return;
            }
        }
    }

    private void removeIfInGui(Player p) {
        editingLoot.remove(p);
        removeLoot.remove(p);
        editingChest.remove(p);
        removeChest.remove(p);
        inChestShow.remove(p);
        inLootShow.remove(p);
    }

    private boolean checkIfInGui(Player p) {
        if (editingLoot.containsKey(p)) return true;
        if (removeLoot.containsKey(p)) return true;
        if (editingChest.containsKey(p)) return true;
        if (removeChest.containsKey(p)) return true;
        if (inChestShow.containsKey(p)) return true;
        if (inLootShow.containsKey(p)) return true;
        return false;
    }

    public LootEditor() {

    }

    public void showLootableItems(Player p) {
        showLootableItems(p, 1);
    }

    public void showLootableItems(Player p, int page) {
        var maxPage = (int) Math.ceil((double) McBoyard.chestFillerModule.getLootableItems().size() / 27);
        if (maxPage == 0) maxPage = 1;

        Inventory inv = Bukkit.createInventory(null, 45, "§cChestR §7| §aLoots enregistrés");
        ItemStack separator = getSeparator();

        for (int i = 0; i < 9; i++) inv.setItem(i, separator);
        for (int i = 45-9; i < 45; i++) inv.setItem(i, separator);

        // Next Page
        ItemStack nextPage = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta nextPageMeta = (SkullMeta) nextPage.getItemMeta();
        nextPageMeta.setOwningPlayer(Bukkit.getOfflinePlayer("MHF_ArrowRight"));
        nextPageMeta.setDisplayName("§aPage suivante");
        nextPage.setItemMeta(nextPageMeta);
        inv.setItem(7, nextPage);
        inv.setItem(43, nextPage);
        // Prev Page
        ItemStack prevPage = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta prevPageMeta = (SkullMeta) prevPage.getItemMeta();
        prevPageMeta.setOwningPlayer(Bukkit.getOfflinePlayer("MHF_ArrowLeft"));
        prevPageMeta.setDisplayName("§aPage précédente");
        prevPage.setItemMeta(prevPageMeta);
        inv.setItem(1, prevPage);
        inv.setItem(37, prevPage);
        // Statut Page
        ItemStack statutPage = new ItemStack(Material.PAPER, 1);
        ItemMeta statutPageMeta = statutPage.getItemMeta();
        statutPageMeta.setDisplayName("§7Page §a" + page + "§7/§a" + maxPage);
        statutPage.setItemMeta(statutPageMeta);
        inv.setItem(4, statutPage);
        inv.setItem(40, statutPage);

        // Set Loots
        int index = page * 27 - 27;
        for (int i = 9; i < 45-9; i++) {
            if (index >= McBoyard.chestFillerModule.getLootableItems().size()) break;
            if (index < page*27) {
                ItemStack item = McBoyard.chestFillerModule.getLootableItems().get(index).getLoot();
                item.setAmount(1);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName("§a" + item.getType().name());
                List<String> lore = new ArrayList<>();
                lore.add("§7Item enregistré");
                lore.add("§7Chance: §a" + McBoyard.chestFillerModule.getLootableItems().get(index).getChance() + "%");
                lore.add("§7Quantité: §a" + McBoyard.chestFillerModule.getLootableItems().get(index).getMin() + " §7à §a"
                        + McBoyard.chestFillerModule.getLootableItems().get(index).getMax());
                // Show action to delete
                lore.add("§7");
                lore.add("§7Cliquez pour supprimer");
                itemMeta.setLore(lore);
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(itemMeta);
                inv.setItem(i, item);
            }
            index++;
        }

        inLootShow.put(p, page);
        p.openInventory(inv);
    }

    public void showEnrollChests(Player p, int page) {
        var maxPage = (int) Math.ceil((double) McBoyard.chestFillerModule.getEnrollChests().size() / 27);
        if (maxPage == 0) maxPage = 1;

        Inventory inv = Bukkit.createInventory(null, 45, "§cChestR §7| §aCoffres enregistrés");
        ItemStack separator = getSeparator();

        for (int i = 0; i < 9; i++) inv.setItem(i, separator);
        for (int i = 45-9; i < 45; i++) inv.setItem(i, separator);

        // Next Page
        ItemStack nextPage = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta nextPageMeta = (SkullMeta) nextPage.getItemMeta();
        nextPageMeta.setOwningPlayer(Bukkit.getOfflinePlayer("MHF_ArrowRight"));
        nextPageMeta.setDisplayName("§aPage suivante");
        nextPage.setItemMeta(nextPageMeta);
        inv.setItem(7, nextPage);
        inv.setItem(43, nextPage);
        // Prev Page
        ItemStack prevPage = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta prevPageMeta = (SkullMeta) prevPage.getItemMeta();
        prevPageMeta.setOwningPlayer(Bukkit.getOfflinePlayer("MHF_ArrowLeft"));
        prevPageMeta.setDisplayName("§aPage précédente");
        prevPage.setItemMeta(prevPageMeta);
        inv.setItem(1, prevPage);
        inv.setItem(37, prevPage);
        // Statut Page
        ItemStack statutPage = new ItemStack(Material.PAPER, 1);
        ItemMeta statutPageMeta = statutPage.getItemMeta();
        statutPageMeta.setDisplayName("§7Page §a" + page + "§7/§a" + maxPage);
        statutPage.setItemMeta(statutPageMeta);
        inv.setItem(4, statutPage);
        inv.setItem(40, statutPage);

        // Show Chest
        int index = page * 27 - 27;
        for (int i = 9; i < 45-9; i++) {
            if (index >= McBoyard.chestFillerModule.getEnrollChests().size()) break;
            if (index < page*27) {
                ItemStack chest = new ItemStack(McBoyard.chestFillerModule.getEnrollChests().get(index).getBlock().getType(), 1);
                ItemMeta chestMeta = chest.getItemMeta();
                chestMeta.setDisplayName("§aCoffre");
                List<String> lore = new ArrayList<>();
                lore.add("§7Coffre enregistré");
                lore.add("§7Type: §a" + McBoyard.chestFillerModule.getEnrollChests().get(index).getBlock().getType().name());
                lore.add("§7Position: §a" + McBoyard.chestFillerModule.getEnrollChests().get(index).getWorld().getName()
                        + " §7| §a" + McBoyard.chestFillerModule.getEnrollChests().get(index).getX() + " §7| §a"
                        + McBoyard.chestFillerModule.getEnrollChests().get(index).getY() + " §7| §a"
                        + McBoyard.chestFillerModule.getEnrollChests().get(index).getZ());
                // Show action to delete
                lore.add("§7");
                lore.add("§7Clique droit pour supprimer");
                // Show action to teleport
                lore.add("§7Clique gauche pour se téléporter");
                chestMeta.setLore(lore);
                chestMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                chest.setItemMeta(chestMeta);
                inv.setItem(i, chest);
            }
            index++;
        }

        inChestShow.put(p, page);
        p.openInventory(inv);
    }

    public void showEnrollChests(Player p) {
        showEnrollChests(p, 1);
    }

    public ItemStack getSeparator() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta data = item.getItemMeta();
        data.setDisplayName(" ");
        data.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        data.addItemFlags(ItemFlag.HIDE_DESTROYS);
        data.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        data.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        data.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        data.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(data);
        return item;
    }

    /**
     * Add new LootableItem by showing GUI to player
     *
     * @param p
     */
    public void addLootableItem(Player p, LootableItem item) {
        Inventory inv = Bukkit.createInventory(null, 27, "§cChestR §7| §aConfirmer le loot ?");
        ItemStack separator = getSeparator();

        for (int i = 0; i < 9; i++) inv.setItem(i, separator);
        inv.setItem(13, separator);
        for (int i = 27-10; i < 27; i++) inv.setItem(i, separator);

        ItemStack itemStack = item.getLoot();
        itemStack.setAmount(1);
        inv.setItem(9, itemStack);

        ItemStack chance = new ItemStack(Material.PAPER, 1);
        ItemMeta chanceMeta = chance.getItemMeta();
        chanceMeta.setDisplayName("§7Chance: §a" + item.getChance() + "%");
        chance.setItemMeta(chanceMeta);
        inv.setItem(10, chance);

        ItemStack min = new ItemStack(Material.PAPER, 1);
        ItemMeta minMeta = min.getItemMeta();
        minMeta.setDisplayName("§7Quantité min: §a" + item.getMin());
        min.setItemMeta(minMeta);

        ItemStack max = new ItemStack(Material.PAPER, 1);
        ItemMeta maxMeta = max.getItemMeta();
        maxMeta.setDisplayName("§7Quantité max: §a" + item.getMax());
        max.setItemMeta(maxMeta);

        inv.setItem(12, max);
        inv.setItem(11, min);

        // Item to validate
        ItemStack validate = new ItemStack(Material.GREEN_WOOL, 1);
        ItemMeta validateMeta = validate.getItemMeta();
        validateMeta.setDisplayName("§aValider");
        validate.setItemMeta(validateMeta);
        inv.setItem(14, validate);

        // Item to cancel
        ItemStack cancel = new ItemStack(Material.RED_WOOL, 1);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cAnnuler");
        cancel.setItemMeta(cancelMeta);
        inv.setItem(16, cancel);

        editingLoot.put(p, item);
        p.openInventory(inv);
    }

    /**
     * Remove LootableItem by showing GUI to player
     * @param p
     * @param item
     */
    public void removeLootableItem(Player p, LootableItem item) {
        Inventory inv = Bukkit.createInventory(null, 27, "§cChestR §7| §aValider suppression ?");
        ItemStack separator = getSeparator();

        for (int i = 0; i < 9; i++) inv.setItem(i, separator);
        inv.setItem(13, separator);
        for (int i = 27-10; i < 27; i++) inv.setItem(i, separator);

        ItemStack itemStack = item.getLoot();
        itemStack.setAmount(1);
        inv.setItem(9, itemStack);

        ItemStack chance = new ItemStack(Material.PAPER, 1);
        ItemMeta chanceMeta = chance.getItemMeta();
        chanceMeta.setDisplayName("§7Chance: §a" + item.getChance() + "%");
        chance.setItemMeta(chanceMeta);
        inv.setItem(10, chance);

        ItemStack min = new ItemStack(Material.PAPER, 1);
        ItemMeta minMeta = min.getItemMeta();
        minMeta.setDisplayName("§7Quantité min: §a" + item.getMin());
        min.setItemMeta(minMeta);

        ItemStack max = new ItemStack(Material.PAPER, 1);
        ItemMeta maxMeta = max.getItemMeta();
        maxMeta.setDisplayName("§7Quantité max: §a" + item.getMax());
        max.setItemMeta(maxMeta);

        inv.setItem(12, max);
        inv.setItem(11, min);

        // Item to validate
        ItemStack validate = new ItemStack(Material.GREEN_WOOL, 1);
        ItemMeta validateMeta = validate.getItemMeta();
        validateMeta.setDisplayName("§aValider");
        validate.setItemMeta(validateMeta);
        inv.setItem(14, validate);

        // Item to cancel
        ItemStack cancel = new ItemStack(Material.RED_WOOL, 1);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cAnnuler");
        cancel.setItemMeta(cancelMeta);
        inv.setItem(16, cancel);

        removeLoot.put(p, item);
        p.openInventory(inv);
    }

    /**
     * Ask player to validate chest location by GUI
     * @param p
     */
    public void enrollChest(Player p) {
        // check if target block is a chest
        Block targetBlock = p.getTargetBlock(null, 5);
        if (!targetBlock.getType().name().contains("CHEST") && !targetBlock.getType().name().contains("SHULKER_BOX") &&
                !targetBlock.getType().name().contains("BARREL")) {
            p.sendMessage("§cCible invalide !");
            return;
        }

        // check if already added
        if (McBoyard.chestFillerModule.getEnrollChests().contains(targetBlock.getLocation())) {
            p.sendMessage("§cCoffre déjà ajouté !");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "§cChestR §7| §aAjouter ?");
        ItemStack separator = getSeparator();

        for (int i = 0; i < 9; i++) inv.setItem(i, separator);
        inv.setItem(13, separator);
        for (int i = 27-9; i < 27; i++) inv.setItem(i, separator);

        ItemStack coffre = new ItemStack(Material.CHEST, 1);
        ItemMeta itemMeta = coffre.getItemMeta();
        itemMeta.setDisplayName("§7Conteneur: §a" + targetBlock.getType().name());
        List<String> lore = new ArrayList<>();
        lore.add("§7World: §a" + targetBlock.getWorld().getName());
        lore.add("§7X: §a" + targetBlock.getX());
        lore.add("§7Y: §a" + targetBlock.getY());
        lore.add("§7Z: §a" + targetBlock.getZ());
        itemMeta.setLore(lore);
        coffre.setItemMeta(itemMeta);
        inv.setItem(4, coffre);

        // Item to validate
        ItemStack validate = new ItemStack(Material.GREEN_WOOL, 1);
        ItemMeta validateMeta = validate.getItemMeta();
        validateMeta.setDisplayName("§aValider");
        validate.setItemMeta(validateMeta);
        inv.setItem(9, validate);
        inv.setItem(10, validate);
        inv.setItem(11, validate);
        inv.setItem(12, validate);

        // Item to cancel
        ItemStack cancel = new ItemStack(Material.RED_WOOL, 1);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cAnnuler");
        cancel.setItemMeta(cancelMeta);
        inv.setItem(14, cancel);
        inv.setItem(15, cancel);
        inv.setItem(16, cancel);
        inv.setItem(17, cancel);

        editingChest.put(p, targetBlock.getLocation());
        p.openInventory(inv);
    }

    /**
     * Ask player to validate to unregister chest location by GUI
     * @param p
     */
    public void unrollChest(Player p, Location loc) {
        Inventory inv = Bukkit.createInventory(null, 27, "§cChestR §7| §aSupprimer ?");
        ItemStack separator = getSeparator();

        // Check if chest is already deleted
        if (!McBoyard.chestFillerModule.getEnrollChests().contains(loc)) {
            p.sendMessage("§cCoffre déjà supprimé !");
            return;
        }

        for (int i = 0; i < 9; i++) inv.setItem(i, separator);
        inv.setItem(13, separator);
        for (int i = 27-9; i < 27; i++) inv.setItem(i, separator);

        ItemStack coffre = new ItemStack(Material.CHEST, 1);
        ItemMeta itemMeta = coffre.getItemMeta();
        itemMeta.setDisplayName("§7Conteneur: §a" + loc.getBlock().getType().name());
        List<String> lore = new ArrayList<>();
        lore.add("§7World: §a" + loc.getWorld().getName());
        lore.add("§7X: §a" + loc.getX());
        lore.add("§7Y: §a" + loc.getY());
        lore.add("§7Z: §a" + loc.getZ());
        itemMeta.setLore(lore);
        coffre.setItemMeta(itemMeta);
        inv.setItem(4, coffre);

        // Item to validate
        ItemStack validate = new ItemStack(Material.GREEN_WOOL, 1);
        ItemMeta validateMeta = validate.getItemMeta();
        validateMeta.setDisplayName("§aValider");
        validate.setItemMeta(validateMeta);
        inv.setItem(9, validate);
        inv.setItem(10, validate);
        inv.setItem(11, validate);
        inv.setItem(12, validate);

        // Item to cancel
        ItemStack cancel = new ItemStack(Material.RED_WOOL, 1);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cAnnuler");
        cancel.setItemMeta(cancelMeta);
        inv.setItem(14, cancel);
        inv.setItem(15, cancel);
        inv.setItem(16, cancel);
        inv.setItem(17, cancel);

        removeChest.put(p, loc);
        p.openInventory(inv);
    }

}
