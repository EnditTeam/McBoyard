package eu.octanne.mcboyard.modules.telephone;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import eu.octanne.mcboyard.McBoyard;

public class Activity {
    private List<Entity> telephones = new ArrayList<>();

    public void placeRoom() {
        Material telephoneMaterial = Material.CARROT_ON_A_STICK;
        int telephoneCustomModelData = 7;
        ItemStack phoneItem = new ItemStack(telephoneMaterial);
        phoneItem.editMeta(meta -> meta.setCustomModelData(telephoneCustomModelData));

        // From -5 93 118 to 3 93 132, summon a phone with a step of 2
        for (int x = -5; x <= 3; x += 2) {
            for (int z = 118; z <= 132; z += 2) {
                Location loc = new Location(McBoyard.getWorld(), x + 0.5, 92.5, z + 0.5, 180, 0);
                ArmorStand phone = summonArmorStand(loc);
                phone.setItem(EquipmentSlot.HEAD, phoneItem.clone());
                telephones.add(phone);
            }
        }

        // From 12 93 114 to 24 93 124, summon a phone with a step of 2
        for (int x = 12; x <= 24; x += 2) {
            for (int z = 114; z <= 124; z += 2) {
                Location loc = new Location(McBoyard.getWorld(), x + 0.5, 92.5, z + 0.5, 90, 0);
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

    public void removeRoom() {
        telephones.forEach(Entity::remove);
        telephones.clear();
    }

    public void start() {
        placeRoom();
    }

    public void stop() {
        removeRoom();
    }
}
