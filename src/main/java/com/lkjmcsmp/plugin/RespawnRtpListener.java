package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.TeleportService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.logging.Logger;

public final class RespawnRtpListener implements Listener {
    private final TeleportService teleportService;
    private final Logger logger;

    public RespawnRtpListener(TeleportService teleportService, Logger logger) {
        this.teleportService = teleportService;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("lkjmcsmp.rtp.use")) {
            return;
        }
        Location respawn = event.getRespawnLocation();
        if (respawn == null || respawn.getWorld() == null) {
            return;
        }
        World world = respawn.getWorld();
        Location spawn = world.getSpawnLocation();
        if (event.isBedSpawn()
                || respawn.getBlockX() != spawn.getBlockX()
                || respawn.getBlockZ() != spawn.getBlockZ()) {
            return;
        }
        teleportService.randomTeleport(player, world.getName(), true, false, result -> {
            if (!result.success()) {
                player.sendMessage("Respawn RTP failed: " + result.message());
            }
        });
    }
}
