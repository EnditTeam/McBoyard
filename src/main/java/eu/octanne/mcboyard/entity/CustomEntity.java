package eu.octanne.mcboyard.entity;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.entity.standkey.CrochetEntity;
import eu.octanne.mcboyard.entity.standkey.KeyEntity;
import eu.octanne.mcboyard.entity.standkey.MiddleEntity;
import eu.octanne.mcboyard.modules.ExcaliburSystem;
import eu.octanne.mcboyard.modules.excalibur.StandKey;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntitySlime;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class CustomEntity implements Listener {

    @EventHandler
    public void onLoadEntities(EntityAddToWorldEvent e) {
        CraftEntity entity = (CraftEntity) e.getEntity();
        Location loc = entity.getLocation();
        if (entity.getType().equals(EntityType.ARMOR_STAND) && entity.getHandle() instanceof EntityArmorStand
                && !(entity.getHandle() instanceof ExcaliburStand)) {
            loadExcaliburStand(entity, loc);
        }
        if (entity.getType().equals(EntityType.ARMOR_STAND) && entity.getHandle() instanceof EntityArmorStand
                && !(entity.getHandle() instanceof MiddleEntity)) {
            loadMiddleEntity(entity, loc);
        }
        if (entity.getType().equals(EntityType.SLIME) && entity.getHandle() instanceof EntitySlime
                && !(entity.getHandle() instanceof CrochetEntity)) {
            loadCrochetEntity(entity, loc);
        }
        if (entity.getType().equals(EntityType.ARMOR_STAND) && entity.getHandle() instanceof EntityArmorStand
                && !(entity.getHandle() instanceof KeyEntity)) {
            loadKeyEntity(entity, loc);
        }
    }

    @EventHandler
    public void onRemoveEntities(EntityRemoveFromWorldEvent e) {
        // Unload ExcaliburStand
        if (e.getEntityType().equals(EntityType.ARMOR_STAND) &&
                ((CraftEntity)e.getEntity()).getHandle() instanceof ExcaliburStand) {
            ExcaliburStand stand = (ExcaliburStand) ((CraftEntity)e.getEntity()).getHandle();
            ExcaliburSystem.removeExcaliburStand(stand);
            //McBoyard.instance.getLogger().info("ExcaliburStand unloaded ! (" + stand.getStandName() + ")");
        }
        // Unload MiddleEntity
        if (e.getEntityType().equals(EntityType.ARMOR_STAND) &&
                ((CraftEntity)e.getEntity()).getHandle() instanceof MiddleEntity) {
            MiddleEntity stand = (MiddleEntity) ((CraftEntity)e.getEntity()).getHandle();
            StandKey.getStandKey(stand.getStandKeyID()).ifPresent(StandKey::detachMiddleEntity);
            //McBoyard.instance.getLogger().info("MiddleEntity unloaded ! (" + stand.getStandKeyID() + ")");
        }
        // Unload CrochetEntity
        if (e.getEntityType().equals(EntityType.SLIME) &&
                ((CraftEntity)e.getEntity()).getHandle() instanceof CrochetEntity) {
            CrochetEntity stand = (CrochetEntity) ((CraftEntity)e.getEntity()).getHandle();
            StandKey.getStandKey(stand.getStandKeyID()).ifPresent(standKey -> standKey.detachCrochetEntity(stand));
            //McBoyard.instance.getLogger().info("CrochetEntity unloaded ! (" + stand.getStandKeyID() + ")");
        }
        // Unload KeyEntity
        if (e.getEntityType().equals(EntityType.ARMOR_STAND) &&
                ((CraftEntity)e.getEntity()).getHandle() instanceof KeyEntity) {
            KeyEntity stand = (KeyEntity) ((CraftEntity)e.getEntity()).getHandle();
            StandKey.getStandKey(stand.getStandKeyID()).ifPresent(StandKey::detachKeyEntity);
            //McBoyard.instance.getLogger().info("KeyEntity unloaded ! (" + stand.getStandKeyID() + ")");
        }
    }

    private static void loadExcaliburStand(CraftEntity entity, Location loc) {
        // Load ExcaliburStand
        NamespacedKey escaliburStand_name = new NamespacedKey("excalibur","excalibur_stand");
        if (entity.getPersistentDataContainer().has(escaliburStand_name, PersistentDataType.TAG_CONTAINER)) {
            CraftPersistentDataContainer nbtC = (CraftPersistentDataContainer) entity.getPersistentDataContainer()
                    .get(escaliburStand_name, PersistentDataType.TAG_CONTAINER);
            if (nbtC != null) {
                NBTTagCompound nbt = nbtC.toTagCompound();
                // log nbt
                int standID = nbt.getInt("standID");
                int nbSwordDurability = nbt.getInt("nbSwordDurability");
                //McBoyard.instance.getLogger().info("ExcaliburStand prepare to load ! (" + standID + ") " + nbt.toString());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ExcaliburStand.spawn(loc, nbSwordDurability, standID);
                        entity.remove();
                        //McBoyard.instance.getLogger().info("ExcaliburStand loaded ! (" + standID + ") " + nbt.toString());
                    }
                }.runTaskLater(McBoyard.instance, 0);
            }
        }
    }

    public static void loadMiddleEntity(CraftEntity entity, Location loc) {
        NamespacedKey middle_entity_name = new NamespacedKey("excalibur","middle_entity");
        if (entity.getPersistentDataContainer().has(middle_entity_name, PersistentDataType.TAG_CONTAINER)) {
            CraftPersistentDataContainer nbtC = (CraftPersistentDataContainer) entity.getPersistentDataContainer()
                    .get(middle_entity_name, PersistentDataType.TAG_CONTAINER);
            if (nbtC != null) {
                NBTTagCompound nbt = nbtC.toTagCompound();
                // log nbt
                UUID standID = UUID.fromString(nbt.getString("standKeyID"));
                //McBoyard.instance.getLogger().info("StandKey prepare to load ! (Attach MiddleEntity) " + nbt.toString());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        StandKey standKey = StandKey.getStandKeyRegenIfNotLoad(standID);
                        boolean success = standKey.attachMiddleEntity(new MiddleEntity(((CraftWorld)loc.getWorld()).getHandle(),loc,standKey));
                        entity.remove();
                        if (success) {
                            //McBoyard.instance.getLogger().info("StandKey loaded ! (Attach MiddleEntity) " + nbt.toString());
                            //if (standKey.isComplete()) McBoyard.instance.getLogger().info("StandKey is complete ! (" + standKey.getID() + ") ");
                        }
                        else McBoyard.instance.getLogger().info("An error occured when loading StandKey ! (Attach MiddleEntity) " + nbt.toString());
                    }
                }.runTaskLater(McBoyard.instance, 0);
            }
        }
    }

    public static void loadKeyEntity(CraftEntity entity, Location loc) {
        NamespacedKey middle_entity_name = new NamespacedKey("excalibur","key_entity");
        if (entity.getPersistentDataContainer().has(middle_entity_name, PersistentDataType.TAG_CONTAINER)) {
            CraftPersistentDataContainer nbtC = (CraftPersistentDataContainer) entity.getPersistentDataContainer()
                    .get(middle_entity_name, PersistentDataType.TAG_CONTAINER);
            if (nbtC != null) {
                NBTTagCompound nbt = nbtC.toTagCompound();
                // log nbt
                UUID standID = UUID.fromString(nbt.getString("standKeyID"));
                //McBoyard.instance.getLogger().info("StandKey prepare to load ! (Attach KeyEntity) " + nbt.toString());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        StandKey standKey = StandKey.getStandKeyRegenIfNotLoad(standID);
                        boolean success = standKey.attachKeyEntity(new KeyEntity(((CraftWorld)loc.getWorld()).getHandle(),loc,standKey));
                        entity.remove();
                        if (success) {
                            //McBoyard.instance.getLogger().info("StandKey loaded ! (Attach KeyEntity) " + nbt.toString());
                            //if (standKey.isComplete()) McBoyard.instance.getLogger().info("StandKey is complete ! (" + standKey.getID() + ") ");
                        }
                        else McBoyard.instance.getLogger().info("An error occured when loading StandKey ! (Attach KeyEntity) " + nbt.toString());
                    }
                }.runTaskLater(McBoyard.instance, 0);
            }
        }
    }

    public static void loadCrochetEntity(CraftEntity entity, Location loc) {
        NamespacedKey middle_entity_name = new NamespacedKey("excalibur","crochet_entity");
        if (entity.getPersistentDataContainer().has(middle_entity_name, PersistentDataType.TAG_CONTAINER)) {
            CraftPersistentDataContainer nbtC = (CraftPersistentDataContainer) entity.getPersistentDataContainer()
                    .get(middle_entity_name, PersistentDataType.TAG_CONTAINER);
            if (nbtC != null) {
                NBTTagCompound nbt = nbtC.toTagCompound();
                // log nbt
                UUID standID = UUID.fromString(nbt.getString("standKeyID"));
                //McBoyard.instance.getLogger().info("StandKey prepare to load ! (Attach CrochetEntity) " + nbt.toString());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        StandKey standKey = StandKey.getStandKeyRegenIfNotLoad(standID);
                        boolean success = standKey.attachCrochetEntity(new CrochetEntity(((CraftWorld)loc.getWorld()).getHandle(),loc,standKey));
                        entity.remove();
                        if (success) {
                            //McBoyard.instance.getLogger().info("StandKey loaded ! (Attach CrochetEntity) " + nbt.toString());
                            //if (standKey.isComplete()) McBoyard.instance.getLogger().info("StandKey is complete ! (" + standKey.getID() + ") ");
                        }
                        else McBoyard.instance.getLogger().info("An error occured when loading StandKey ! (Attach CrochetEntity) " + nbt.toString());
                    }
                }.runTaskLater(McBoyard.instance, 0);
            }
        }
    }
}
