package eu.octanne.mcboyard.entity;

import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

public class MiddleEntity extends EntityArmorStand {

    public MiddleEntity(World world, Location loc) {
        super(EntityTypes.ARMOR_STAND, world);
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());

        world.addEntity(this);
    }

    @Override
    public void tick() {
        super.tick();
    }

}
