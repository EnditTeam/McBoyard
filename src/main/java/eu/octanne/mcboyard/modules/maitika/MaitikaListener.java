package eu.octanne.mcboyard.modules.maitika;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import eu.octanne.mcboyard.McBoyard;

public class MaitikaListener implements Listener {
    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Location arenaCenter = McBoyard.maitikaModule.getArenaCenter();
        double distance = player.getLocation().distanceSquared(arenaCenter);
        if (distance <= 9) {
            McBoyard.maitikaModule.removeArmor(player);
            e.setKeepInventory(true);
        }
    }
}
