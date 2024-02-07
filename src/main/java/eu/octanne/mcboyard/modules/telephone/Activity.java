package eu.octanne.mcboyard.modules.telephone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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
    private Room currentRoom = null;
    private Entity currentPhone = null;
    private Location currentPhoneLocation = null;
    private RingType currentRingType = null;
    private int ringTick = 0;
    private static final int TICK_PAUSE_BEFORE_NEXT_RINGING_PHONE = -20;
    private Block blockTelephone1 = null;
    private Block blockTelephone2 = null;
    private Vector telephonesMoveDelta = new Vector(0, 0, 0);
    private boolean isCustomItem = false;

    private List<RingType> ringTypesToDo;
    private static final Location ROOM_CENTER = new Location(McBoyard.getWorld(), 8, 93, 124);

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
        currentRoom = null;
        currentPhone = null;
        currentPhoneLocation = null;
        currentRingType = null;
        resetBlockSocles();
        if (task != null) {
            task.cancel();
            task = null;
        }
        ringTypesToDo = new ArrayList<>(List.of(RingType.values()));
        task = Bukkit.getScheduler().runTaskTimer(McBoyard.instance, this::tick, 0, 1);
        ringTick = TICK_PAUSE_BEFORE_NEXT_RINGING_PHONE;
    }

    public void stop() {
        removeRoom();
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    protected int getRingTick() {
        return ringTick;
    }

    protected Location getRingLocation() {
        return currentPhoneLocation;
    }

    private void tick() {
        if (ringTick < 0) {
            // Délai de 20 ticks pour le prochain téléphone/reset des socles
            ringTick++;
            if (ringTick == 0) {
                if (currentRoom == Room.ROOM2) {
                    resetBlockSocles(); // Reset 1s after the link in room 2
                }
                nextRingingPhone();
            }
            return;
        }
        if (currentPhoneLocation != null) {
            if (currentRingType.isTimesUp(ringTick)) {
                // Restart the ring sequence by ringing in the first room
                resetBlockSocles();
                currentPhoneLocation.getWorld().playSound(currentPhoneLocation, Sound.BLOCK_CONDUIT_DEACTIVATE, 1, 1);
                getNearbyPlayers().forEach(
                    p -> p.sendMessage("§cLe téléphone a raccroché\n§rUn nouveau téléphone sonne dans la salle §averte"));
                Entity nextPhone = getRandomPhone(Room.ROOM1);
                setRingingPhone(nextPhone, currentRingType);
            } else {
                currentRingType.tick(this);
                ringTick++;
            }
        }
    }

    public void setRingingPhone(Entity phone, RingType ringType) {
        if (currentRingType != null && currentRingType != ringType) {
            currentRingType.stopSounds();
            resetTelephoneItem();
            resetTelephonesLocation();
        }
        if (phone == null || ringType == null) {
            currentRoom = null;
            currentPhone = null;
            currentPhoneLocation = null;
            currentRingType = null;
            return;
        }
        currentRoom = telephones1.contains(phone) ? Room.ROOM1 : Room.ROOM2;
        currentPhone = phone;
        currentPhoneLocation = phone.getLocation().clone().add(0, 2, 0);
        RingType previousRingType = currentRingType;
        currentRingType = ringType;
        ringTick = 0;
        if (previousRingType != currentRingType) {
            currentRingType.init(this);
        }
    }

    public void nextRingingPhone() {
        Entity nextPhone;
        RingType nextRingType;

        if (currentRoom == Room.ROOM1) {
            nextPhone = getRandomPhone(Room.ROOM2);
            nextRingType = currentRingType; // same ring type
            getNearbyPlayers().forEach(p -> p.sendMessage("Un téléphone sonne dans la salle §crouge"));
        } else {
            // Room.ROOM2 or null
            if (currentRoom == Room.ROOM2) {
                if (ringTypesToDo.isEmpty()) {
                    win();
                    return;
                }
            }
            nextPhone = getRandomPhone(Room.ROOM1);
            nextRingType = ringTypesToDo.get((int) (Math.random() * ringTypesToDo.size()));
            McBoyard.instance.getLogger().info("Telephone nextRingType: " + nextRingType);
            getNearbyPlayers().forEach(p -> p.sendMessage("Un téléphone sonne dans la salle §averte"));
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
        if (currentPhone != null && currentPhone.equals(entity) && ringTick > 0) {
            Location loc = currentPhoneLocation;
            if (currentRingType == RingType.DUCK) {
                loc.getWorld().playSound(loc, "minecraft:telephone/quack1", 1, 1);
            } else {
                loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
            this.currentRingType.stopSounds();
            ringTick = TICK_PAUSE_BEFORE_NEXT_RINGING_PHONE;

            if (currentRoom == Room.ROOM1) {
                blockTelephone1 = currentPhone.getLocation().getBlock().getRelative(
                    0 - telephonesMoveDelta.getBlockX(), 1 - telephonesMoveDelta.getBlockY(), 0 - telephonesMoveDelta.getBlockZ());
                blockTelephone1.setType(Material.GLOWSTONE);
                getNearbyPlayers().forEach(p -> p.sendMessage("Téléphone décroché"));
            } else {
                ringTypesToDo.remove(currentRingType);
                blockTelephone2 = currentPhone.getLocation().getBlock().getRelative(
                    0 - telephonesMoveDelta.getBlockX(), 1 - telephonesMoveDelta.getBlockY(), 0 - telephonesMoveDelta.getBlockZ());
                blockTelephone2.setType(Material.GLOWSTONE);
                int ringTypes = RingType.values().length;
                int ringTypesDone = ringTypes - getRingTypesToDoSize();
                getNearbyPlayers().forEach(
                    p -> p.sendMessage("Téléphone décroché, " + ringTypesDone + " / " + ringTypes + " téléphones liés"));
            }

            return true;
        }
        return false;
    }

    public int getRingTypesToDoSize() {
        return ringTypesToDo.size();
    }

    public static Collection<Player> getNearbyPlayers() {
        return ROOM_CENTER.getWorld().getNearbyPlayers(
            ROOM_CENTER, 18, 5, 11, p -> !p.isInvisible() && p.getGameMode() != org.bukkit.GameMode.SPECTATOR);
    }

    protected void setTelephoneItem(Material material, int customModelData) {
        ItemStack phoneItem = new ItemStack(material);
        phoneItem.editMeta(meta -> meta.setCustomModelData(customModelData));
        telephones.forEach(phone -> ((ArmorStand) phone).setItem(EquipmentSlot.HEAD, phoneItem.clone()));
        isCustomItem = true;
    }

    protected void resetTelephoneItem() {
        if (isCustomItem) {
            setTelephoneItem(Material.CARROT_ON_A_STICK, 7);
            isCustomItem = false;
        }
    }

    /**
     * Move all telephones relatively to their current position
     */
    protected void moveTelephones(Vector delta) {
        Vector cumule = telephonesMoveDelta == null ? delta : telephonesMoveDelta.add(delta);
        telephonesMoveDelta = cumule;
        telephones.forEach(phone -> phone.teleport(phone.getLocation().add(delta)));
    }

    protected List<Entity> getTelephones() {
        return telephones;
    }

    protected void resetTelephonesLocation() {
        // From -5 93 118 to 3 93 132, summon a phone with a step of 2
        int index = 0;
        for (int x = -5; x <= 3; x += 2) {
            for (int z = 118; z <= 132; z += 2) {
                Location loc = new Location(McBoyard.getWorld(), x + 0.5, 92.45, z + 0.5, 180, 0);
                Entity phone = telephones1.get(index);
                phone.teleport(loc);
                index++;
            }
        }

        // From 12 93 114 to 24 93 124, summon a phone with a step of 2
        index = 0;
        for (int x = 12; x <= 24; x += 2) {
            for (int z = 114; z <= 124; z += 2) {
                Location loc = new Location(McBoyard.getWorld(), x + 0.5, 92.45, z + 0.5, 90, 0);
                Entity phone = telephones2.get(index);
                phone.teleport(loc);
                index++;
            }
        }

        telephonesMoveDelta = new Vector(0, 0, 0);
    }

    private void win() {
        stop();
        McBoyard.instance.getLogger().info("Telephone win");
        Location loc = new Location(McBoyard.getWorld(), -6, 99, 114);
        loc.getBlock().setType(Material.REDSTONE_BLOCK, true); // fb_cle:actions/telephone/monte_c4
    }

    private void resetBlockSocles() {
        if (blockTelephone1 != null) {
            blockTelephone1.setType(Material.GREEN_WOOL);
            blockTelephone1 = null;
        }
        if (blockTelephone2 != null) {
            blockTelephone2.setType(Material.RED_WOOL);
            blockTelephone2 = null;
        }
    }
}
