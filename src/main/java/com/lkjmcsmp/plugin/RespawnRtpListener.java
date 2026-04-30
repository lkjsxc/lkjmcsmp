package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.plugin.temporarydimension.TemporaryDimensionManager;
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
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionManager temporaryDimensionManager;
    private final InitialSpawnRegistry initialSpawns;
    private final Logger logger;

    public RespawnRtpListener(TeleportService teleportService, SchedulerBridge schedulerBridge,
                              TemporaryDimensionManager temporaryDimensionManager,
                              InitialSpawnRegistry initialSpawns,
                              Logger logger) {
        this.teleportService = teleportService;
        this.schedulerBridge = schedulerBridge;
        this.temporaryDimensionManager = temporaryDimensionManager;
        this.initialSpawns = initialSpawns;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("lkjmcsmp.rtp.use")) {
            return;
        }
        if (event.getRespawnReason() != PlayerRespawnEvent.RespawnReason.DEATH) {
            return;
        }
        if (event.isBedSpawn() || event.isAnchorSpawn()) {
            return;
        }
        if (temporaryDimensionManager.isTemporaryDimensionWorld(player.getWorld().getName())) {
            return;
        }
        Location respawn = event.getRespawnLocation();
        World world = respawn.getWorld();
        if (world == null) {
            return;
        }
        if (!initialSpawns.isInitialSpawnBlock(respawn)) {
            return;
        }
        schedulerBridge.runPlayerDelayedTask(player, 1L, () -> teleportService.randomTeleport(player, world.getName(), true, false, result -> {
            if (!result.success()) {
                player.sendMessage("Respawn RTP failed: " + result.message());
                logger.warning("Respawn RTP failed for " + player.getUniqueId() + ": " + result.message());
            }
        }));
    }

}
