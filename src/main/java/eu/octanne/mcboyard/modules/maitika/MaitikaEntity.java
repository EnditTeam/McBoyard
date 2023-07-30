package eu.octanne.mcboyard.modules.maitika;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftSpider;
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
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.MobEffect;
import net.minecraft.server.v1_16_R3.MobEffects;
import net.minecraft.server.v1_16_R3.MovingObjectPositionEntity;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.SoundEffects;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldAccess;

public class MaitikaEntity extends EntitySpider {
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
    private static final float SPIT_SPEED = 0.7f;
    private static final float SPIT_CURVE_COEFF = 0.3F; // Should be inverse proportional to spit speed
    private static final float SPIT_INACCURACY = 0.1f;

    private MaitikaAttackState attackState = MaitikaAttackState.VANILLA;
    private int ticksUntilNewState = 0;

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

        double dX = target.locX() - spit.locX();
        double dY = target.e(0.5) - spit.locY(); // middle of the target
        double dZ = target.locZ() - spit.locZ();
        float f = MathHelper.sqrt(dX * dX + dZ * dZ) * SPIT_CURVE_COEFF;
        spit.shoot(dX, dY + f, dZ, SPIT_SPEED, SPIT_INACCURACY);
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
        double xMin = idbox.minX;
        double xMax = idbox.maxX;
        double zMin = idbox.minZ;
        double zMax = idbox.maxZ;
        blocks[0] = new Location(getWorld().getWorld(), idbox.minX, idbox.minY, idbox.minZ).getBlock();
        blocks[1] = blocks[0].getRelative(0, 0, 1);
        blocks[2] = blocks[0].getRelative(1, 0, 0);
        blocks[3] = blocks[0].getRelative(1, 0, 1);

        if (blocks[0].getBoundingBox().contains(idbox.minX, idbox.minY, idbox.minZ)) {
            BoundingBox box = blocks[0].getBoundingBox();
            xMin = Math.max(xMin, box.getMaxX());
            zMin = Math.max(zMin, box.getMaxZ());
        }
        if (blocks[1].getBoundingBox().contains(idbox.minX, idbox.minY, idbox.maxZ)) {
            BoundingBox box = blocks[1].getBoundingBox();
            xMin = Math.max(xMin, box.getMaxX());
            zMax = Math.min(zMax, box.getMinZ());
        }
        if (blocks[2].getBoundingBox().contains(idbox.maxX, idbox.minY, idbox.minZ)) {
            BoundingBox box = blocks[2].getBoundingBox();
            xMax = Math.min(xMax, box.getMinX());
            zMin = Math.max(zMin, box.getMaxZ());
        }
        if (blocks[3].getBoundingBox().contains(idbox.maxX, idbox.minY, idbox.maxZ)) {
            BoundingBox box = blocks[3].getBoundingBox();
            xMax = Math.min(xMax, box.getMinX());
            zMax = Math.min(zMax, box.getMinZ());
        }

        double xMid = (xMin + xMax) / 2;
        double zMid = (zMin + zMax) / 2;
        setPosition(xMid, locY(), zMid);
    }
}
