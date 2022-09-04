package eu.octanne.mcboyard;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

public class Utils {

	// en.getBukkitEntity().getLocation().distance(loc2) > 0.5
	/*SchedulerTask task = new SchedulerTask(0,1){

		int maxItr = 10;
		int idx = 0;

		Vector vecLoc = vec.multiply(0.1);

		@Override
		public void run() {
			if(idx < maxItr) {
				idx++;
				Bukkit.broadcastMessage("Vector : "+vecLoc.toString()+" idx = "+idx);
				en.move(EnumMoveType.SELF, vecLoc.getX(), vecLoc.getY(), vecLoc.getZ());
			}
			if(idx == maxItr-1) {
				// ACTION DE FIN DE COURSE
				if(p != null){
					p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 1.0f);
					p.getScoreboardTags().remove("onTyro");
				}
				en.needToDie = true;
				en.killEntity();
				cancelTask();
			}
		}
	};
	task.startTask();*/
	
	public static class SchedulerTask implements Runnable {
		
		private int id;
		private long delay;
		private long repeatDelay;
		
		public SchedulerTask(long delay, long repeatDelay) {
			this.delay = delay;
			this.repeatDelay = repeatDelay;
		}
		
		public int getId() {
			return id;
		}
		
		public void setId(int id) {
			this.id = id;
		}
		
		public void cancelTask() {
			Bukkit.getScheduler().cancelTask(id);
		}
		
		public void startTask() {
			id = Bukkit.getScheduler().scheduleSyncRepeatingTask(McBoyard.instance, this, delay, repeatDelay);
		}
		
		@Override
		public void run() {
			
		}
	}

	static public Vector calcVect(Location loc1, Location loc2) {
		return new Vector(loc2.getX()-loc1.getX(),
				loc2.getY()-loc1.getY(),loc2.getZ()-loc1.getZ());
	}
	
	static public Vector divideVect(Vector vect,int divisor) {
		vect.setX(vect.getX()/divisor); 
		vect.setY(vect.getY()/divisor);
		vect.setZ(vect.getZ()/divisor);
		return vect;
	}
	
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

		ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1, (short) Type.ordinal());
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
