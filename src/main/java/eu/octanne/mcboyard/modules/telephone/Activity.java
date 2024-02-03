package eu.octanne.mcboyard.modules.telephone;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import eu.octanne.mcboyard.McBoyard;

public class Activity {
    private List<Entity> telephones = new ArrayList<>();
    private BukkitTask task = null;
    private Entity currentPhone = null;
    private Location currentPhoneLocation = null;
    private RingType currentRingType = null;
    private int ringTick = 0;

    private void placeRoom() {
        Material telephoneMaterial = Material.CARROT_ON_A_STICK;
        int telephoneCustomModelData = 7;
        ItemStack phoneItem = new ItemStack(telephoneMaterial);
        phoneItem.editMeta(meta -> meta.setCustomModelData(telephoneCustomModelData));

        // From -5 93 118 to 3 93 132, summon a phone with a step of 2
        for (int x = -5; x <= 3; x += 2) {
            for (int z = 118; z <= 132; z += 2) {
                Location loc = new Location(McBoyard.getWorld(), x + 0.5, 92.45, z + 0.5, 180, 0);
                ArmorStand phone = summonArmorStand(loc);
                phone.setItem(EquipmentSlot.HEAD, phoneItem.clone());
                telephones.add(phone);
            }
        }

        // From 12 93 114 to 24 93 124, summon a phone with a step of 2
        for (int x = 12; x <= 24; x += 2) {
            for (int z = 114; z <= 124; z += 2) {
                Location loc = new Location(McBoyard.getWorld(), x + 0.5, 92.45, z + 0.5, 90, 0);
                ArmorStand phone = summonArmorStand(loc);
                phone.setItem(EquipmentSlot.HEAD, phoneItem.clone());
                telephones.add(phone);
            }
        }
    }

    private static ArmorStand summonArmorStand(Location location) {
        ArmorStand phone = (ArmorStand) McBoyard.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        phone.setGravity(false);
        phone.setInvulnerable(true);
        phone.setArms(false);
        phone.setBasePlate(false);
        phone.setInvisible(true);
        phone.setDisabledSlots(EquipmentSlot.values());
        return phone;
    }

    private void removeRoom() {
        telephones.forEach(Entity::remove);
        telephones.clear();
    }

    public void start() {
        placeRoom();
        if (task != null) {
            task.cancel();
            task = null;
        }
        task = Bukkit.getScheduler().runTaskTimer(McBoyard.instance, this::tick, 0, 1);
    }

    public void stop() {
        removeRoom();
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        if (currentPhoneLocation != null) {
            ringTick++;
            currentRingType.tick(ringTick, currentPhoneLocation);
        }
    }

    public void setRingingPhone(int phone, RingType ringType) {
        if (this.currentRingType != null) {
            this.currentRingType.deinit(this);
        }
        if (phone < 0 || phone >= telephones.size()) {
            currentPhone = null;
            currentPhoneLocation = null;
            currentRingType = null;
            return;
        }
        ArmorStand phoneEntity = (ArmorStand) telephones.get(phone);
        currentPhone = phoneEntity;
        currentPhoneLocation = phoneEntity.getLocation().clone().add(0, 2, 0);
        currentRingType = ringType;
        currentRingType.init(this);
    }

    public void nextRingingPhone() {
        int phone = getRandomPhoneId();
        RingType ringType = RingType.values()[(int) (Math.random() * RingType.values().length)];
        setRingingPhone(phone, ringType);
    }

    public int getRandomPhoneId() {
        return (int) (Math.random() * telephones.size());
    }

    public boolean isPhone(Entity entity) {
        return telephones.contains(entity);
    }

    public boolean onPhoneInteract(Entity entity) {
        if (currentPhone != null && currentPhone.equals(entity)) {
            Location loc = currentPhoneLocation;
            loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            nextRingingPhone();
            return true;
        }
        return false;
    }

    protected void setTelephoneItem(Material material, int customModelData) {
        ItemStack phoneItem = new ItemStack(material);
        phoneItem.editMeta(meta -> meta.setCustomModelData(customModelData));
        telephones.forEach(phone -> ((ArmorStand) phone).setItem(EquipmentSlot.HEAD, phoneItem.clone()));
    }

    protected void resetTelephoneItem() {
        setTelephoneItem(Material.CARROT_ON_A_STICK, 7);
    }

    protected void moveTelephones(Vector delta) {
        telephones.forEach(phone -> phone.teleport(phone.getLocation().add(delta)));
    }
}
