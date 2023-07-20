package eu.octanne.mcboyard.utils.doors;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Fence;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import eu.octanne.mcboyard.McBoyard;

public class GridAnimation {
    private final Location posClosed;
    private final Vector size;
    private final int openOffset;
    private float openSpeed;
    private float closeSpeed;
    private final GhostStructure ghost;
    private BukkitTask task;

    public GridAnimation(Location posClosed, Vector size, int openOffset, float openSpeed, float closeSpeed) {
        this.posClosed = posClosed;
        this.size = size;
        this.openOffset = openOffset;
        this.openSpeed = openSpeed;
        this.closeSpeed = closeSpeed;
        this.ghost = new GhostStructure(posClosed.clone());
    }

    public Location getOpenLocation() {
        return posClosed.clone().add(0, openOffset, 0);
    }

    public Location getClosedLocation() {
        return posClosed.clone();
    }

    private void place(Location location) {
        boolean connectX = size.getBlockX() > 1;
        boolean connectZ = size.getBlockZ() > 1;
        BuildUtils.replace(location, size, Material.AIR, Material.IRON_BARS, block -> {
            BlockState state = block.getState();
            BlockData data = state.getBlockData();
            if (!(data instanceof Fence))
                return;

            Fence fence = (Fence) data;
            // Connect to neighbors
            if (connectX) {
                fence.setFace(BlockFace.EAST, true);
                fence.setFace(BlockFace.WEST, true);
            }
            if (connectZ) {
                fence.setFace(BlockFace.NORTH, true);
                fence.setFace(BlockFace.SOUTH, true);
            }
            state.setBlockData(fence);
            state.update(true);
        });
    }

    private void deconstruct(Location location) {
        BuildUtils.deconstruct(location, size, Material.IRON_BARS);
    }

    private void deconstruct() {
        deconstruct(getClosedLocation());
        deconstruct(getOpenLocation());
    }

    private void spawn(Location location) {
        List<GhostBlockData> ghostsDatas = new ArrayList<>();
        for (int x = 0; x < size.getBlockX(); x++) {
            for (int y = 0; y < size.getBlockY(); y += 2) { // un bloc sur deux
                for (int z = 0; z < size.getBlockZ(); z++) {
                    ghostsDatas.add(new GhostBlockData(new Vector(x, y, z), Material.GHAST_TEAR, 11102, 90));
                }
            }
        }
        ghost.respawn(location, ghostsDatas);
    }

    public void placeClosed() {
        if(task != null)
            task.cancel();
        deconstruct(getOpenLocation());
        place(getClosedLocation());
        ghost.despawn();
    }

    public void placeOpen() {
        if(task != null)
            task.cancel();
        deconstruct(getClosedLocation());
        place(getOpenLocation());
        ghost.despawn();
    }

    public void openAnimation() {
        deconstruct();
        spawn(getClosedLocation());
        startAnimation(true);
    }

    public void closeAnimation() {
        deconstruct();
        spawn(getOpenLocation());
        startAnimation(false);
    }

    private void startAnimation(boolean open) {
        if (task != null)
            task.cancel();

        Location target = open ? getOpenLocation() : getClosedLocation();
        float speed = open ? openSpeed : -closeSpeed;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                Location location = ghost.getLocation().clone();

                double distanceBefore = Math.abs(target.getY() - location.getY());
                location.add(0, speed, 0);
                double distanceAfter = Math.abs(target.getY() - location.getY());

                if (distanceAfter > distanceBefore) {
                    // Stop here
                    task.cancel();
                    place(open ? getOpenLocation() : getClosedLocation());
                    ghost.despawn();
                    return;
                }

                ghost.teleport(location);
            }
        }.runTaskTimer(McBoyard.instance, 1, 1);
    }

    public void stopAnimation() {
        if (task != null)
            task.cancel();
        ghost.despawn();
        place(getClosedLocation());
    }
}
