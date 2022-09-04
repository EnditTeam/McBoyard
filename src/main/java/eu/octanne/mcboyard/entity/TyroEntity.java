package eu.octanne.mcboyard.entity;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_16_R3.ScoreboardTeamBase.EnumTeamPush;

public class TyroEntity extends EntitySlime {

	static private ArrayList<TyroEntity> instances = new ArrayList<>(); 

	private ScoreboardTeam tm = null;

	public boolean needToDie = false;

	private boolean firstRound = true;
	private int tickLeashUpdate = 0;

	private void initTeam() {
		if(world.getScoreboard().getTeam("Tyro") == null) {
			tm = world.getScoreboard().createTeam("Tyro");
			tm.setCollisionRule(EnumTeamPush.NEVER);
		}
	}

	public TyroEntity(World world) {
		super(EntityTypes.SLIME, world);
		createEntity();
	}

	public TyroEntity(org.bukkit.World world) {
		super(EntityTypes.SLIME,((CraftWorld) world).getHandle());
		createEntity();
	}

	private void createEntity() {
		this.setInvulnerable(true);
		this.setNoGravity(true);
		this.setNoAI(true);
		this.setSilent(true);
		this.setSize(1, false);

		if(tm == null) initTeam();

		instances.add(this);
	}

	public void Y() { // TODO A revoir
		tickLeashUpdate++;
		if(tickLeashUpdate >= 5) {
			for(Player p : world.getWorld().getPlayers()) {
				if(p.getLocation().distance(getBukkitEntity().getLocation()) <= 48) {
					// SEND PACKET TO ATTACH
					PacketPlayOutAttachEntity packetS = new PacketPlayOutAttachEntity(this, getLeashHolder());
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetS);
				}
			}
			tickLeashUpdate = 0;
		}
		if(firstRound == true) {
			//world.getScoreboard().addPlayerToTeam(this.bn(),"Tyro");
			((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10000000, 1, false, false));
			firstRound = false;
		}
		//super.Y();
	}

	public void die() {
		if(needToDie) {
			instances.remove(this);
			super.die();
		}	
	}

	public void die(DamageSource source) {
		if(needToDie) {
			instances.remove(this);
			super.die(source);
		}
	}

	public boolean damageEntity(DamageSource damagesource, float f) {
		if(needToDie) return super.damageEntity(damagesource, f);
		else return false;
	}
	
	public void killEntity() {
		if(needToDie)
			super.killEntity();
	}

	public void leashedTo(net.minecraft.server.v1_16_R3.Entity en) {
		((LivingEntity)this.getBukkitEntity()).setLeashHolder(en.getBukkitEntity());
	}

	static public TyroEntity getTyroEntity(UUID id) {
		for(TyroEntity it : instances) {
			if(it.uniqueID.equals(id)) return it;
		}
		return null;
	}

}

