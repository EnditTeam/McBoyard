package eu.octanne.mcboyard.entity;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.excalibur.StandKey;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

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
        world.addEntity(this);
    }

    public MiddleEntity(World world, Location loc, UUID standKeyID) {
        super(EntityTypes.ARMOR_STAND, world);
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());

        setTagEntity();
        this.standKeyID = standKeyID;
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
        if (!getStandKey().isUpdate()) {
            McBoyard.instance.getLogger().info("MiddleEntity de StandKey : "+getStandKeyID()+" lance la mise à jour");
            if (getStandKey().updateStandKeyInstance())
                McBoyard.instance.getLogger().info("MiddleEntity de StandKey : "+getStandKeyID()+" a fini la mise à jour");
            else McBoyard.instance.getLogger().info("MiddleEntity de StandKey : "+getStandKeyID()+" erreur lors de la mise à jour");
        }
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
        NBTTagCompound nbtBase = new NBTTagCompound();
        nbtBase.setString("standKeyID", standKeyID.toString());
        getBukkitEntity().getPersistentDataContainer()
                .put(new NamespacedKey("excalibur","middle_entity").toString(), nbtBase);
        super.saveData(nbttagcompound);
        McBoyard.instance.getLogger().info("Sauvegarde MiddleEntity de StandKey : "+getStandKeyID());
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