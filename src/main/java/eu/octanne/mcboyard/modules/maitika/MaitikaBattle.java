package eu.octanne.mcboyard.modules.maitika;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import eu.octanne.mcboyard.McBoyard;

public class MaitikaBattle {
    private int ticks = 0;
    private BukkitTask task;
    private Consumer<Boolean> onEnd;
    private MaitikaEntity maitika;
    private BossBar bossBar = null;

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
        for (Player player : Bukkit.getOnlinePlayers()) {
            McBoyard.maitikaModule.removeArmor(player);
        }
        McBoyard.instance.getLogger().info("Maitika battle stopped.");
        onEnd.accept(false);
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    private void setupArena() {
        McBoyard.maitikaModule.placeArena();
        for (Player player : McBoyard.maitikaModule.getArenaCenter().getNearbyPlayers(3)) {
            McBoyard.maitikaModule.equipGoodArmor(player);
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SURVIVAL)
                player.setGameMode(GameMode.ADVENTURE);
        }
        bossBar = Bukkit.createBossBar("Maïtika", org.bukkit.boss.BarColor.YELLOW,
                org.bukkit.boss.BarStyle.SEGMENTED_12);
        bossBar.setVisible(true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
        bossBar.setProgress(1.0);
    }

    public Player getNearestPlayer() {
        Location center = McBoyard.maitikaModule.getArenaCenter();
        return center.getNearbyPlayers(3)
                .stream()
                .filter(player -> player.getGameMode() == org.bukkit.GameMode.SURVIVAL
                        || player.getGameMode() == org.bukkit.GameMode.ADVENTURE)
                .min((p1, p2) -> {
                    double d1 = p1.getLocation().distanceSquared(center);
                    double d2 = p2.getLocation().distanceSquared(center);
                    return Double.compare(d1, d2);
                }).orElse(null);
    }

    private void makeMaitikaAngryAtPlayer() {
        // Make Maitika attack the nearest player
        // (with setGoalTarget, the spider lose the target 10 seconds after)
        Player nearestPlayer = getNearestPlayer();
        maitika.getBukkitMonster().damage(0.1, nearestPlayer);
    }

    private void onTick() {
        ticks++;
        if (ticks >= 2400) {
            stop();
            return;
        }

        if (ticks == 1) {
            setupArena();
        } else if (ticks == 40) {
            maitika = McBoyard.maitikaModule.spawnMaitika();
        } else if (ticks == 100) {
            makeMaitikaAngryAtPlayer();
        } else if (ticks == 1200) {
            McBoyard.instance.getLogger().info("Maitika battle half time.");
        } else if (ticks == 2300) {
            McBoyard.maitikaModule.removeMaitika();
            if (bossBar != null)
                bossBar.setProgress(0);
        }

        if (ticks < 2300 && ticks % 20 == 0 && bossBar != null) {
            bossBar.setProgress(1.0 - ticks / 2300.0);
        }
    }
}
