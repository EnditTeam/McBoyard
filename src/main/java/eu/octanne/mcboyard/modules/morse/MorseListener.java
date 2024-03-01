package eu.octanne.mcboyard.modules.morse;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import eu.octanne.mcboyard.McBoyard;

public class MorseListener implements Listener {
    private boolean onBlockInteract(Player player, Block target, Cancellable event) {
        if (!McBoyard.morseModule.isActive())
            return false;
        if (target.getLocation().equals(McBoyard.morseModule.getComputerBlock().getLocation())) {
            ItemStack item = player.getInventory().getItemInMainHand();
            McBoyard.morseModule.interactWithComputer(player, item);
        }
        event.setCancelled(true);
        return true;
    }

    /**
     * Handle player right clicking an armor stand
     */
    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        onBlockInteract(player, block, e);
    }
}
