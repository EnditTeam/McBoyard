package eu.octanne.mcboyard.entity;

import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_12_R1.EntitySlime;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;
import net.minecraft.server.v1_12_R1.ScoreboardTeamBase.EnumTeamPush;
import net.minecraft.server.v1_12_R1.World;

public class TyroEntity extends EntitySlime {
	
	private ScoreboardTeam tm = null;

	private boolean firstRound = true;
	
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
		
		this.setInvisible(false);
		
		this.setInvulnerable(true);
		this.collides = false;
		this.setNoGravity(true);
		this.setNoAI(true);
		this.setSilent(true);
		this.setSize(1, true);
		this.setCustomName("Tyro");
		this.setCustomNameVisible(false);
		
		if(tm == null) initTeam();
	}
	
	public void Y() {
		if(firstRound == true) {
			((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1, false, false));
			world.scoreboard.addPlayerToTeam(this.bn(),"Tyro");
			firstRound = false;
		}
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

