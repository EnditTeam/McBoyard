package eu.octanne.mcboyard.entity;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.EntitySlime;
import net.minecraft.server.v1_12_R1.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;
import net.minecraft.server.v1_12_R1.ScoreboardTeamBase.EnumTeamPush;
import net.minecraft.server.v1_12_R1.World;

public class TyroEntity extends EntitySlime {

	static private ArrayList<TyroEntity> instances = new ArrayList<>(); 

	private ScoreboardTeam tm = null;

	public boolean needToDie = false;

	private boolean firstRound = true;
	private int tickLeashUpdate = 0;

	private void initTeam() {
		if(world.scoreboard.getTeam("Tyro") == null) {
			tm = world.scoreboard.createTeam("Tyro");
			tm.setCollisionRule(EnumTeamPush.NEVER);
		}
	}

	public TyroEntity(World world) {
		super(world);
		createEntity();
	}

	public TyroEntity(org.bukkit.World world) {
		super(((CraftWorld) world).getHandle());
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

	public void Y() {
		tickLeashUpdate++;
		if(tickLeashUpdate >= 5) {
			for(Player p : world.getWorld().getPlayers()) {
				if(p.getLocation().distance(bukkitEntity.getLocation()) <= 48) {
					// SEND PACKET TO ATTACH
					PacketPlayOutAttachEntity packetS = new PacketPlayOutAttachEntity(this, getLeashHolder());
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetS);
				}
			}
			tickLeashUpdate = 0;
		}
		if(firstRound == true) {
			world.scoreboard.addPlayerToTeam(this.bn(),"Tyro");
			((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10000000, 1, false, false));
			firstRound = false;
		}
		super.Y();
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

	public void killEntity() {
		if(needToDie)
			super.killEntity();
	}

	public void leashedTo(net.minecraft.server.v1_12_R1.Entity en) {
		((LivingEntity)this.bukkitEntity).setLeashHolder(en.getBukkitEntity());
	}

	static public TyroEntity getTyroEntity(UUID id) {
		for(TyroEntity it : instances) {
			if(it.uniqueID.equals(id)) return it;
		}
		return null;
	}

}

