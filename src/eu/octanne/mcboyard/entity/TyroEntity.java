package eu.octanne.mcboyard.entity;

import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EntitySlime;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;
import net.minecraft.server.v1_12_R1.ScoreboardTeamBase.EnumTeamPush;
import net.minecraft.server.v1_12_R1.World;

public class TyroEntity extends EntityArmorStand {

	private EntitySlime headTyro;
	
	private ScoreboardTeam tm;
	
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
		this.setSilent(true);
		this.setSmall(true);
		this.setCustomName("Tyro");
		this.setCustomNameVisible(false);
		
		if(tm == null) initTeam();
		
		// Slime to Leash
		headTyro = new EntitySlime(world);
		headTyro.setInvulnerable(true);
		headTyro.setNoAI(true);
		headTyro.setNoGravity(true);
		headTyro.setCustomName("Tyro");
		headTyro.setCustomNameVisible(false);
	    //world.scoreboard.addPlayerToTeam(tm.getName(), headTyro.bn());
	}
	
	public void Y() {
		if(justCreated == true) {
			((LivingEntity) headTyro.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1, false, false));
			headTyro.setPosition(this.bukkitEntity.getLocation().getX(), this.bukkitEntity.getLocation().getY(), 
					this.bukkitEntity.getLocation().getZ());
			this.world.addEntity(headTyro);
			
			this.getBukkitEntity().addPassenger(headTyro.getBukkitEntity());
		}
	}
	
	public void leashedTo(Entity en) {
		headTyro.setLeashHolder(((CraftEntity)en).getHandle(), false);
		//this.setLeashHolder(((CraftEntity) en).getHandle(), true);
	}
	
	/*public CraftEntity getBukkitEntity() {
		return this.bukkitEntity;
	}*/
}

