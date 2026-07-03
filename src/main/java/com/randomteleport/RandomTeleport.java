package com.randomteleport;

import org.bukkit.plugin.java.JavaPlugin;

public class RandomTeleport extends JavaPlugin {

    private ConfigManager configManager;
    private RTPManager rtpManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        rtpManager = new RTPManager(this, configManager);

        getCommand("rtp").setExecutor(new RTPCommand(this, rtpManager, configManager));

        if (configManager.isCancelOnMove()) {
            getServer().getPluginManager().registerEvents(new TeleportListener(rtpManager, configManager), this);
        }

        getLogger().info("RandomTeleport v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RandomTeleport disabled!");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public RTPManager getRTPManager() { return rtpManager; }
}