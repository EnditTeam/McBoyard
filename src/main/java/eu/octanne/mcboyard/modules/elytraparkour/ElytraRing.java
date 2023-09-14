package eu.octanne.mcboyard.modules.elytraparkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ElytraRing implements ConfigurationSerializable  {

    private final Location loc;
    private final int nbDuraGiveback;

    public ElytraRing(Location loc, int nbDuraGiveback) {
        this.loc = loc;
        this.nbDuraGiveback = nbDuraGiveback;
    }

    public ElytraRing(Map<String, Object> map) {
        World world = Bukkit.getWorld((String) map.get("worldName"));
        loc = new Location(world, (double) map.get("x"), (double) map.get("y"), (double) map.get("z"));
        nbDuraGiveback = (int) map.get("nbDuraGiveback");
    }

    @Override
    public String toString() {
        return "ElytraRing{" +
                "loc=(" + getLocString() +
                "), nbDuraGiveback=" + nbDuraGiveback +
                '}';
    }

    private String getLocString() {
        return loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("worldName", loc.getWorld().getName());
        map.put("x", loc.getX());
        map.put("y", loc.getY());
        map.put("z", loc.getZ());
        map.put("nbDuraGiveback", nbDuraGiveback);
        return map;
    }

    public Location getLocation() {
        return loc;
    }

    public int getNbDuraGiveback() {
        return nbDuraGiveback;
    }
}
