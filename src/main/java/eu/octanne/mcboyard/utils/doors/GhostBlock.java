package eu.octanne.mcboyard.utils.doors;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GhostBlock {
    private final ArmorStand armorStand;
    private final Vector offset;
    private final float offsetYaw;

    private GhostBlock(@NotNull ArmorStand armorStand, @NotNull Vector offset, float offsetYaw) {
        this.armorStand = armorStand;
        this.offset = offset;
        this.offsetYaw = offsetYaw;
    }

    public static @Nullable GhostBlock spawn(@NotNull Location target, @NotNull GhostBlockData ghostData) {
        Location location = getLocation(target, ghostData.offset, ghostData.offsetYaw);
        if (!location.getChunk().isLoaded()) {
            return null;
        }

        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.addScoreboardTag("McBoyard_GhostBlock");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            armorStand.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING);
        }
        armorStand.getEquipment().setHelmet(ghostData.createItem());
        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        return new GhostBlock(armorStand, ghostData.offset, ghostData.offsetYaw);
    }

    public void despawn() {
        armorStand.remove();
    }

    private static Location getLocation(@NotNull Location location, @NotNull Vector offset, float offsetYaw) {
        location = location.clone().add(offset);
        location.setYaw(location.getYaw() + offsetYaw);
        return location;
    }

    public void teleport(@NotNull Location target) {
        armorStand.teleport(getLocation(target, offset, offsetYaw));
    }
}
