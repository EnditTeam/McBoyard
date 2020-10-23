package eu.octanne.mcboyard;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Utils {

	static public double round(double number) {
		return Math.round(number*1000000.0)/1000000.0;
	}
	
	static public String getDirection(float yaw) {
		// YAW IN DEGREE
		double rotation = (yaw - 90.0F) % 360.0F;
		if(rotation < 0.0D)rotation += 360.0D;
		// NORTH
		if((rotation < 135.0D && rotation >= 45.0D)) {
			return "NORTH";
		}
		// WEST DONE
		if((rotation <= 360.0D && rotation >= 315.0D) || (rotation >= 0.0D && rotation < 45.0D)) {
			return "WEST";
		}
		// SOUTH DONE
		if(rotation < 315.0D && rotation >= 225.0D) {
			return "SOUTH";
		}
		// EAST DONE
		if(rotation < 225.0D && rotation >= 135.0D) {
			return "EAST";
		}
		return null;
	}
	
	// CREATE ITEM WITH DATA
	@SuppressWarnings("deprecation")
	static public ItemStack createItemStack(String DisplayName, Material id, int QteItem, ArrayList<String> Lore,
			int data, boolean Glowing, boolean unbreakable) {

		if(Lore == null) Lore = new ArrayList<>();
		
		ItemStack item = new ItemStack(id, QteItem, (short) 0, (byte) data);
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setDisplayName(DisplayName);
		itemmeta.setLore(Lore);
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		itemmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		if (Glowing) {
			itemmeta.addEnchant(Enchantment.ARROW_FIRE, 10, true);
		}
		itemmeta.setUnbreakable(unbreakable);
		item.setItemMeta(itemmeta);
		return item;
	}

	@SuppressWarnings("deprecation")
	static public ItemStack createItemStack(String DisplayName, Material id, int QteItem, ArrayList<String> Lore,
			int data, ItemMeta meta, boolean unbreakable) {

		if(Lore == null) Lore = new ArrayList<>();
		
		ItemStack item = new ItemStack(id, QteItem, (short) 0, (byte) data);
		ItemMeta itemmeta = meta;
		itemmeta.setDisplayName(DisplayName);
		itemmeta.setLore(Lore);
		itemmeta.setUnbreakable(unbreakable);
		item.setItemMeta(itemmeta);
		return item;
	}

	// CREATE ITEM SKULL WITHOUT DATA
	@SuppressWarnings("deprecation")
	static public ItemStack createItemSkull(String DisplayName, ArrayList<String> Lore, SkullType Type, String Owner,
			boolean Glowing) {

		if(Lore == null) Lore = new ArrayList<>();
		
		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) Type.ordinal());
		SkullMeta itemmeta = (SkullMeta) item.getItemMeta();
		itemmeta.setLore(Lore);
		itemmeta.setDisplayName(DisplayName);
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		itemmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		itemmeta.setOwningPlayer(Bukkit.getOfflinePlayer(Owner));
		if (Glowing) {
			itemmeta.addEnchant(Enchantment.DURABILITY, 10, true);
		}
		item.setItemMeta(itemmeta);
		return item;
	}
	
}
