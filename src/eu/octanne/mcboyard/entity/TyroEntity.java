package eu.octanne.mcboyard.entity;

import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import net.minecraft.server.v1_12_R1.EntitySlime;
import net.minecraft.server.v1_12_R1.World;

public class TyroEntity extends EntitySlime {

	public TyroEntity(org.bukkit.World world) {
		super(((CraftWorld) world).getHandle());
		createEntity();
	}
	
	public TyroEntity(World world) {
		super(world);
		createEntity();
	}
	
	private void createEntity() {
		
		//this.setInvisible(true);
		
		this.setSize(1, false);
		
		this.setInvulnerable(true);
		this.collides = false;
		this.setNoGravity(true);
		this.setSilent(true);
		this.setNoAI(true);
	}
	
	public void leashedTo(Entity en) {
		this.setLeashHolder(((CraftEntity) en).getHandle(), true);
	}
	
	/*public CraftEntity getBukkitEntity() {
		return this.bukkitEntity;
	}*/
	
}

