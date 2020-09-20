package eu.octanne.mcboyard.entity;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.GenericAttributes;

public class ChairEntity extends EntityArmorStand {

	public ChairEntity(World world) {
		super(((CraftWorld)world).getHandle());
		
		this.setCustomNameVisible(false);
		this.setCustomName("Chair");
		this.setSmall(true);
		this.setInvulnerable(true);
		this.setBasePlate(false);
		this.setInvisible(true);
		this.setHealth(0.5f);
		this.collides = false;
		this.setNoGravity(true);
		this.getAttributeInstance(GenericAttributes.maxHealth).setValue(0.5f);
	}
	
	/*public CraftEntity getBukkitEntity() {
		return this.bukkitEntity;
	}*/
	
}
