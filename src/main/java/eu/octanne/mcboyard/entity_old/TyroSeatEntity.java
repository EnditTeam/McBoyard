package eu.octanne.mcboyard.entity_old;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.PacketPlayOutMount;

public class TyroSeatEntity extends EntityArmorStand {
	
	public boolean needToDie = false;
	
	public static ArrayList<TyroSeatEntity> instances = new ArrayList<>();
	
	public TyroSeatEntity(World world) {
		super(((CraftWorld)world).getHandle(), 0,0,0);
		
		this.noclip = true;
		this.setCustomNameVisible(false);
		this.setSmall(true);
		this.setInvulnerable(true);
		this.setBasePlate(false);
		this.setInvisible(true);
		this.setHealth(0.5f);
		this.collides = false;
		this.setNoGravity(true);
		this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(0.5f);
		instances.add(this);
	}
	
	public void putOnSeat(Player p) {
		this.getBukkitEntity().addPassenger(p);
		PacketPlayOutMount npc = new PacketPlayOutMount(((CraftPlayer) p).getHandle());
		//the a field used to be public, we'll need to use reflection to access:
		try {
		    Field field = npc.getClass().getDeclaredField("a");
		    field.setAccessible(true);// allows us to access the field
		 
		    field.setInt(npc, this.getBukkitEntity().getEntityId());// sets the field to an integer
		    
		    Field field2 = npc.getClass().getDeclaredField("b");
		    field2.setAccessible(true);// allows us to access the field
			 
		    int[] passengerList = {p.getEntityId()};
		    field2.set(npc, passengerList);// sets the field to an integer
		} catch(Exception x) {
		    x.printStackTrace();
		}
		for(Player pS : Bukkit.getOnlinePlayers()) {
			//now comes the sending
			CraftPlayer pC = (CraftPlayer) pS;
			pC.getHandle().playerConnection.sendPacket(npc);
		}
	}
	
	public void die() {
		if(needToDie) {
			super.die();
			instances.remove(this);
		}
	}
	
	public static void killAll() {
		try {
			for(TyroSeatEntity en: instances) {
				en.needToDie = true;
				en.die();
			}
		}catch(ConcurrentModificationException e) {
			return;
		}
	}
	
	public void die(DamageSource source) {
		if(needToDie) {
			super.die(source);
			instances.remove(this);
		}
			
	}
}
