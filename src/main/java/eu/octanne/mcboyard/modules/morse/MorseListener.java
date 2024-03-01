package eu.octanne.mcboyard.modules.morse;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import eu.octanne.mcboyard.McBoyard;

public class MorseListener implements Listener {

    /**
     * Handle player right clicking on the computer
     */
    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (!McBoyard.morseModule.isActive())
            return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (!block.equals(McBoyard.morseModule.getComputerBlock()))
            return;

        event.setCancelled(true);

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir())
            return;

        if (McBoyard.morseModule.computerInteraction(player, item)) {
            player.getInventory().remove(item);
        }
    }
}
