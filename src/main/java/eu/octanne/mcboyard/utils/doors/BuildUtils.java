package eu.octanne.mcboyard.utils.doors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Fence;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

public class BuildUtils {
    private BuildUtils() {
    }

    public static void deconstruct(Location pos, Vector size) {
        // Fill with air
        fill(pos, size, Material.AIR);
    }

    public static void deconstruct(Location pos, Vector size, Material filter) {
        // Replace with air
        replace(pos, size, filter, Material.AIR);
    }

    public static void fill(Location pos, Vector size, Material type) {
        for (int x = 0; x < size.getBlockX(); x++) {
            for (int y = 0; y < size.getBlockY(); y++) {
                for (int z = 0; z < size.getBlockZ(); z++) {
                    Block block = pos.clone().add(x, y, z).getBlock();
                    block.setType(type);
                }
            }
        }
    }

    public static void replace(Location pos, Vector size, Material from, Material to) {
        for (int x = 0; x < size.getBlockX(); x++) {
            for (int y = 0; y < size.getBlockY(); y++) {
                for (int z = 0; z < size.getBlockZ(); z++) {
                    Block block = pos.clone().add(x, y, z).getBlock();
                    if (block.getType() == from)
                        block.setType(to);
                }
            }
        }
    }

    public static void replace(Location pos, Vector size, Material from, Material to, Consumer<Block> blockConsumer) {
        for (int x = 0; x < size.getBlockX(); x++) {
            for (int y = 0; y < size.getBlockY(); y++) {
                for (int z = 0; z < size.getBlockZ(); z++) {
                    Block block = pos.clone().add(x, y, z).getBlock();
                    if (block.getType() == from) {
                        block.setType(to);
                        blockConsumer.accept(block);
                    }
                }
            }
        }
    }

    public static void walls(Location minPos, Location maxPos, Material from, Material to,
            Consumer<Block> blockConsumer) {
        Vector size = maxPos.toVector().subtract(minPos.toVector()).add(new Vector(1, 1, 1));
        replace(minPos, size.clone().setX(1), from, to, blockConsumer);
        replace(minPos, size.clone().setZ(1), from, to, blockConsumer);
        replace(minPos.clone().add(size.getX() - 1, 0, 0), size.clone().setX(1), from, to, blockConsumer);
        replace(minPos.clone().add(0, 0, size.getZ() - 1), size.clone().setZ(1), from, to, blockConsumer);
    }

    public static void connectFenceWithSameType(Block block) {
        BlockState state = block.getState();
        BlockData data = state.getBlockData();
        if (!(data instanceof Fence))
            return;

        MultipleFacing fence = (MultipleFacing) data;
        // Connect to neighbors
        for (BlockFace face : fence.getAllowedFaces()) {
            if (block.getRelative(face).getType() == fence.getMaterial())
                fence.setFace(face, true);
        }
        state.setBlockData(fence);
        state.update(true);
    }
}
