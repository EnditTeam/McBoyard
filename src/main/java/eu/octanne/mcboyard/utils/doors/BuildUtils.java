package eu.octanne.mcboyard.utils.doors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
                    Location block = pos.clone().add(x, y, z);
                    block.getBlock().setType(type);
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
}
