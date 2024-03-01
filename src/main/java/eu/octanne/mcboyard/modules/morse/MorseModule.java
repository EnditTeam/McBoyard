package eu.octanne.mcboyard.modules.morse;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.PlugModule;

public class MorseModule extends PlugModule {
    private static ComputerAnimation computer = null;

    public MorseModule(JavaPlugin instance) {
        super(instance);
    }

    @Override
    public void onEnable() {
        MorseCommand morseCommand = new MorseCommand();
        pl.getCommand("morse").setExecutor(morseCommand);
        pl.getCommand("morse").setTabCompleter(morseCommand);
        Bukkit.getPluginManager().registerEvents(new MorseListener(), pl);
    }

    @Override
    public void onDisable() {
        reset();
    }

    public void reset() {
        if (computer != null) {
            computer.reset();
        }
    }

    public Block getComputerBlock() {
        World world = McBoyard.getWorld();
        // Block is at -34 84 22
        return world.getBlockAt(-34, 84, 22);
    }

    public void startComputerAnimation() {
        if (computer == null) {
            computer = new ComputerAnimation(getComputerBlock(), this::computerAnimationEnd);
        }
        computer.startAnimation();
    }

    private void computerAnimationEnd() {
        // TODO: give a book
    }

    public void interactWithComputer(Player player, ItemStack item) {
        startComputerAnimation();
        // TODO: lock the animation
    }

    public boolean isActive() {
        // TODO
        return false;
    }
}
