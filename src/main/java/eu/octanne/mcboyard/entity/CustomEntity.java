package eu.octanne.mcboyard.entity;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.ExcaliburSystem;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomEntity implements Listener {

    @EventHandler
    public void onLoadEntities(EntityAddToWorldEvent e) {
        CraftEntity entity = (CraftEntity) e.getEntity();
        Location loc = entity.getLocation();
        if (entity.getType().equals(EntityType.ARMOR_STAND) && entity.getHandle() instanceof EntityArmorStand
                && !(entity.getHandle() instanceof ExcaliburStand)) {
            // Load ExcaliburStand
            NamespacedKey escaliburStand_name = new NamespacedKey("excalibur","excalibur_stand");
            if (entity.getPersistentDataContainer().has(escaliburStand_name, PersistentDataType.TAG_CONTAINER)) {
                CraftPersistentDataContainer nbtC = (CraftPersistentDataContainer) e.getEntity().getPersistentDataContainer()
                        .get(escaliburStand_name, PersistentDataType.TAG_CONTAINER);
                if (nbtC != null) {
                    NBTTagCompound nbt = nbtC.toTagCompound();
                    // log nbt
                    int standID = nbt.getInt("standID");
                    int nbSwordDurability = nbt.getInt("nbSwordDurability");
                    McBoyard.instance.getLogger().info("ExcaliburStand prepare to load ! (" + standID + ") " + nbt.toString());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ExcaliburStand.spawn(loc, nbSwordDurability, standID);
                            entity.remove();
                            McBoyard.instance.getLogger().info("ExcaliburStand loaded ! (" + standID + ") " + nbt.toString());
                        }
                    }.runTaskLater(McBoyard.instance, 0);
                }
            }
        }
    }

    @EventHandler
    public void onRemoveEntities(EntityRemoveFromWorldEvent e) {
        if (e.getEntityType().equals(EntityType.ARMOR_STAND) && ((CraftEntity)e.getEntity()).getHandle() instanceof ExcaliburStand) {
            ExcaliburStand stand = (ExcaliburStand) ((CraftEntity)e.getEntity()).getHandle();
            ExcaliburSystem.removeExcaliburStand(stand);
            McBoyard.instance.getLogger().info("ExcaliburStand unloaded ! (" + stand.getStandName() + ")");
        }
    }

}
