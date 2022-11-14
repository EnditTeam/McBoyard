package eu.octanne.mcboyard.modules.excalibur;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ExcaliburListener implements Listener {

    @EventHandler
    public void onInteractWithBlock(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && (e.getClickedBlock().getType().equals(Material.DAMAGED_ANVIL)
            || e.getClickedBlock().getType().equals(Material.GRINDSTONE))) {
            for (StandKey standKey : StandKey.getStandKeys()) {
                for (Location loc : standKey.getBlocksLoc()) {
                    if (loc.getBlock().equals(e.getClickedBlock())) {
                        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK) && useSword(e.getPlayer())
                                && e.getClickedBlock().getType().equals(Material.GRINDSTONE)) {
                            standKey.attaquerCorde(standKey.getCloserCrochet(e.getClickedBlock().getLocation()), e.getPlayer());
                        }

                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    public static boolean useSword(Player p) {
        if (p.getInventory().getItemInMainHand().getType().equals(Material.IRON_SWORD) && p.getInventory().getItemInMainHand().hasItemMeta()
                && p.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()
                && p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("§bExcalibur")) {
            return true;
        } else {
            return false;
        }
    }
}
