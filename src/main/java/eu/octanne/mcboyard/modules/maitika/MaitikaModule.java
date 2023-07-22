package eu.octanne.mcboyard.modules.maitika;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.PlugModule;
import eu.octanne.mcboyard.utils.doors.BuildUtils;

public class MaitikaModule extends PlugModule {
    private MaitikaBattle battle = null;

    public MaitikaModule(JavaPlugin instance) {
        super(instance);
    }

    @Override
    public void onEnable() {
        MaitikaCommand maitikaCommand = new MaitikaCommand();
        pl.getCommand("maitika").setExecutor(maitikaCommand);
        pl.getCommand("maitika").setTabCompleter(maitikaCommand);
    }

    @Override
    public void onDisable() {
        if (battle != null)
            battle.stop();
    }

    public void placeArena() {
        // 34 72 -55 => 39 75 -50 with iron bars
        World w = McBoyard.getWorld();
        BuildUtils.walls(new Location(w, 34, 72, -55), new Location(w, 39, 75, -50), Material.AIR, Material.IRON_BARS,
                BuildUtils::connectFenceWithSameType);
        // play iron sound at each corner
        w.playSound(new Location(w, 34, 72, -55), Sound.BLOCK_ANVIL_PLACE, 1, 1);
        w.playSound(new Location(w, 39, 72, -55), Sound.BLOCK_ANVIL_PLACE, 1, 1);
        w.playSound(new Location(w, 34, 72, -50), Sound.BLOCK_ANVIL_PLACE, 1, 1);
        w.playSound(new Location(w, 39, 72, -50), Sound.BLOCK_ANVIL_PLACE, 1, 1);
    }

    public void removeArena() {
        // 34 72 -55 => 39 75 -50 with air
        World w = McBoyard.getWorld();
        BuildUtils.walls(new Location(w, 34, 72, -55), new Location(w, 39, 75, -50), Material.IRON_BARS, Material.AIR,
                b -> {
                });
        w.playSound(new Location(w, 34, 72, -55), Sound.BLOCK_CHAIN_BREAK, 1, 1);
        w.playSound(new Location(w, 39, 72, -55), Sound.BLOCK_CHAIN_BREAK, 1, 1);
        w.playSound(new Location(w, 34, 72, -50), Sound.BLOCK_CHAIN_BREAK, 1, 1);
        w.playSound(new Location(w, 39, 72, -50), Sound.BLOCK_CHAIN_BREAK, 1, 1);
    }

    public Location getArenaCenter() {
        return new Location(McBoyard.getWorld(), 36.5, 73, -52.5);
    }

    public MaitikaEntity spawnMaitika() {
        return new MaitikaEntity(getArenaCenter());
    }

    public void removeMaitika() {
        MaitikaEntity.getMaitikaEntities(McBoyard.getWorld()).forEach(entity -> entity.remove());
    }

    public void equipGoodArmor(Player player) {
        EntityEquipment equipment = player.getEquipment();
        equipment.setHelmet(null);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestplateMeta.setColor(Color.fromRGB(255, 255, 255));
        chestplateMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                "generic.armor", 10, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
        chestplateMeta.setUnbreakable(true);
        chestplateMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        chestplate.setItemMeta(chestplateMeta);
        equipment.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(Color.fromRGB(90, 82, 71));
        leggingsMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                "generic.armor", 10, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
        leggingsMeta.setUnbreakable(true);
        leggingsMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        leggings.setItemMeta(leggingsMeta);
        equipment.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(Color.fromRGB(82, 66, 49));
        bootsMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                "generic.armor", 10, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
        bootsMeta.setUnbreakable(true);
        bootsMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        boots.setItemMeta(bootsMeta);
        equipment.setBoots(boots);

        ItemStack sword = new ItemStack(Material.GOLDEN_SWORD);
        // set attack speed
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", 100,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        sword.setItemMeta(swordMeta);
        equipment.setItemInMainHand(sword);

        if (!player.getInventory().contains(Material.COOKED_BEEF, 32)) {
            ItemStack food = new ItemStack(Material.COOKED_BEEF, 32);
            player.getInventory().addItem(food);
        }
    }

    public void equipBadArmor(Player player) {
        EntityEquipment equipment = player.getEquipment();
        equipment.setHelmet(null);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestplateMeta.setColor(Color.fromRGB(255, 255, 255));
        Damageable chestplateDamageable = (Damageable) chestplateMeta;
        chestplateDamageable.setDamage(77);
        chestplate.setItemMeta(chestplateMeta);
        equipment.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(Color.fromRGB(90, 82, 71));
        leggingsMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                "generic.armor", 10, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
        Damageable leggingsDamageable = (Damageable) leggingsMeta;
        leggingsDamageable.setDamage(70);
        leggings.setItemMeta(leggingsMeta);
        equipment.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(Color.fromRGB(82, 66, 49));
        bootsMeta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(),
                "generic.movement_speed", -0.05, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
        bootsMeta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(),
                "generic.knockback_resistance", 5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
        bootsMeta.setUnbreakable(true);
        bootsMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        boots.setItemMeta(bootsMeta);
        equipment.setBoots(boots);

        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1200, 128, false, false, false));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
    }

    public void startBattle() {
        if (battle != null)
            return;
        battle = new MaitikaBattle(b -> battle = null);
        battle.start();
    }

    public void stopBattle() {
        if (battle == null)
            return;
        battle.stop();
    }
}
