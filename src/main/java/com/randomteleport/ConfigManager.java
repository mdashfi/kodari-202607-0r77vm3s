package com.randomteleport;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public int getMinRadius() { return config.getInt("min-radius", 500); }
    public int getMaxRadius() { return config.getInt("max-radius", 2000); }
    public String getCenter() { return config.getString("center", "worldspawn"); }
    public int getTeleportDelay() { return config.getInt("teleport-delay", 5); }
    public int getCooldown() { return config.getInt("cooldown", 60); }
    public boolean isCancelOnMove() { return config.getBoolean("cancel-on-move", true); }

    public boolean isEconomyEnabled() { return config.getBoolean("economy.enabled", false); }
    public double getCost() { return config.getDouble("economy.cost", 100.0); }

    public String getPrefix() {
        return color(config.getString("messages.prefix", "&8[&bRTP&8] &7"));
    }

    public String getMessage(String path, String def) {
        return config.getString("messages." + path, def);
    }

    public String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}