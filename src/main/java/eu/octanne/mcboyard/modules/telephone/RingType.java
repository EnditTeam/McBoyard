package eu.octanne.mcboyard.modules.telephone;

import java.util.Collection;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public enum RingType {
    HARP(RingType::tickHarp),
    DIAL(RingType::tickDialPhone),    // normal dial phone
    GLITCH(RingType::tickGlitch),     // around the phone
    PARTICLE(RingType::tickParticle), // particles around the phone
    SMOKE(RingType::tickSmoke),       // cloud particles
    SHAKING(RingType::tickShaking),   // the camera is shaking
    DUCK(RingType::tickDuck),         // phones are ducks
    CEILING(RingType::tickCeiling),   // phones are attached to the ceiling
    TRUCK(RingType::tickTruck),       // truck horn
    HOTLINE(RingType::tickHotline),   // red phone
    ;

    private Consumer<Activity> tickConsumer;

    private RingType(Consumer<Activity> tickConsumer) {
        this.tickConsumer = tickConsumer;
    }

    public void tick(Activity activity) {
        tickConsumer.accept(activity);
    }

    public void init(Activity activity) {
        switch (this) {
            case DUCK:
                activity.setTelephoneItem(Material.TOTEM_OF_UNDYING, 0);
                break;
            case CEILING:
                activity.moveTelephones(new Vector(0, 2, 0));
                break;
            default:
                break;
        }
    }

    public boolean isTimesUp(Integer ringTick) {
        switch (this) {
            case HARP:     // 5"
            case GLITCH:   // 5"
            case PARTICLE: // 3"
            case DUCK:     // 5"
            case CEILING:  // 4"
            case TRUCK:    // 5"
                return ringTick >= 200;
            case DIAL:    // 5"
            case HOTLINE: // 5"
                return ringTick >= 240;
            case SMOKE:   // 8"
            case SHAKING: // 9"
                return ringTick >= 400;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public void stopSounds() {
        String stopSoundStr = null;
        switch (this) {
            case DIAL:
                stopSoundStr = "minecraft:telephone/dialphone";
                break;
            case SMOKE:
                Activity.getNearbyPlayers().forEach(player -> player.removePotionEffect(PotionEffectType.BLINDNESS));
                break;
            case DUCK:
                stopSoundStr = "minecraft:telephone/canard";
                break;
            case HOTLINE:
                stopSoundStr = "minecraft:telephone/hotline";
                break;
            default:
                break;
        }
        if (stopSoundStr != null) {
            final String finalStopSoundStr = stopSoundStr;
            Activity.getNearbyPlayers().forEach(player -> player.stopSound(finalStopSoundStr));
        }
    }

    private static void playSound(Location loc, Sound sound, float volume, float pitch) {
        loc.getWorld().playSound(loc, sound, volume, pitch);
    }

    private static void playSound(Location loc, String sound, float volume, float pitch) {
        loc.getWorld().playSound(loc, sound, volume, pitch);
    }

    private static void tickHarp(Activity activity) {
        int ringTick = activity.getRingTick();
        if (ringTick % 40 >= 30)
            return;
        Location loc = activity.getRingLocation();
        playSound(loc, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1.5f);
    }

    private static void tickDialPhone(Activity activity) {
        int ringTick = activity.getRingTick();
        if (ringTick % 80 == 20) {
            Location loc = activity.getRingLocation();
            playSound(loc, "minecraft:telephone/dialphone", 1, 1);
        }
    }

    private static void tickGlitch(Activity activity) {
        int ringTick = activity.getRingTick();
        int index = (ringTick / 4) % 16;
        // Move according to the index as a square around the phone
        Location loc = activity.getRingLocation().clone();
        if (index == 0) {
            loc.add(0, 0, 1);
        } else if (index == 1) {
            loc.add(1, 0, 1);
        } else if (index == 2) {
            loc.add(1, 0, 0);
        } else if (index == 3) {
            loc.add(1, 0, -1);
        } else if (index == 4) {
            loc.add(0, 0, -1);
        } else if (index == 5) {
            loc.add(-1, 0, -1);
        } else if (index == 6) {
            loc.add(-1, 0, 0);
        } else if (index == 7) {
            loc.add(-1, 0, 1);
        }
        playSound(loc, Sound.BLOCK_NOTE_BLOCK_BIT, 1, 0.5f);
    }

    private static void tickParticle(Activity activity) {
        int ringTick = activity.getRingTick();
        if (ringTick % 40 >= 20)
            return;
        Location loc = activity.getRingLocation();
        loc.getWorld().spawnParticle(Particle.NOTE, loc, 1, 0, 0, 0, 0);
    }

    private static void tickSmoke(Activity activity) {
        // Smoke around the players in survival mode
        Collection<Player> players = Activity.getNearbyPlayers();
        int i = 0;
        for (Player player : players) {
            double yaw = Math.toRadians(player.getLocation().getYaw());
            double x = -Math.sin(yaw);
            double z = Math.cos(yaw);
            player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().add(x, 1.2, z), 20, 1, 0.2, 1, 0.02);
            i++;
            if (i > 5)
                break;
        }
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 60, 0);
        for (Player player : players) {
            player.addPotionEffect(blindness);
        }
        Location loc = activity.getRingLocation();
        playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.5f);
    }

    private static void tickShaking(Activity activity) {
        int ringTick = activity.getRingTick();
        if (ringTick % 40 >= 20)
            return;
        if (ringTick % 2 == 0) {
            Collection<Player> players = Activity.getNearbyPlayers();
            for (Player player : players) {
                // Add random velocity and rotation to the player
                Vector velocity = player.getVelocity();
                velocity.add(new Vector(Math.random() * 0.2 - 0.1, 0, Math.random() * 0.2 - 0.1));
                Location locP = player.getLocation().clone();
                float yaw = locP.getYaw() + (float) (Math.random() * 2 - 1);
                float pitch = locP.getPitch() + (float) (Math.random() * 2 - 1);
                locP.setYaw(yaw);
                locP.setPitch(pitch);
                player.teleport(locP);
                player.setVelocity(velocity);
            }
        }
        Location loc = activity.getRingLocation();
        playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.5f);
    }

    private static void tickDuck(Activity activity) {
        int ringTick = activity.getRingTick();
        if (ringTick % 100 == 0) {
            Location loc = activity.getRingLocation();
            playSound(loc, "minecraft:telephone/canard", 1, 1);
        }
    }

    private static void tickCeiling(Activity activity) {
        int ringTick = activity.getRingTick();
        activity.getTelephones().forEach(entity -> {
            int seed = entity.hashCode();
            // wave of period T = 200 ticks and delta y = 0.3
            double y1 = Math.sin((ringTick + seed) / 200.0 * 2 * Math.PI) * 0.15;
            double y2 = Math.sin((ringTick + seed + 1) / 200.0 * 2 * Math.PI) * 0.15;
            Location loc = entity.getLocation();
            loc.add(0, y1 - y2, 0);
            entity.teleport(loc);
        });

        if (ringTick % 40 >= 20)
            return;
        Location loc = activity.getRingLocation();
        playSound(loc, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1, 1.5f);
    }

    private static void tickTruck(Activity activity) {
        int ringTick = activity.getRingTick();
        Location loc = activity.getRingLocation();
        if (ringTick % 80 == 0)
            playSound(loc, "minecraft:telephone/truck1", 0.5f, 1);
        if (ringTick % 80 == 20)
            playSound(loc, "minecraft:telephone/truck2", 0.5f, 1);
    }

    private static void tickHotline(Activity activity) {
        int ringTick = activity.getRingTick();
        if (ringTick % 100 == 40) {
            Location loc = activity.getRingLocation();
            playSound(loc, "minecraft:telephone/hotline", 1, 1);
        }
    }
}
