package eu.octanne.mcboyard.entity;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import net.minecraft.server.v1_12_R1.EntityZombie;

public class TyroEntity extends EntityZombie {

	public TyroEntity(World world) {
		super(((CraftWorld)world).getHandle());
		
		this.setCustomNameVisible(false);
		this.setCustomName("Tyro Poles");
		this.setInvulnerable(true);
		this.setInvisible(false); // TODO
		this.collides = false;
		this.setBaby(true);
		this.setNoGravity(true);
		this.canPickUpLoot = false;
		this.setSilent(true);
		this.setNoAI(true);

	}
	
	/*public CraftEntity getBukkitEntity() {
		return this.bukkitEntity;
	}*/
	
}
