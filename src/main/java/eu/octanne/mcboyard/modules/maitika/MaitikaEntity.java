package eu.octanne.mcboyard.modules.maitika;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftSpider;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.DifficultyDamageScaler;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityLlamaSpit;
import net.minecraft.server.v1_16_R3.EntityPose;
import net.minecraft.server.v1_16_R3.EntitySize;
import net.minecraft.server.v1_16_R3.EntitySpider;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.GroupDataEntity;
import net.minecraft.server.v1_16_R3.IRangedEntity;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.MobEffect;
import net.minecraft.server.v1_16_R3.MobEffects;
import net.minecraft.server.v1_16_R3.MovingObjectPositionEntity;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_16_R3.SoundEffects;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldAccess;

public class MaitikaEntity extends EntitySpider implements IRangedEntity {
    public enum MaitikaAttackState {
        VANILLA,
        THROW,
        ON_PLAYER
    }

    private static final int MAX_HEALTH = 100;
    private static final float MOVEMENT_SPEED = 0.5f;
    private static final int POISON_DURATION_TICKS = 30;
    private static final int POISON_LEVEL = 1;
    private static final float GENERIC_DAMAGE = 3.0f;
    private static final float SPIT_DAMAGE = 1.5f;
    private static final float SPIT_SPEED = 0.3f;
    private static final float SPIT_CURVE_COEFF = 0.5F; // Should be inverse proportional to spit speed
    private static final float SPIT_INACCURACY = 0.1f;

    private MaitikaAttackState attackState = MaitikaAttackState.VANILLA;
    private int ticksUntilNewState = 0;
    private int ticksPreviousRangedAttack = 0;
    private Vec3D previousTargetLoc = null;

    public MaitikaEntity(World world, Location loc) {
        super(EntityTypes.CAVE_SPIDER, world);
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());
        this.setYawPitch(loc.getYaw(), loc.getPitch());
        addScoreboardTag("maitika");
        world.addEntity(this);
        setCustomName(new ChatComponentText("Maïtika"));
        setCustomNameVisible(true);
        craftAttributes.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(MAX_HEALTH);
        setHealth(MAX_HEALTH);
        craftAttributes.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        craftAttributes.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(GENERIC_DAMAGE);

        moveAvoidBlocks();
    }

    public MaitikaEntity(Location loc) {
        this(((CraftWorld) loc.getWorld()).getHandle(), loc);
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(1, new PathfinderGoalMaitikaAttack(this, 1.0D, 40, 10.0F));
    }

    static class PathfinderGoalMaitikaAttack extends PathfinderGoalArrowAttack {
        private final MaitikaEntity entity;

        public PathfinderGoalMaitikaAttack(IRangedEntity entity, double var1, int range, float var4) {
            super(entity, var1, range, var4);
            this.entity = (MaitikaEntity) entity;
        }

        /**
         * Can the spider throw a spit at the target ?
         */
        @Override
        public boolean a() {
            if (!super.a())
                return false;
            EntityLiving target = this.entity.getGoalTarget();
            if (target == null)
                return false;
            double distance = target.getPositionVector().distanceSquared(this.entity.getPositionVector());
            if (distance < 4)
                return false; // at least 2 blocks away from the target
            int ticksLived = this.entity.getTicksLived();
            int ticksPreviousRangedAttack = this.entity.getTicksPreviousRangedAttack();
            if (ticksLived - ticksPreviousRangedAttack < 20)
                return false; // wait 1 second between each attack
            return true;
        }
    }

    @Nullable
    @Override
    public GroupDataEntity prepare(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler,
            EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity,
            @Nullable NBTTagCompound nbttagcompound) {
        return groupdataentity;
    }

    @Override
    protected float b(EntityPose entitypose, EntitySize entitysize) {
        return 0.45F;
    }

    @Override
    public boolean isClimbing() {
        // Empêche l'araignée de grimper
        return false;
    }

    public static List<CraftSpider> getMaitikaEntities(org.bukkit.World world) {
        return world.getEntitiesByClasses(Spider.class)
                .stream()
                .filter(entity -> entity.getScoreboardTags().contains("maitika"))
                .map(entity -> (CraftSpider) entity)
                .toList();
    }

    @Override
    public void tick() {
        super.tick();
        EntityLiving target = this.getGoalTarget();
        if (target != null) {
            previousTargetLoc = target.getPositionVector();
        } else {
            previousTargetLoc = null;
        }
    }

    @Override
    public boolean attackEntity(Entity target) {
        onTickDamage();
        switch (attackState) {
            case VANILLA:
                return super.attackEntity(target);
            case THROW:
                // Throw a llama spit
                throwSpit(target);
                return false;
            case ON_PLAYER:
                if (target instanceof EntityLiving) {
                    EntityLiving entityLiving = (EntityLiving) target;
                    if (!this.isPassenger()) {
                        this.startRiding(entityLiving);
                    }
                    entityLiving.addEffect(new MobEffect(MobEffects.POISON, POISON_DURATION_TICKS, POISON_LEVEL));
                    super.attackEntity(target);
                    return true;
                }
                return false;
        }
        return false;
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        onTickDamage();
        return super.damageEntity(damagesource, f);
    }

    /**
     * When the spider attacks or is attacked, change its attack state randomly
     */
    private void onTickDamage() {
        ticksUntilNewState--;
        if (ticksUntilNewState <= 0) {
            setAttackState(MaitikaAttackState.values()[(int) (Math.random() * MaitikaAttackState.values().length)]);
            ticksUntilNewState = (int) (Math.random() * 9) + 1; // Entre 1 et 10 coups
            if (attackState == MaitikaAttackState.ON_PLAYER) {
                // Increase the duration of the attack
                ticksUntilNewState *= 1.5;
            }
        }
    }

    public void setAttackState(MaitikaAttackState attackState) {
        if (this.attackState == MaitikaAttackState.ON_PLAYER) {
            this.stopRiding();
            if (movingTarget != null) {
                setPosition(movingTarget.getX(), movingTarget.getY(), movingTarget.getZ());
            }
            moveAvoidBlocks();
        }

        this.attackState = attackState;

        if (this.attackState == MaitikaAttackState.ON_PLAYER) {
            Entity target = this.getGoalTarget();
            if (target == null)
                return;
            this.startRiding(target);
        }
    }

    public static Vec3D guessNextLocation(Vec3D previousLoc, Vec3D currentLoc, int ticks) {
        // Guess the next target location in X ticks
        // => currentLoc - motion * ticks
        return new Vec3D(
                currentLoc.getX() + (currentLoc.getX() - previousLoc.getX()) * ticks,
                currentLoc.getY() + (currentLoc.getY() - previousLoc.getY()) * ticks,
                currentLoc.getZ() + (currentLoc.getZ() - previousLoc.getZ()) * ticks);
    }

    /**
     * Throw a spit at the target (by guessing where the target will be in 5 ticks)
     */
    public void throwSpit(@NotNull Entity target) {
        MaitikaEntity maitika = this;
        EntityLlamaSpit spit = new EntityLlamaSpit(EntityTypes.LLAMA_SPIT, getWorld()) {
            @Override
            protected void a(MovingObjectPositionEntity target) {
                target.getEntity().damageEntity(DamageSource.a(this, maitika).c(), SPIT_DAMAGE);
            }
        };
        spit.setPosition(locX(), locY() + 0.7, locZ()); // Lower = the spit hit the carpet
        spit.setShooter(this);

        Vec3D targetLoc = target.getPositionVector();

        double dX = targetLoc.getX() - spit.locX();
        double dY = targetLoc.getY() - spit.locY() + target.getHeight() * 0.5; // middle of the target
        double dZ = targetLoc.getZ() - spit.locZ();
        float distance = MathHelper.sqrt(dX * dX + dZ * dZ);
        // Increase the speed and decrease the curve coeff if the target is far
        float speed = Math.max(1f, SPIT_SPEED * (float) Math.pow(distance, 0.8));
        float curveCoeff = SPIT_CURVE_COEFF / Math.max(1f, (float) Math.pow(distance, 0.6));
        float curvePower = distance * curveCoeff;

        if (previousTargetLoc != null) {
            // Guess the next target location in 5 ticks
            Vec3D nextTargetLoc = guessNextLocation(previousTargetLoc, targetLoc, 5);
            double yTarget = (previousTargetLoc.getY() + targetLoc.getY()) / 2;

            dX = nextTargetLoc.getX() - spit.locX();
            dY = yTarget - spit.locY() + target.getHeight() * 0.5;
            dZ = nextTargetLoc.getZ() - spit.locZ();
            distance = MathHelper.sqrt(dX * dX + dZ * dZ);
            curvePower = distance * curveCoeff;
        }

        spit.shoot(dX, dY + curvePower, dZ, speed, SPIT_INACCURACY);
        float r1 = random.nextFloat();
        float r2 = random.nextFloat();
        this.world.playSound(null, locX(), locY(), locZ(),
                SoundEffects.ENTITY_LLAMA_SPIT, this.getSoundCategory(),
                0.5F, 1.8F + (r1 - r2) * 0.2F);
        this.world.addEntity(spit);
    }

    /**
     * Avoid glitching into blocks
     */
    public void moveAvoidBlocks() {
        AxisAlignedBB idbox = getBoundingBox();
        Block[] blocks = new Block[4];
        double x, xMin, xMax, z, zMin, zMax;
        x = xMin = xMax = (idbox.minX + idbox.maxX) / 2; // mid
        z = zMin = zMax = (idbox.minZ + idbox.maxZ) / 2; // mid
        double sizeX2 = (idbox.maxX - idbox.minX) / 2; // half size
        double sizeZ2 = (idbox.maxZ - idbox.minZ) / 2; // half size
        org.bukkit.World w = getWorld().getWorld();
        blocks[0] = new Location(w, x, idbox.minY, idbox.minZ).getBlock(); // north
        blocks[1] = new Location(w, x, idbox.minY, idbox.maxZ).getBlock(); // south
        blocks[2] = new Location(w, idbox.maxX, idbox.minY, z).getBlock(); // east
        blocks[3] = new Location(w, idbox.minX, idbox.minY, z).getBlock(); // west

        if (blocks[0].getBoundingBox().contains(x, idbox.minY, idbox.minZ)) {
            BoundingBox box = blocks[0].getBoundingBox();
            zMin = Math.max(z, box.getMaxZ() + sizeZ2);
        }
        if (blocks[1].getBoundingBox().contains(x, idbox.minY, idbox.maxZ)) {
            BoundingBox box = blocks[1].getBoundingBox();
            zMax = Math.min(z, box.getMinZ() - sizeZ2);
        }
        if (blocks[2].getBoundingBox().contains(idbox.maxX, idbox.minY, z)) {
            BoundingBox box = blocks[2].getBoundingBox();
            xMax = Math.min(x, box.getMinX() - sizeX2);
        }
        if (blocks[3].getBoundingBox().contains(idbox.minX, idbox.minY, z)) {
            BoundingBox box = blocks[3].getBoundingBox();
            xMin = Math.max(x, box.getMaxX() + sizeX2);
        }

        if (xMax < x && x < xMin) {
            x = (xMin + xMax) / 2;
        } else if (xMax < x) {
            x = xMax;
        } else if (x < xMin) {
            x = xMin;
        }

        if (zMax < z && z < zMin) {
            z = (zMin + zMax) / 2;
        } else if (zMax < z) {
            z = zMax;
        } else if (z < zMin) {
            z = zMin;
        }

        setPosition(x, locY(), z);
    }

    /**
     * IRangedEntity.a(target, f) : throw a projectile at the target
     */
    @Override
    public void a(EntityLiving entityliving, float f) {
        for (Player player : Bukkit.getWorlds().get(0).getPlayers()) {
            player.sendMessage("throwSpit as IRangedEntity !");
        }
        throwSpit(entityliving);
        ticksPreviousRangedAttack = ticksLived;
    }

    public int getTicksLived() {
        return ticksLived;
    }

    public int getTicksPreviousRangedAttack() {
        return ticksPreviousRangedAttack;
    }
}
