package eu.octanne.mcboyard.entity;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import net.minecraft.server.v1_12_R1.EntityBat;

public class TyroEntity extends EntityBat {

	public TyroEntity(World world) {
		super(((CraftWorld) world).getHandle());
		createEntity();
	}
	
	public TyroEntity(net.minecraft.server.v1_12_R1.World world) {
		super(world);
		createEntity();
	}
	
	private void createEntity() {
		this.setInvulnerable(true);
		this.collides = false;
		this.setNoGravity(true);
		this.setSilent(true);
		this.setNoAI(true);
	}
	
	/*public CraftEntity getBukkitEntity() {
		return this.bukkitEntity;
	}*/
	
}

