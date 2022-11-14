package eu.octanne.mcboyard.entity.standkey;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.entity.CustomEntity;
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

public class KeyEntity extends EntityArmorStand {

    private boolean toDie;

    private final UUID standKeyID;

    private StandKey standKey;

    private static org.bukkit.inventory.ItemStack keyItem;

    static {
        keyItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BOWL);
        // set DisplayName
        org.bukkit.inventory.meta.ItemMeta meta = keyItem.getItemMeta();
        meta.setDisplayName("Clé (Y)");
        keyItem.setItemMeta(meta);
    }

    public KeyEntity(World world, Location loc, StandKey standKey) {
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
        this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.BOWL));
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public void lootKey() {
        Location loc = getStandKey().getBukkitLocation().clone();
        loc.setY(loc.getY() + 1);
        loc.setX(loc.getX() + 0.5);
        loc.setZ(loc.getZ() + 0.5);
        getBukkitEntity().getWorld().dropItem(loc, keyItem);
        this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.AIR));
    }

    public void restoreKey() {
        this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.BOWL));
        setTagEntity();
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
        //McBoyard.instance.getLogger().info("Sauvegarde KeyEntity de StandKey : "+getStandKeyID());
    }

    public void saveCustomData() {
        NBTTagCompound nbtBase = new NBTTagCompound();
        nbtBase.setString("standKeyID", standKeyID.toString());
        getBukkitEntity().getPersistentDataContainer()
                .put(new NamespacedKey("excalibur","key_entity").toString(), nbtBase);
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