package eu.octanne.mcboyard.modules.excalibur;

import eu.octanne.mcboyard.entity.ExcaliburStand;
import net.minecraft.server.v1_16_R3.EntityTypes;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class ExcaliburListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent e) {
        // check if interact with ExcaliburStand
        if (((CraftEntity)e.getRightClicked()).getHandle().getEntityType() == EntityTypes.ARMOR_STAND &&
        ((CraftEntity) e.getRightClicked()).getHandle() instanceof ExcaliburStand) {
            ExcaliburStand stand = (ExcaliburStand) ((CraftEntity)e.getRightClicked()).getHandle();
            // give sword to player
            stand.takeSword(e.getPlayer());
            e.setCancelled(true);
        }
    }
}
