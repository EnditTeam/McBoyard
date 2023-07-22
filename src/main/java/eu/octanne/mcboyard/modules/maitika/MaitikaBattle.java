package eu.octanne.mcboyard.modules.maitika;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import eu.octanne.mcboyard.McBoyard;

public class MaitikaBattle {
    private int ticks = 0;
    private BukkitTask task;
    private Consumer<Boolean> onEnd;
    private MaitikaEntity maitika;

    public MaitikaBattle(Consumer<Boolean> onEnd) {
        this.onEnd = onEnd;
    }

    public void start() {
        if (task != null)
            return;
        task = Bukkit.getScheduler().runTaskTimer(McBoyard.instance, this::onTick, 1, 1);
        McBoyard.instance.getLogger().info("Maitika battle started.");
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        McBoyard.maitikaModule.removeArena();
        McBoyard.maitikaModule.removeMaitika();
        McBoyard.instance.getLogger().info("Maitika battle stopped.");
        onEnd.accept(false);
    }

    private void onTick() {
        ticks++;
        if (ticks >= 2400) {
            stop();
        }

        if (ticks == 1) {
            McBoyard.maitikaModule.placeArena();
            for (Player player : McBoyard.maitikaModule.getArenaCenter().getNearbyPlayers(3)) {
                McBoyard.maitikaModule.equipGoodArmor(player);
            }
        } else if (ticks == 40) {
            maitika = McBoyard.maitikaModule.spawnMaitika();
        } else if (ticks == 100) {
            Location center = McBoyard.maitikaModule.getArenaCenter();
            Player nearestPlayer = center.getNearbyPlayers(3)
                    .stream()
                    .filter(player -> player.getGameMode() == org.bukkit.GameMode.SURVIVAL
                            || player.getGameMode() == org.bukkit.GameMode.ADVENTURE)
                    .min((p1, p2) -> {
                        double d1 = p1.getLocation().distanceSquared(center);
                        double d2 = p2.getLocation().distanceSquared(center);
                        return Double.compare(d1, d2);
                    }).orElse(null);
            maitika.getBukkitMonster().damage(0.1, nearestPlayer);
        } else if (ticks == 1200) {
            for (Player player : McBoyard.maitikaModule.getArenaCenter().getNearbyPlayers(3)) {
                McBoyard.maitikaModule.equipBadArmor(player);
            }
            McBoyard.instance.getLogger().info("Maitika battle half time.");
        }
    }
}
