package com.randomteleport;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TeleportListener implements Listener {

    private final RTPManager rtpManager;
    private final ConfigManager config;

    public TeleportListener(RTPManager rtpManager, ConfigManager config) {
        this.rtpManager = rtpManager;
        this.config = config;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!rtpManager.hasPendingTeleport(player)) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        if (rtpManager.cancelPendingTeleport(player)) {
            player.sendMessage(config.getPrefix() + config.color(config.getMessage("cancelled", "&cTeleport cancelled because you moved!")));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        rtpManager.cancelPendingTeleport(event.getPlayer());
    }
}