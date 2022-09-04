package eu.octanne.mcboyard.entity;

import net.minecraft.server.v1_16_R3.ChatBaseComponent;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.GenericAttributes;

public class ChairEntity extends EntityArmorStand {

	public ChairEntity(World world) {
		super(((CraftWorld)world).getHandle(), 0,0,0);
		
		this.setCustomNameVisible(false);
		this.setCustomName(IChatBaseComponent.ChatSerializer.a("Chair"));
		this.setSmall(true);
		this.setInvulnerable(true);
		this.setBasePlate(false);
		this.setInvisible(true);
		this.setHealth(0.5f);
		this.collides = false;
		this.setNoGravity(true);
		this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(0.5f);
	}
	
	/*public CraftEntity getBukkitEntity() {
		return this.bukkitEntity;
	}*/
	
}
