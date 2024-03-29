package eu.octanne.mcboyard.entity;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.ExcaliburSystem;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.Damageable;

public class ExcaliburStand extends EntityArmorStand {

    private int nbSwordDurability = 5;
    private boolean hasSword = true;
    private int standID;

    private boolean toRemove = false;

    public ExcaliburStand(World world, Location loc, int nbSwordDurability, int standID) {
        super(EntityTypes.ARMOR_STAND, world);

        this.standID = standID;
        this.nbSwordDurability = nbSwordDurability;
        super.setInvisible(true);
        super.setInvulnerable(true);
        super.setNoGravity(true);
        super.setBasePlate(false);
        super.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        // Set Iron Sword In Hand
        super.setSlot(EnumItemSlot.HEAD, new net.minecraft.server.v1_16_R3.ItemStack(net.minecraft.server.v1_16_R3.Items.IRON_SWORD));
        // set durability of sword
        super.getEquipment(EnumItemSlot.MAINHAND).setDamage(nbSwordDurability);
    }

    public boolean takeSword(Player p) {
        // give sword to player if hasSword is true
        if(hasSword) {
            // create sword with durability
            org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(Material.IRON_SWORD);
            // set Name
            org.bukkit.inventory.meta.ItemMeta meta = sword.getItemMeta();
            meta.setDisplayName("§bExcalibur");
            // set durability of sword
            ((Damageable)meta).setDamage(250 - nbSwordDurability);
            sword.setItemMeta(meta);
            // give sword to player
            p.getInventory().addItem(sword);
            hasSword = false;
            // remove sword in hand
            super.setSlot(EnumItemSlot.HEAD, new net.minecraft.server.v1_16_R3.ItemStack(net.minecraft.server.v1_16_R3.Items.AIR));
            return true;
        } else return false;
    }

    public void putBackSword() {
        // Set Iron Sword In Hand
        super.setSlot(EnumItemSlot.HEAD, new net.minecraft.server.v1_16_R3.ItemStack(net.minecraft.server.v1_16_R3.Items.IRON_SWORD));
        // set durability of sword
        super.getEquipment(EnumItemSlot.MAINHAND).setDamage(super.getEquipment(EnumItemSlot.MAINHAND).getDamage() - nbSwordDurability);
        hasSword = true;
    }

    public void lockInventory() {
        ((CraftArmorStand)super.getBukkitEntity()).addEquipmentLock(EquipmentSlot.HEAD, CraftArmorStand.LockType.ADDING_OR_CHANGING);
        ((CraftArmorStand)super.getBukkitEntity()).addEquipmentLock(EquipmentSlot.CHEST, CraftArmorStand.LockType.ADDING_OR_CHANGING);
        ((CraftArmorStand)super.getBukkitEntity()).addEquipmentLock(EquipmentSlot.LEGS, CraftArmorStand.LockType.ADDING_OR_CHANGING);
        ((CraftArmorStand)super.getBukkitEntity()).addEquipmentLock(EquipmentSlot.FEET, CraftArmorStand.LockType.ADDING_OR_CHANGING);
        ((CraftArmorStand)super.getBukkitEntity()).addEquipmentLock(EquipmentSlot.HAND, CraftArmorStand.LockType.ADDING_OR_CHANGING);
        ((CraftArmorStand)super.getBukkitEntity()).addEquipmentLock(EquipmentSlot.OFF_HAND, CraftArmorStand.LockType.ADDING_OR_CHANGING);
    }

    public static void spawn(Location loc, int nbSwordDurability) {
        ExcaliburStand stand = new ExcaliburStand(((CraftWorld)loc.getWorld()).getHandle(), loc,
                nbSwordDurability, getDispoID(ExcaliburSystem.getExcaliburStands().size()));
        ((CraftWorld)loc.getWorld()).getHandle().addEntity(stand);
        ExcaliburSystem.addExcaliburStand(stand);
    }

    private static int getDispoID(int startID) {
        int idChoose = startID;
        boolean alreadyUse = false;
        for (ExcaliburStand stand : ExcaliburSystem.getExcaliburStands()) {
            if(stand.standID == idChoose) {
                alreadyUse = true;
                break;
            }
        }
        return alreadyUse ? getDispoID(startID + 1) : idChoose;
    }

    public static void spawn(Location loc, int nbSwordDurability, int standID) {
        ExcaliburStand stand = new ExcaliburStand(((CraftWorld)loc.getWorld()).getHandle(), loc,
                nbSwordDurability, standID);
        ((CraftWorld)loc.getWorld()).getHandle().addEntity(stand);
        ExcaliburSystem.addExcaliburStand(stand);
    }

    public static boolean despawn(int numStand) {
        // Search excalibur stand with numStand
        for(ExcaliburStand stand : ExcaliburSystem.getExcaliburStands()) {
            if(stand.standID == numStand) {
                stand.toRemove = true;
                stand.die();
                ExcaliburSystem.removeExcaliburStand(stand);
                return true;
            }
        }

        return false;
    }

    public int getNbSwordDurability() {
        return nbSwordDurability;
    }

    public int getStandId() {
        return standID;
    }

    public String getStandName() {
        return "ExcaliburStand_"+standID;
    }

    public String getStandLocation() {
        // Show Location of ExcaliburStand
        return "X: "+super.locX()+" Y: "+super.locY()+" Z: "+super.locZ()+" World: "+super.world.getWorld().getName();
    }

    @Override
    public void saveData(NBTTagCompound nbttagcompound) {
        NBTTagCompound nbtBase = new NBTTagCompound();
        nbtBase.setInt("nbSwordDurability", nbSwordDurability);
        nbtBase.setInt("standID", standID);
        getBukkitEntity().getPersistentDataContainer().put(new NamespacedKey("excalibur","excalibur_stand").toString(), nbtBase);
        super.saveData(nbttagcompound);
        //McBoyard.instance.getLogger().info("Sauvegarde de l'entité "+getStandName());
    }

    @Override
    public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
        // Take Sword
        if(entityhuman.getBukkitEntity() instanceof Player) {
            if (takeSword((Player) entityhuman.getBukkitEntity())) {
                entityhuman.getBukkitEntity().sendMessage("§aVous avez récupéré l'épée Excalibur");
            } else {
                entityhuman.getBukkitEntity().sendMessage("§cL'épée Excalibur est déjà prise");
            }
        }
        return EnumInteractionResult.SUCCESS;
    }

    @Override
    public void die() {
        if (toRemove) {
            McBoyard.instance.getLogger().info("Destruction de l'ExcaliburStand : "+getStandName());
            ExcaliburSystem.removeExcaliburStand(this);
            super.die();
        } //else McBoyard.instance.getLogger().info("ExcaliburStand "+getStandName()+" ne peut pas être détruit");
    }

    @Override
    public void tick() {
        super.tick();
        if (ExcaliburSystem.excaliburUpdate == standID) {
            ExcaliburSystem.excaliburUpdate = -2;
            // Log fin des réenregistrements
            McBoyard.instance.getLogger().info("Re-enregistrement des ExcaliburStand terminé !");
        }
        else if (ExcaliburSystem.excaliburUpdate != -2 && !ExcaliburSystem.hadExcaliburStand(this)) {
            ExcaliburSystem.addExcaliburStand(this);
            // log le réenregistrement de l'entité
            McBoyard.instance.getLogger().info("Re-enregistrement de "+getStandName());
            if (ExcaliburSystem.excaliburUpdate == -1) ExcaliburSystem.excaliburUpdate = standID;
        }
    }

}
