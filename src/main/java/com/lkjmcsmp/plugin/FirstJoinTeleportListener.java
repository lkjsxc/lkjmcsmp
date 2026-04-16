package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.persistence.FirstJoinDao;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Instant;
import java.util.logging.Logger;

public final class FirstJoinTeleportListener implements Listener {
    private final TeleportService teleportService;
    private final FirstJoinDao firstJoinDao;
    private final SchedulerBridge schedulerBridge;
    private final String firstJoinWorld;
    private final Logger logger;

    public FirstJoinTeleportListener(
            TeleportService teleportService,
            FirstJoinDao firstJoinDao,
            SchedulerBridge schedulerBridge,
            String firstJoinWorld,
            Logger logger) {
        this.teleportService = teleportService;
        this.firstJoinDao = firstJoinDao;
        this.schedulerBridge = schedulerBridge;
        this.firstJoinWorld = firstJoinWorld;
        this.logger = logger;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        schedulerBridge.runAsyncTask(() -> {
            try {
                if (firstJoinDao.hasCompleted(player.getUniqueId())) {
                    return;
                }
                schedulerBridge.runPlayerTask(player, () -> runFirstJoinTeleport(player));
            } catch (Exception ex) {
                logger.warning("First-join RTP check failed for " + player.getName() + ": " + ex.getMessage());
            }
        });
    }

    private void runFirstJoinTeleport(Player player) {
        if (!player.isOnline()) {
            return;
        }
        String targetWorld = firstJoinWorld.isBlank() ? player.getWorld().getName() : firstJoinWorld;
        teleportService.randomTeleport(player, targetWorld, true, result -> {
            if (result.success()) {
                player.sendMessage("First join setup complete: " + result.message());
                persistCompletion(player);
            } else {
                player.sendMessage("First join setup failed: " + result.message());
            }
        });
    }

    private void persistCompletion(Player player) {
        schedulerBridge.runAsyncTask(() -> {
            try {
                firstJoinDao.markCompleted(player.getUniqueId(), Instant.now());
            } catch (Exception ex) {
                logger.warning("Failed to persist first-join RTP for " + player.getName() + ": " + ex.getMessage());
            }
        });
    }
}
