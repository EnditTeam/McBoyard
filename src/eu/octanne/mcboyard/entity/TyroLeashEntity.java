package eu.octanne.mcboyard.entity;

import java.util.Iterator;
import java.util.List;

import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;

import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityLeash;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumHand;
import net.minecraft.server.v1_12_R1.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_12_R1.World;

public class TyroLeashEntity extends EntityLeash {

	private boolean firstRound = true;

	public TyroLeashEntity(World world) {
		super(world);
		createEntity();
	}

	public TyroLeashEntity(org.bukkit.World world) {
		super(((CraftWorld) world).getHandle());
		createEntity();
	}

	private void createEntity() {
		this.setInvulnerable(true);
		this.setNoGravity(true);
		this.setSilent(true);
	}

	public void Y() {
		if(firstRound == true) {
			
			firstRound = false;
		}
		super.Y();
	}

	public void die() {
		super.die();
	}

	public boolean b(EntityHuman entityhuman, EnumHand enumhand) {
		if (this.world.isClientSide)
			return true; 
		List<EntityInsentient> list = this.world.a(EntityInsentient.class, new AxisAlignedBB(this.locX - 7.0D, this.locY - 7.0D, this.locZ - 7.0D, this.locX + 7.0D, this.locY + 7.0D, this.locZ + 7.0D));
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
}

