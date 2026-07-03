package com.randomteleport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPCommand implements CommandExecutor {

    private final RandomTeleport plugin;
    private final RTPManager rtpManager;
    private final ConfigManager config;

    public RTPCommand(RandomTeleport plugin, RTPManager rtpManager, ConfigManager config) {
        this.plugin = plugin;
        this.rtpManager = rtpManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rtp.use")) {
            player.sendMessage(config.color(config.getPrefix() + "&cYou don't have permission to use this command."));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("rtp.admin")) {
                player.sendMessage(config.getPrefix() + config.color("&cYou don't have permission to reload the config."));
                return true;
            }
            config.reload();
            player.sendMessage(config.getPrefix() + config.color("&aConfiguration reloaded!"));
            return true;
        }

        if (rtpManager.isOnCooldown(player)) {
            long remaining = rtpManager.getCooldownRemaining(player);
            String msg = config.getMessage("cooldown", "&cPlease wait &e{seconds}&c seconds before using /rtp again.")
                    .replace("{seconds}", String.valueOf(remaining));
            player.sendMessage(config.getPrefix() + config.color(msg));
            return true;
        }

        rtpManager.teleportRandom(player);
        return true;
    }
}