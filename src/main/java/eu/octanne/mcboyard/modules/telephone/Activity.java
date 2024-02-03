package eu.octanne.mcboyard.modules.telephone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import eu.octanne.mcboyard.McBoyard;

public class Activity {
    private List<Entity> telephones = new ArrayList<>();
    private List<Entity> telephones1 = new ArrayList<>();
    private List<Entity> telephones2 = new ArrayList<>();
    private BukkitTask task = null;
    private Entity currentPhone = null;
    private Location currentPhoneLocation = null;
    private RingType currentRingType = null;
    private int ringTick = 0;

    private List<RingType> ringTypesToDo;

    public enum Room { ROOM1, ROOM2 }

    private void placeRoom() {
        Material telephoneMaterial = Material.CARROT_ON_A_STICK;
        int telephoneCustomModelData = 7;
        ItemStack phoneItem = new ItemStack(telephoneMaterial);
        phoneItem.editMeta(meta -> meta.setCustomModelData(telephoneCustomModelData));

        // Kill existing armor stands
        McBoyard.getWorld()
            .getEntitiesByClass(ArmorStand.class)
            .stream()
            .filter(armorstand -> armorstand.getScoreboardTags().contains("mcboyard_telephone"))
            .forEach(Entity::remove);

        // From -5 93 118 to 3 93 132, summon a phone with a step of 2
        for (int x = -5; x <= 3; x += 2) {
            for (int z = 118; z <= 132; z += 2) {
                Location loc = new Location(McBoyard.getWorld(), x + 0.5, 92.45, z + 0.5, 180, 0);
                ArmorStand phone = summonArmorStand(loc);
                phone.setItem(EquipmentSlot.HEAD, phoneItem.clone());
                telephones.add(phone);
                telephones1.add(phone);
            }
        }

        // From 12 93 114 to 24 93 124, summon a phone with a step of 2
        for (int x = 12; x <= 24; x += 2) {
            for (int z = 114; z <= 124; z += 2) {
                Location loc = new Location(McBoyard.getWorld(), x + 0.5, 92.45, z + 0.5, 90, 0);
                ArmorStand phone = summonArmorStand(loc);
                phone.setItem(EquipmentSlot.HEAD, phoneItem.clone());
                telephones.add(phone);
                telephones2.add(phone);
            }
        }

        Location loc = new Location(McBoyard.getWorld(), -6, 101, 114);
        loc.getBlock().setType(Material.REDSTONE_BLOCK, true); // fb_cle:actions/telephone/reset_c4
    }

    private static ArmorStand summonArmorStand(Location location) {
        ArmorStand phone = (ArmorStand) McBoyard.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        phone.setGravity(false);
        phone.setInvulnerable(true);
        phone.setArms(false);
        phone.setBasePlate(false);
        phone.setInvisible(true);
        phone.setDisabledSlots(EquipmentSlot.values());
        phone.addScoreboardTag("mcboyard_telephone");
        return phone;
    }

    private void removeRoom() {
        telephones.forEach(Entity::remove);
        telephones.clear();
        telephones1.clear();
        telephones2.clear();
    }

    public void start() {
        placeRoom();
        currentPhone = null;
        currentPhoneLocation = null;
        currentRingType = null;
        if (task != null) {
            task.cancel();
            task = null;
        }
        ringTypesToDo = new ArrayList<>(List.of(RingType.values()));
        task = Bukkit.getScheduler().runTaskTimer(McBoyard.instance, this::tick, 0, 1);
        nextRingingPhone();
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
            if (currentRingType.isTimesUp(ringTick)) {
                // Restart the ring sequence by ringing in the first room
                Entity nextPhone = getRandomPhone(Room.ROOM1);
                setRingingPhone(nextPhone, currentRingType);
            } else {
                currentRingType.tick(ringTick, currentPhoneLocation);
                ringTick++;
            }
        }
    }

    public void setRingingPhone(Entity phone, RingType ringType) {
        if (this.currentRingType != null) {
            this.currentRingType.deinit(this);
        }
        if (phone == null || ringType == null) {
            currentPhone = null;
            currentPhoneLocation = null;
            currentRingType = null;
            return;
        }
        currentPhone = phone;
        currentPhoneLocation = phone.getLocation().clone().add(0, 2, 0);
        currentRingType = ringType;
        ringTick = 0;
        currentRingType.init(this);
    }

    public void nextRingingPhone() {
        Room currentRoom = getCurrentRoom();
        Entity nextPhone;
        RingType nextRingType;

        if (currentRoom == Room.ROOM1) {
            nextPhone = getRandomPhone(Room.ROOM2);
            nextRingType = currentRingType; // same ring type
        } else {
            // Room.ROOM2 or null
            if (currentRoom == Room.ROOM2) {
                ringTypesToDo.remove(currentRingType);
                if (ringTypesToDo.isEmpty()) {
                    win();
                    return;
                }
            }
            nextPhone = getRandomPhone(Room.ROOM1);
            nextRingType = ringTypesToDo.get((int) (Math.random() * ringTypesToDo.size()));
            McBoyard.instance.getLogger().info("Telephone nextRingType: " + nextRingType);
        }

        setRingingPhone(nextPhone, nextRingType);
    }

    public Entity getRandomPhone(Room room) {
        if (room == Room.ROOM1) {
            return telephones1.get((int) (Math.random() * telephones1.size()));
        }
        return telephones2.get((int) (Math.random() * telephones2.size()));
    }

    public boolean isPhone(Entity entity) {
        return telephones.contains(entity);
    }

    public boolean onPhoneInteract(Entity entity) {
        if (currentPhone != null && currentPhone.equals(entity)) {
            Location loc = currentPhoneLocation;
            if (currentRingType == RingType.DUCK) {
                loc.getWorld().playSound(loc, "minecraft:telephone/quack1", 1, 1);
            } else {
                loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
            nextRingingPhone();
            return true;
        }
        return false;
    }

    public int getRingTypesToDoSize() {
        return ringTypesToDo.size();
    }

    public Room getCurrentRoom() {
        if (currentPhone != null) {
            if (telephones1.contains(currentPhone)) {
                return Room.ROOM1;
            } else if (telephones2.contains(currentPhone)) {
                return Room.ROOM2;
            }
        }
        return null;
    }

    public static Collection<Player> getNearbyPlayers() {
        Location loc = new Location(McBoyard.getWorld(), 8, 93, 122);
        return loc.getWorld().getNearbyPlayers(loc, 30, 10, 20, p -> !p.isInvisible() && p.getGameMode() != org.bukkit.GameMode.SPECTATOR);
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

    private void win() {
        stop();
        McBoyard.instance.getLogger().info("Telephone win");
        Location loc = new Location(McBoyard.getWorld(), -6, 99, 114);
        loc.getBlock().setType(Material.REDSTONE_BLOCK, true); // fb_cle:actions/telephone/monte_c4
    }
}
