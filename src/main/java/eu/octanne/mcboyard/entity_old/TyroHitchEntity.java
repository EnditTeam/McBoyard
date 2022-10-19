package eu.octanne.mcboyard.entity_old;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;

public class TyroHitchEntity extends EntityLeash {

	private boolean firstRound = true;

	public boolean needToDie = false;
	
	public TyroHitchEntity(World world) {
		super(EntityTypes.LEASH_KNOT, world);
		createEntity();
	}

	public TyroHitchEntity(org.bukkit.World world) {
		super(EntityTypes.LEASH_KNOT, ((CraftWorld) world).getHandle());
		createEntity();
	}

	private void createEntity() {
		this.setInvulnerable(true);
		this.setNoGravity(true);
		this.setSilent(true);
	}

	public void Y() { // TODO Retrouver le nom de la fonction
		if(firstRound == true) {
			firstRound = false;
		}
		//super.Y();
	}

	public void die() {
		if(needToDie)
			super.die();
	}

	public void B_() {
		this.lastX = this.locX();
		this.lastY = this.locY();
		this.lastZ = this.locZ();
	}

	public boolean survives() {
		return true;
	}

	public boolean b(EntityHuman entityhuman, EnumHand enumhand) { // TODO Retrouver le nom de la fonction
		if (this.world.isClientSide)
			return true; 
		List<EntityInsentient> list = this.world.a(EntityInsentient.class, new AxisAlignedBB(this.locX() - 7.0D, this.locY() - 7.0D, this.locZ() - 7.0D, this.locX() + 7.0D, this.locY() + 7.0D, this.locZ() + 7.0D));
		Iterator<EntityInsentient> iterator = list.iterator();
		while (iterator.hasNext()) {
			EntityInsentient entityinsentient = iterator.next();
			if (entityinsentient.isLeashed() && entityinsentient.getLeashHolder() == entityhuman) {
				if (CraftEventFactory.callPlayerLeashEntityEvent(entityinsentient, this, entityhuman).isCancelled()) {
					((EntityPlayer)entityhuman).playerConnection.sendPacket(new PacketPlayOutAttachEntity(entityinsentient, entityinsentient.getLeashHolder()));
					continue;
				}
				entityinsentient.setLeashHolder(this, false);
			}
		}
		return true;
	}

	public Location getLoc() {
		return super.getBukkitEntity().getLocation();
	}
	
	/*
	public final String getSaveID() {
		MinecraftKey minecraftkey = EntityTypes.a(new EntityLeash(world));
		return (minecraftkey == null) ? null : minecraftkey.toString();
	}
	*/
}

