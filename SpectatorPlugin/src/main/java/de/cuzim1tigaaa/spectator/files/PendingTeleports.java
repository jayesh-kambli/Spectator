package de.cuzim1tigaaa.spectator.files;

import de.cuzim1tigaaa.spectator.Spectator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Persists pre-spectate locations across server restarts.
 * Written immediately on quit; cleared on successful rejoin teleport.
 */
public class PendingTeleports {

    private final Spectator plugin;
    private final File file;
    private final YamlConfiguration yaml;

    public PendingTeleports(Spectator plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pending_teleports.yml");
        this.yaml = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
    }

    public void save(UUID uuid, Location location) {
        String key = uuid.toString();
        yaml.set(key + ".world", location.getWorld().getName());
        yaml.set(key + ".x", location.getX());
        yaml.set(key + ".y", location.getY());
        yaml.set(key + ".z", location.getZ());
        yaml.set(key + ".yaw", (double) location.getYaw());
        yaml.set(key + ".pitch", (double) location.getPitch());
        persist();
    }

    public boolean contains(UUID uuid) {
        return yaml.contains(uuid.toString());
    }

    /** Returns the saved location and removes it, or null if not present / world unloaded. */
    public Location getAndRemove(UUID uuid) {
        String key = uuid.toString();
        if(!yaml.contains(key)) return null;

        String worldName = yaml.getString(key + ".world");
        double x     = yaml.getDouble(key + ".x");
        double y     = yaml.getDouble(key + ".y");
        double z     = yaml.getDouble(key + ".z");
        float yaw    = (float) yaml.getDouble(key + ".yaw");
        float pitch  = (float) yaml.getDouble(key + ".pitch");

        yaml.set(key, null);
        persist();

        World world = Bukkit.getWorld(worldName);
        if(world == null) return null;

        return new Location(world, x, y, z, yaw, pitch);
    }

    private void persist() {
        try {
            yaml.save(file);
        } catch(IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save pending_teleports.yml", e);
        }
    }
}
