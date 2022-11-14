package eu.octanne.mcboyard.entity.standkey;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.entity.CustomEntity;
import eu.octanne.mcboyard.modules.ExcaliburSystem;
import eu.octanne.mcboyard.modules.excalibur.StandKey;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.UUID;

public class MiddleEntity extends EntityArmorStand {

    private boolean toDie;

    private final UUID standKeyID;

    private StandKey standKey;

    public MiddleEntity(World world, Location loc, StandKey standKey) {
        super(EntityTypes.ARMOR_STAND, world);
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());

        setTagEntity();
        this.standKeyID = standKey.getID();
        this.standKey = standKey;

        saveCustomData();
        world.addEntity(this);
    }

    private void setTagEntity() {
        toDie = false;
        this.setSilent(true);
        this.setInvisible(true);
        this.setArms(false);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void die() {
        if (toDie) {
            super.die();
        }
    }

    @Override
    public void saveData(NBTTagCompound nbttagcompound) {
        saveCustomData();
        super.saveData(nbttagcompound);
        //McBoyard.instance.getLogger().info("Sauvegarde MiddleEntity de StandKey : "+getStandKeyID());
    }

    public void saveCustomData() {
        NBTTagCompound nbtBase = new NBTTagCompound();
        nbtBase.setString("standKeyID", standKeyID.toString());
        getBukkitEntity().getPersistentDataContainer()
                .put(new NamespacedKey("excalibur","middle_entity").toString(), nbtBase);
    }



    @Override
    public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
        return EnumInteractionResult.PASS;
        // super.a(entityhuman, vec3d, enumhand);
    }

    public void despawn() {
        this.toDie = true;
        getBukkitEntity().remove();
    }

    public UUID getStandKeyID() {
        return standKeyID;
    }

    public StandKey getStandKey() {
        if (standKey == null) {
            Optional<StandKey> optional = StandKey.getStandKey(standKeyID);
            optional.ifPresent(key -> standKey = key);
        }

        return standKey;
    }
}