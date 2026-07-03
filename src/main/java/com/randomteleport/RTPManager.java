package com.randomteleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RTPManager {

    private final RandomTeleport plugin;
    private final ConfigManager config;
    private final Random random = new Random();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> pendingTeleports = new HashMap<>();
    private static final int MAX_ATTEMPTS = 50;

    public RTPManager(RandomTeleport plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean isOnCooldown(Player player) {
        if (player.hasPermission("rtp.bypasscooldown")) return false;
        Long lastUsed = cooldowns.get(player.getUniqueId());
        if (lastUsed == null) return false;
        long elapsed = System.currentTimeMillis() - lastUsed;
        return elapsed < config.getCooldown() * 1000L;
    }

    public long getCooldownRemaining(Player player) {
        Long lastUsed = cooldowns.get(player.getUniqueId());
        if (lastUsed == null) return 0;
        long elapsed = System.currentTimeMillis() - lastUsed;
        long remaining = (config.getCooldown() * 1000L) - elapsed;
        return Math.max(0, remaining / 1000 + 1);
    }

    public void teleportRandom(Player player) {
        int delay = config.getTeleportDelay();

        if (delay <= 0) {
            Location loc = findSafeLocation(player.getWorld());
            if (loc != null) {
                player.teleport(loc);
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                player.sendMessage(config.getPrefix() + config.color(config.getMessage("teleported", "&aTeleported to a random location!")));
            } else {
                player.sendMessage(config.getPrefix() + config.color(config.getMessage("no-safe-location", "&cCould not find a safe location. Try again.")));
            }
            return;
        }

        // Delayed teleport
        String countdownMsg = config.getMessage("teleporting", "&aTeleporting in &e{seconds}&a seconds. Don't move!")
                .replace("{seconds}", String.valueOf(delay));
        player.sendMessage(config.getPrefix() + config.color(countdownMsg));

        UUID uuid = player.getUniqueId();
        int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (!player.isOnline()) {
                pendingTeleports.remove(uuid);
                return;
            }
            Location loc = findSafeLocation(player.getWorld());
            if (loc != null) {
                player.teleport(loc);
                cooldowns.put(uuid, System.currentTimeMillis());
                player.sendMessage(config.getPrefix() + config.color(config.getMessage("teleported", "&aTeleported to a random location!")));
            } else {
                player.sendMessage(config.getPrefix() + config.color(config.getMessage("no-safe-location", "&cCould not find a safe location. Try again.")));
            }
            pendingTeleports.remove(uuid);
        }, delay * 20L);

        pendingTeleports.put(uuid, taskId);
    }

    public boolean cancelPendingTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        Integer taskId = pendingTeleports.remove(uuid);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            return true;
        }
        return false;
    }

    public boolean hasPendingTeleport(Player player) {
        return pendingTeleports.containsKey(player.getUniqueId());
    }

    public Location findSafeLocation(World world) {
        int[] center = resolveCenter(world);
        int centerX = center[0];
        int centerZ = center[1];
        int minR = config.getMinRadius();
        int maxR = config.getMaxRadius();

        if (minR >= maxR) maxR = minR + 1;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int x = centerX + (random.nextBoolean() ? 1 : -1) * (minR + random.nextInt(maxR - minR));
            int z = centerZ + (random.nextBoolean() ? 1 : -1) * (minR + random.nextInt(maxR - minR));

            Block ground = world.getHighestBlockAt(x, z);
            if (ground == null || ground.getType() == Material.AIR) continue;

            Material groundType = ground.getType();
            if (groundType == Material.LAVA || groundType == Material.MAGMA_BLOCK ||
                groundType == Material.CACTUS || groundType == Material.FIRE ||
                groundType == Material.SWEET_BERRY_BUSH || groundType.name().contains("CAMPFIRE")) continue;

            Block above = ground.getRelative(0, 1, 0);
            Block head = ground.getRelative(0, 2, 0);

            if (!isPassable(above) || !isPassable(head)) continue;
            if (above.getType() == Material.WATER || above.getType() == Material.LAVA) continue;

            Location loc = ground.getLocation().add(0.5, 1, 0.5);
            loc.setYaw(random.nextFloat() * 360);

            if (!loc.isChunkLoaded()) loc.getChunk().load();
            return loc;
        }
        return null;
    }

    private int[] resolveCenter(World world) {
        String centerStr = config.getCenter();
        if (centerStr.equalsIgnoreCase("worldspawn")) {
            Location spawn = world.getSpawnLocation();
            return new int[]{spawn.getBlockX(), spawn.getBlockZ()};
        }
        String[] parts = centerStr.split(",");
        if (parts.length >= 2) {
            try {
                return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
            } catch (NumberFormatException ignored) {}
        }
        return new int[]{0, 0};
    }

    private boolean isPassable(Block block) {
        Material type = block.getType();
        return type.isAir() || type.name().contains("BAMBOO") || type.name().contains("SAPLING") ||
               type.name().contains("FLOWER") || type.name().contains("GRASS") || type.name().contains("DEAD") ||
               type.name().contains("VINE") || type.name().contains("MUSHROOM") || type.name().contains("SNOW") ||
               type.name().contains("CARPET") || type.name().contains("TORCH") || type.name().contains("SIGN");
    }
}