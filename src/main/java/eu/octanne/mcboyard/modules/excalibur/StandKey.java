package eu.octanne.mcboyard.modules.excalibur;

import eu.octanne.mcboyard.entity.CrochetEntity;
import eu.octanne.mcboyard.entity.MiddleEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class StandKey {

    private final int[] locStand;
    private final String locWorld;

    private CrochetEntity[] crochetEntities;

    private MiddleEntity middleEntity;

    public StandKey(String locWorld, int locX, int locY, int locZ) {
        this.locStand = new int[]{locX, locY, locZ};
        this.locWorld = locWorld;

        constructCrochetEntities();
        setBlock();
    }

    public Location getBukkitLocation() {
        return new Location(Bukkit.getWorld(locWorld), locStand[0], locStand[1], locStand[2]);
    }

    private void constructCrochetEntities() {
        crochetEntities = new CrochetEntity[4];
        // TODO construct crochet entities
    }

    public void despawn() {
        for (CrochetEntity crochetEntity : crochetEntities) {
            crochetEntity.getBukkitEntity().remove();
        }
        middleEntity.getBukkitEntity().remove();
    }

    public void respawn() {
        for (CrochetEntity crochetEntity : crochetEntities) {
            crochetEntity.getBukkitEntity().remove();
        }
        middleEntity.getBukkitEntity().remove();
        constructCrochetEntities();
        setBlock();
    }

    public void delete() {
        // TODO
    }

    private void setBlock() {
        // TODO setblock of crochet & middle stand
    }

    public boolean entityIsStandKey(Entity entity) {
        if (((CraftEntity)entity).getHandle() instanceof MiddleEntity ||
                ((CraftEntity)entity).getHandle() instanceof CrochetEntity) {
            net.minecraft.server.v1_16_R3.Entity nmsEntity = ((CraftEntity)entity).getHandle();
            if (middleEntity.getBukkitEntity().equals(entity) ||
                    crochetEntities[0].getBukkitEntity().equals(entity) ||
                    crochetEntities[1].getBukkitEntity().equals(entity) ||
                    crochetEntities[2].getBukkitEntity().equals(entity) ||
                    crochetEntities[3].getBukkitEntity().equals(entity)) {
                return true;
            } else return false;
        } else return false;
    }

}
