package eu.octanne.mcboyard.entity;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.excalibur.StandKey;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import java.util.Optional;
import java.util.UUID;

public class CrochetEntity extends EntitySlime {

    private boolean toDie;

    private final UUID standKeyID;

    private StandKey standKey;

    public CrochetEntity(World world, Location loc, UUID standKeyID) {
        super(EntityTypes.SLIME, world);
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());

        this.toDie = false;
        this.setSilent(true);
        this.setInvulnerable(true);
        this.standKeyID = standKeyID;
        world.addEntity(this);
    }

    @Override
    public void tick() {
        if (!standKey.isUpdate()) {
            McBoyard.instance.getLogger().info("CrochetEntity de StandKey : "+getStandKeyID()+" lance la mise à jour");
            if (standKey.updateStandKeyInstance())
                McBoyard.instance.getLogger().info("CrochetEntity de StandKey : "+getStandKeyID()+" a fini la mise à jour");
            else McBoyard.instance.getLogger().info("CrochetEntity de StandKey : "+getStandKeyID()+" erreur lors de la mise à jour");
        }
        super.tick();
    }

    @Override
    public void die() {
        if (toDie) {
            this.die();
        }
    }

    @Override
    public void saveData(NBTTagCompound nbttagcompound) {
        NBTTagCompound nbtBase = new NBTTagCompound();
        nbtBase.setString("standKeyID", standKeyID.toString());
        getBukkitEntity().getPersistentDataContainer()
                .put(new NamespacedKey("excalibur","crochet_entity").toString(), nbtBase);
        super.saveData(nbttagcompound);
        McBoyard.instance.getLogger().info("Sauvegarde CrochetEntity de StandKey : "+getStandKeyID());
    }

    @Override
    public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
        return super.a(entityhuman, vec3d, enumhand);
    }

    public void despawn() {
        this.toDie = true;
        this.die();
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