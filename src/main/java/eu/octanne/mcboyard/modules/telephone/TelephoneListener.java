package eu.octanne.mcboyard.modules.telephone;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class TelephoneListener implements Listener {
    @EventHandler
    private void onPlayerDeath(PlayerInteractEvent e) {
        Player player = e.getPlayer();
    }
}
