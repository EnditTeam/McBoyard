package eu.octanne.mcboyard.modules.coffrefort;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.utils.doors.GridAnimation;

public class CoffreFortAnimation {
    private static final Random random = new Random();
    private final GridAnimation[] grids;
    private BukkitTask task;
    private int ticksUntilDrop = 0;
    private int ticksSalle = 0;
    private final Block dropper1;
    private final Block dropper2;

    public CoffreFortAnimation() {
        World w = McBoyard.getWorld();
        grids = new GridAnimation[4];
        dropper1 = w.getBlockAt(32, 78, 25);
        dropper2 = w.getBlockAt(32, 78, 28);
        grids[0] = createGrid(new Vector(27, 73, 31), new Vector(4, 4, 1), 0f);
        grids[1] = createGrid(new Vector(33, 73, 23), new Vector(1, 4, 8), 90f);
        grids[2] = createGrid(new Vector(27, 73, 22), new Vector(4, 4, 1), 0f);
        grids[3] = createGrid(new Vector(41, 73, 25), new Vector(1, 4, 4), 90f);
    }

    private GridAnimation createGrid(Vector posClosed, Vector size, float yaw) {
        final int offset = 4;
        final float speedOpen = offset / (20f * 4); // 4 seconds
        final float speedClose = offset / (20f * 60 * 5); // 5 minutes
        Location loc = new Location(McBoyard.getWorld(), posClosed.getX(), posClosed.getY(), posClosed.getZ(), yaw, 0)
                .add(0.5, 0, 0.5);
        return new GridAnimation(loc, size, 4, speedOpen, speedClose);
    }

    public void reset() {
        for (GridAnimation grid : grids) {
            grid.placeOpen();
        }

        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void start() {
        for (GridAnimation grid : grids) {
            grid.closeAnimation();
        }

        ticksSalle = 20 * 60 * 5; // 5 minutes
        task = Bukkit.getScheduler().runTaskTimer(McBoyard.instance, this::onTick, 1, 1);
        McBoyard.instance.getLogger().info("CoffreFortAnimation started");
    }

    public void stop() {
        for (GridAnimation grid : grids) {
            grid.placeClosed();
        }

        if (task != null) {
            task.cancel();
            task = null;
        }
        McBoyard.instance.getLogger().info("CoffreFortAnimation stopped");
    }

    public void dropCode() {
        Block dropper = random.nextBoolean() ? dropper1 : dropper2;
        if (!dropper.getLocation().isChunkLoaded())
            return;
        CoffreCodeItem code = CoffreCodeItem.createCode();
        ItemStack item = code.getItem();

        Location loc = dropper.getLocation().add(-0.3, 0.1, 0.5);
        Item itemEntity = loc.getWorld().dropItem(loc, item);
        itemEntity.setVelocity(new Vector(
                random.nextGaussian(-0.15, 0.1),
                random.nextGaussian(0, 0.05),
                random.nextGaussian(0, 0.1)));
        loc.getWorld().playSound(loc, Sound.BLOCK_DISPENSER_DISPENSE, 1, 1);
        loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 10, 0.5, 0.5, 0.5, 0.1);
    }

    public void killCodesItems(boolean ifTooMany) {
        Location loc = new Location(dropper1.getWorld(), 28, 73, 27);
        List<Item> items = loc.getWorld().getNearbyEntities(loc, 8, 3, 8, entity -> entity instanceof Item)
                .stream()
                .map(entity -> (Item) entity)
                .filter(entity -> (entity).getItemStack().getType() == Material.PAPER)
                .toList();
        int nbItemsMax = ifTooMany ? 50 : 0;
        if (items.size() > nbItemsMax) {
            for (int i = 0; i < items.size() - nbItemsMax; i++) {
                Item item = items.get(i);
                item.remove();
            }
        }
    }

    public void playAlarme() {
        Location loc = new Location(McBoyard.getWorld(), 41, 77, 26);
        List<Player> players = loc.getWorld().getNearbyEntities(loc, 30, 10, 20, entity -> entity instanceof Player)
                .stream()
                .map(entity -> (Player) entity)
                .toList();

        for (Player player : players) {
            player.playSound(loc, "minecraft:alarme", SoundCategory.RECORDS, 0.02f, 0.5f);
        }
    }

    private void onTick() {
        ticksSalle--;
        if (ticksSalle < 0) {
            stop();
            return;
        }

        ticksUntilDrop--;
        if (ticksUntilDrop <= 0) {
            ticksUntilDrop = random.nextInt(20 * 10);
            dropCode();
        }

        if (ticksSalle % 200 == 0) {
            // every 10 seconds
            killCodesItems(true);
        }

        if (ticksSalle % 113 == 0) {
            playAlarme();
        }
    }
}
