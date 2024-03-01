package eu.octanne.mcboyard.modules.morse;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
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
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (block == null || !block.equals(McBoyard.morseModule.getComputerBlock()))
            return;

        event.setCancelled(true);

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.PAPER) {
            // play a sound to indicate that the item is not valid
            Location loc = block.getLocation();
            World world = loc.getWorld();
            if (world != null) {
                world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            }
            return;
        }

        if (McBoyard.morseModule.computerInteraction(player, item)) {
            player.getInventory().remove(item);
        }
    }
}
