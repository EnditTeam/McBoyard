package eu.octanne.mcboyard.utils.doors;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import eu.octanne.mcboyard.McBoyard;

public class GhostStructure {
    private final List<@NotNull GhostBlock> blocks;
    private @NotNull Location location;

    public GhostStructure(@NotNull Location location) {
        this.blocks = new ArrayList<>();
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void spawn(List<GhostBlockData> ghostBlocks) {
        if (!blocks.isEmpty())
            despawn();
        boolean isChunkLoaded = location.getChunk().isLoaded();
        if (!isChunkLoaded) {
            McBoyard.instance.getLogger().warning("GhostStructure won't spawn : Chunk not loaded at " + location);
        }
        boolean someGhostBlockNotSpawned = false;
        for (GhostBlockData ghostData : ghostBlocks) {
            GhostBlock ghostBlock = GhostBlock.spawn(location, ghostData);
            if (ghostBlock != null)
                blocks.add(ghostBlock);
            else
                someGhostBlockNotSpawned = true;
        }
        if (someGhostBlockNotSpawned) {
            McBoyard.instance.getLogger().warning("GhostStructure : Some GhostBlock didn't spawned at " + location);
        }
    }

    public void despawn() {
        for (GhostBlock ghostBlock : blocks) {
            ghostBlock.despawn();
        }
        blocks.clear();
    }

    public void teleport(@NotNull Location location) {
        this.location = location;
        for (GhostBlock ghostBlock : blocks) {
            ghostBlock.teleport(location);
        }
    }

    public void respawn(@NotNull Location location, List<GhostBlockData> ghostBlocks) {
        despawn();
        this.location = location;
        spawn(ghostBlocks);
    }

    public boolean isSpawned() {
        return !blocks.isEmpty();
    }
}
