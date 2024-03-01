package eu.octanne.mcboyard.modules.morse;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.scheduler.BukkitTask;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import eu.octanne.mcboyard.McBoyard;

public class ComputerAnimation {
    private Block headBlock;
    private BukkitTask taskFrame;
    private BukkitTask taskSound;
    private int frame;
    private Runnable onEnd;
    private boolean playedBefore;

    public ComputerAnimation(Block headBlock, Runnable onEnd) {
        this.headBlock = headBlock;
        setFrame(0);
        this.onEnd = onEnd;
    }

    public void reset() {
        if (taskFrame != null) {
            taskFrame.cancel();
            taskFrame = null;
        }
        if (taskSound != null) {
            taskSound.cancel();
            taskSound = null;
        }
        setFrame(0);
    }

    public void startAnimation() {
        reset();
        taskFrame = McBoyard.instance.getServer().getScheduler().runTaskTimer(McBoyard.instance, () -> {
            if (frame > 8) {
                reset();
                onEnd.run();
            } else {
                frame++;
                setFrame(frame);
            }
        }, 0, 10);
        taskSound = McBoyard.instance.getServer().getScheduler().runTaskTimer(
            McBoyard.instance, () -> { playedBefore = playRandomSound(headBlock.getLocation(), playedBefore); }, 0, 1);
    }

    private void setFrame(int frame) {
        this.frame = frame;
        String textureValue = getTextureValue(frame);

        Skull skull = (Skull) headBlock.getState();
        PlayerProfile profile = skull.getPlayerProfile();
        Set<ProfileProperty> properties = profile.getProperties();

        // Set the texture value
        properties.removeIf(prop -> prop.getName().equals("textures"));
        properties.add(new ProfileProperty("textures", textureValue));

        profile.setProperties(properties);
        skull.setPlayerProfile(profile);
        skull.update();
    }

    /**
     * Return the texture value of the skin for the frame.
     * Original skin author: Lord Razen
     * Skin edited by Jiogo18 for this animation
     */
    public static String getTextureValue(int frame) {
        switch (frame) {
            case 0:
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwOTMxMTc0NjA5MiwKICAicHJvZmlsZUlkIiA6ICIzMjhkYjMwNjM4Zjc0MGE4OTliMGFkMWY1YTk0NzhiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJKYXJ2ZW5fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FmYjZjZjQ5MThiOTg3MzAyNjlhN2QzOTkyMmQxMGYwYTkzZmI4YzdjY2MxYjBjZmM5OTM4YWM4MjViMTAwNTciCiAgICB9CiAgfQp9";
            case 1:
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwOTMxMTY3NDQ5NCwKICAicHJvZmlsZUlkIiA6ICIzMjhkYjMwNjM4Zjc0MGE4OTliMGFkMWY1YTk0NzhiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJKYXJ2ZW5fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NmYjI5Mzc3YmIxOTQ3OGUwZTM5MTIxMGI0MDA1NmM1NjUzM2RjOGExNjExNGE1YTkzY2I0Njk2NjRmOTZjOSIKICAgIH0KICB9Cn0=";
            case 2:
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwOTMxMTcxNjc1MywKICAicHJvZmlsZUlkIiA6ICIzMjhkYjMwNjM4Zjc0MGE4OTliMGFkMWY1YTk0NzhiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJKYXJ2ZW5fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJkZTg4MDBhOGYwY2RhNWE4YTIwMjI1ZjhhNTA4MDJhM2ZkNTg5NmY3NzUxMWMyOTVkNjllZTQ1MTJkZTkwZTUiCiAgICB9CiAgfQp9";
            case 3:
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwOTMxMTYyOTY0NSwKICAicHJvZmlsZUlkIiA6ICIzMjhkYjMwNjM4Zjc0MGE4OTliMGFkMWY1YTk0NzhiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJKYXJ2ZW5fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYzODhkOWQxYWQ5ZWU5OTRkZWNkNzA2NjZhM2NmNmNjOWQxNGYyOTJlNGEyZGRkYmZmMjVhZjVkYmEyYTNlODUiCiAgICB9CiAgfQp9";
            case 4:
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwOTMxMTU5NjI4NiwKICAicHJvZmlsZUlkIiA6ICIzMjhkYjMwNjM4Zjc0MGE4OTliMGFkMWY1YTk0NzhiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJKYXJ2ZW5fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NiZjJlZDBmMzM2ODFiM2Q5NWVkZTFiODRkZTQxZmExZDdlNTY2ZGRmMzM2MTc3ZGZmMzliMTdiNDgyYWIxZmEiCiAgICB9CiAgfQp9";
            case 5:
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwOTMxMTU1MzI0NywKICAicHJvZmlsZUlkIiA6ICIzMjhkYjMwNjM4Zjc0MGE4OTliMGFkMWY1YTk0NzhiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJKYXJ2ZW5fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q0ODdhZWUxYmViYjFlMTZjNzcyZTc4MDU2ZWUyM2ZjYTlmZmZkNGJkOTIyZTRlNTlmOTc5OWIzZTZhZThkZjEiCiAgICB9CiAgfQp9";
            case 6:
            case 7:
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwOTMxMTQ4ODEyMywKICAicHJvZmlsZUlkIiA6ICIzMjhkYjMwNjM4Zjc0MGE4OTliMGFkMWY1YTk0NzhiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJKYXJ2ZW5fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM2N2Y5YzFkYTkxOTgwODQ1MWViZDllOGJhNzRjM2Q4MWU3MWIyZjc3NTk5NWMyOTk2ZjY3MGU1ODU2MTAyNjEiCiAgICB9CiAgfQp9";
            case 8:
            default:
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwOTMxMTQ1ODE5OSwKICAicHJvZmlsZUlkIiA6ICIzMjhkYjMwNjM4Zjc0MGE4OTliMGFkMWY1YTk0NzhiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJKYXJ2ZW5fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlhYTg3NmM3ODQ4NDI1ZWM4YmIwYmIwZWE3YjYxMTRmNDJjOGRjNTRlYTQwN2M4NTE2NmY3ZmZjZDRlOWMwZGUiCiAgICB9CiAgfQp9";
        }
    }

    public static boolean playRandomSound(Location loc, boolean playedBefore) {

        // There is a 75 % chance to do the same action again
        boolean play = playedBefore ^ (Math.random() < 0.80);

        if (play) {
            // Can play more than one sound at once with a different probability
            playSoundWithProbability(loc, Sound.BLOCK_NOTE_BLOCK_SNARE, 0.5f, 0.5f, 0.05f);
            playSoundWithProbability(loc, Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 0.5f, 0.1f);
            playSoundWithProbability(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f, 0.2f);
            playSoundWithProbability(loc, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 0.5f, 0.2f);
        }
        return play;
    }

    private static void playSoundWithProbability(Location loc, Sound sound, float volume, float pitch, float probability) {
        if (Math.random() < probability) {
            loc.getWorld().playSound(loc, sound, volume, pitch);
        }
    }
}
