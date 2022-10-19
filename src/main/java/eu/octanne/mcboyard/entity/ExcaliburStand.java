package eu.octanne.mcboyard.entity;

import eu.octanne.mcboyard.modules.ExcaliburSystem;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class ExcaliburStand extends EntityArmorStand {

    private int nbSwordDurability = 5;
    private boolean hasSword = false;
    private int standID;

    private boolean toRemove = false;

    public ExcaliburStand(EntityTypes entityTypes, World world) {
        super(entityTypes, world);
    }
    public ExcaliburStand(World world, Location loc, int nbSwordDurability, int standID) {
        super(CustomEntity.EXCALIBUR_STAND, world);

        this.standID = standID;
        this.nbSwordDurability = nbSwordDurability;
        lockInventory();
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

    public void takeSword(Player p) {
        // give sword to player if hasSword is true
        if(hasSword) {
            // create sword with durability
            org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(Material.IRON_SWORD);
            // set durability of sword
            sword.setDurability((short) (nbSwordDurability));
            // give sword to player
            p.getInventory().addItem(sword);
            hasSword = false;
            // remove sword in hand
            super.setSlot(EnumItemSlot.HEAD, new ItemStack(net.minecraft.server.v1_16_R3.Items.AIR));
        }
    }

    public void putBackSword() {
        // Set Iron Sword In Hand
        super.setSlot(EnumItemSlot.HEAD, new net.minecraft.server.v1_16_R3.ItemStack(net.minecraft.server.v1_16_R3.Items.IRON_SWORD));
        // set durability of sword
        super.getEquipment(EnumItemSlot.MAINHAND).setDamage(nbSwordDurability);
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
                nbSwordDurability, ExcaliburSystem.getExcaliburStands().size());
        ((CraftWorld)loc.getWorld()).getHandle().addEntity(stand);
        ExcaliburSystem.addExcaliburStand(stand);
    }

    public static void despawn(int numStand) {
        ExcaliburStand stand = ExcaliburSystem.getExcaliburStands().get(numStand);
        stand.die();
        ExcaliburSystem.removeExcaliburStand(stand);
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
        super.saveData(nbttagcompound);
        nbttagcompound.setInt("nbSwordDurability", nbSwordDurability);
        nbttagcompound.setInt("standID", standID);
        Bukkit.broadcastMessage("Sauvegarde de l'entité "+getStandName());
    }

    @Override
    public void loadData(NBTTagCompound nbttagcompound) {
        super.loadData(nbttagcompound);
        nbSwordDurability = nbttagcompound.getInt("nbSwordDurability");
        standID = nbttagcompound.getInt("standID");
        if (!ExcaliburSystem.getExcaliburStands().contains(this)) {
            ExcaliburSystem.addExcaliburStand(this);
            Bukkit.broadcastMessage("Rechargement de l'ExcaliburStand : "+getStandName());
        } else {
            Bukkit.broadcastMessage("ExcaliburStand "+getStandName()+" déjà chargé");
        }
    }

    @Override
    public EnumInteractionResult a(EntityHuman entityhuman, EnumHand enumhand) {
        Bukkit.broadcastMessage("Interact with "+getStandName());
        return super.a(entityhuman, enumhand);
    }

    @Override
    public void die() {
        if (toRemove) {
            Bukkit.broadcastMessage("Destruction de l'ExcaliburStand : "+getStandName());
            ExcaliburSystem.removeExcaliburStand(this);
            super.die();
        } else Bukkit.broadcastMessage("ExcaliburStand "+getStandName()+" ne peut pas être détruit");
    }
}
