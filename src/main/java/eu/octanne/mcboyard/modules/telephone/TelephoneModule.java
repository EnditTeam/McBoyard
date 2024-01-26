package eu.octanne.mcboyard.modules.telephone;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import eu.octanne.mcboyard.modules.PlugModule;

public class TelephoneModule extends PlugModule {
    private Activity activity = null;

    public TelephoneModule(JavaPlugin instance) {
        super(instance);
    }

    @Override
    public void onEnable() {
        TelephoneCommand telephoneCommand = new TelephoneCommand();
        pl.getCommand("telephone").setExecutor(telephoneCommand);
        pl.getCommand("telephone").setTabCompleter(telephoneCommand);
        Bukkit.getPluginManager().registerEvents(new TelephoneListener(), pl);
    }

    @Override
    public void onDisable() {
        stop();
    }

    public void start() {
        if (activity == null) {
            activity = new Activity();
            activity.start();
        }
    }

    public void stop() {
        if (activity != null) {
            activity.stop();
            activity = null;
        }
    }

    public void restart() {
        if (activity != null) {
            activity.stop();
            activity.start();
        } else {
            start();
        }
    }
}
