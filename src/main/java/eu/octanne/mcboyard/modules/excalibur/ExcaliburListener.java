package eu.octanne.mcboyard.modules.excalibur;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ExcaliburListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && (e.getClickedBlock().getType().equals(Material.DAMAGED_ANVIL)
            || e.getClickedBlock().getType().equals(Material.GRINDSTONE))) {
            for (StandKey standKey : StandKey.getStandKeys()) {
                for (Location loc : standKey.getBlocksLoc()) {
                    if (loc.getBlock().equals(e.getClickedBlock())) {
                        e.setCancelled(true);
                        // Message
                        e.getPlayer().sendMessage("§cVous ne pouvez pas interagir avec ce bloc !");
                        return;
                    }
                }
            }
        }
    }
}
