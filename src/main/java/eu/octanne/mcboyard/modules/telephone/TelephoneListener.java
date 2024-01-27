package eu.octanne.mcboyard.modules.telephone;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import eu.octanne.mcboyard.McBoyard;
import io.papermc.paper.event.player.PlayerArmSwingEvent;

public class TelephoneListener implements Listener {
    private boolean onEntityInteract(Player player, Entity target, Cancellable event) {
        Activity activity = McBoyard.telephoneModule.getActivity();
        if (activity == null)
            return false;
        if (!activity.isPhone(target))
            return false;
        if (activity.onPhoneInteract(target)) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
        event.setCancelled(true);
        return true;
    }

    /**
     * Handle player right clicking an armor stand
     */
    @EventHandler
    private void onPlayerInteract(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        Entity entity = e.getRightClicked();
        onEntityInteract(player, entity, e);
    }

    /**
     * Handle player in creative hitting an armor stand
     */
    @EventHandler
    public void onPlayerInteractBlock(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();
        if (damager instanceof Player) {
            Player player = (Player) damager;
            onEntityInteract(player, damaged, event);
        }
    }

    /**
     * Handle player in survival hitting an armor stand
     */
    @EventHandler
    private void onPlayerSwingAtEntity(PlayerArmSwingEvent e) {
        Player player = e.getPlayer();
        Entity target = player.getTargetEntity(5);
        if (target != null) {
            onEntityInteract(player, target, e);
        }
    }
}
