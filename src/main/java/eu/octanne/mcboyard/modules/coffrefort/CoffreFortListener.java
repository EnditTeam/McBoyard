package eu.octanne.mcboyard.modules.coffrefort;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import eu.octanne.mcboyard.McBoyard;
import net.kyori.adventure.text.Component;

public class CoffreFortListener implements Listener {

    @EventHandler
    public void onInteractWithBlock(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null)
            return;

        Chest coffre = McBoyard.coffreFortModule.getCoffre(block);
        if (coffre == null)
            return;

        Player player = e.getPlayer();
        if (player.isOp() && player.isSneaking()) {
            player.sendActionBar(Component.text("Vous êtes OP, vous pouvez ouvrir le coffre."));
            return;
        }

        boolean interactionAllowed = McBoyard.coffreFortModule.openCoffre(coffre, player);
        if (!interactionAllowed) {
            e.setCancelled(true);
            player.playSound(block.getLocation(), Sound.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1, 1.2f);
        }
    }
}
