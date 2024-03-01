package eu.octanne.mcboyard.modules.morse;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import eu.octanne.mcboyard.McBoyard;
import eu.octanne.mcboyard.modules.PlugModule;

public class MorseModule extends PlugModule {
    private ComputerAnimation computer = null;
    private String computerInputWord = null;
    private Player computerPlayer = null;
    private Block computerBlock = null;

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
        computerInputWord = null;
    }

    public Block getComputerBlock() {
        if (computerBlock == null) {
            World world = McBoyard.getWorld();
            computerBlock = world.getBlockAt(-34, 84, 22);
        }
        return computerBlock;
    }

    public boolean computerInteraction(Player player, ItemStack item) {
        if (computerInputWord != null) {
            player.sendMessage("§cL'ordinateur est occupé.");
            return false;
        }

        computerPlayer = player;
        computerInputWord = "MORSE";
        if (item != null) {
            computerInputWord = MorseTranslator.getItemName(item);
        }

        player.sendMessage("§eL'ordinateur analyse le mot...");
        if (computer == null) {
            computer = new ComputerAnimation(getComputerBlock(), this::computerAnimationEnd);
        }
        computer.startAnimation();
        return true;
    }

    private void computerAnimationEnd() {
        if (computerPlayer != null) {
            computerPlayer.sendMessage("§eL'ordinateur a fini d'analyser le mot.");
            computerPlayer = null;
        }
        if (computerInputWord != null) {
            spawnTranslatedBook(computerInputWord);
            computerInputWord = null;
        }
    }

    /**
     * Spawn a book with the MORSE translation of the word
     */
    protected void spawnTranslatedBook(String word) {
        ItemStack book = MorseTranslator.translateWithBook(word);
        Location loc = getComputerBlock().getLocation().add(0.5, 0.5, 0.5);
        loc.getWorld().dropItem(loc, book);
    }

    public boolean isActive() {
        // TODO
        return false;
    }
}
