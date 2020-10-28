package eu.octanne.mcboyard.entity;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_12_R1.EntitySlime;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.MobEffect;
import net.minecraft.server.v1_12_R1.MobEffects;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityEffect;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;
import net.minecraft.server.v1_12_R1.ScoreboardTeamBase.EnumTeamPush;
import net.minecraft.server.v1_12_R1.World;

public class TyroEntity extends EntitySlime {
	
	private ScoreboardTeam tm = null;

	private boolean firstRound = true;
	
	/*private ArrayList<Player> playerNear = new ArrayList<>();
	private ArrayList<Player> playerAlready = new ArrayList<>();*/
	
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
		this.setSilent(false); // 
		this.setSize(1, true);
		this.setCustomName("Tyro");
		this.setCustomNameVisible(false);
		
		if(tm == null) initTeam();
	}
	
	public void Y() {
		if(firstRound == true) {
			world.scoreboard.addPlayerToTeam(this.bn(),"Tyro");
			//((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1, false, false));
			firstRound = false;
		}
		/*playerNear.clear();
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getLocation().distance(bukkitEntity.getLocation()) <= 32) {
				playerNear.add(p);
			}
		}
		for(Player p : playerNear) {
			if(!playerAlready.contains(p)) {
				Bukkit.broadcastMessage("SN");
				MobEffect effect = new MobEffect(MobEffects.INVISIBILITY,254,254,false,true);
				PacketPlayOutEntityEffect pckt = new PacketPlayOutEntityEffect(this.getBukkitEntity().getEntityId(), effect);
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(pckt);
				playerAlready.add(p);
			}
		}*/
	}

	public Item getLoot() {
		return null;
	}
	
	public void leashedTo(Entity en) {
		((LivingEntity)this.bukkitEntity).setLeashHolder(en);
	}
	
	/*public CraftEntity getBukkitEntity() {
		return this.bukkitEntity;
	}*/
}

