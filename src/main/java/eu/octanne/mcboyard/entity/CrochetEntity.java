package eu.octanne.mcboyard.entity;

import net.minecraft.server.v1_16_R3.EntitySlime;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;

public class CrochetEntity extends EntitySlime {

    public CrochetEntity(World world, Location loc) {
        super(EntityTypes.SLIME, world);
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());

        world.addEntity(this);
    }

    @Override
    public void tick() {
        super.tick();
    }

}
