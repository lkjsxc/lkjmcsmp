package com.lkjmcsmp.plugin.temporaryend;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.plugin.SchedulerBridge;
import com.lkjmcsmp.plugin.hud.ActionBarHudService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

public final class TemporaryEndListener implements Listener {
    private final TemporaryEndManager manager;
    private final SchedulerBridge schedulerBridge;
    private final Logger logger;
    private final ActionBarHudService actionBarHudService;

    public TemporaryEndListener(TemporaryEndManager manager, SchedulerBridge schedulerBridge, Logger logger,
                                ActionBarHudService actionBarHudService) {
        this.manager = manager;
        this.schedulerBridge = schedulerBridge;
        this.logger = logger;
        this.actionBarHudService = actionBarHudService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        schedulerBridge.runAsyncTask(() -> {
            var pending = manager.pollPendingReturns(player.getUniqueId());
            if (pending == null) return;
            schedulerBridge.runPlayerTask(player, () -> {
                if (!player.isOnline()) return;
                World world = Bukkit.getWorld(pending.world());
                if (world == null) world = Bukkit.getWorlds().get(0);
                Location loc = new Location(world, pending.x(), pending.y(), pending.z(), pending.yaw(), pending.pitch());
                player.teleportAsync(loc).whenComplete((ok, ex) -> {
                    if (ex != null) {
                        logger.warning("Deferred return failed for " + player.getUniqueId() + ": " + ex.getMessage());
                    }
                });
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // No-op; expiry handles offline participants via DB records.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            String fromWorld = event.getFrom().getWorld() != null ? event.getFrom().getWorld().getName() : "";
            if (manager.isTemporaryEndWorld(fromWorld)) {
                manager.removeParticipant(fromWorld, event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        String fromWorld = event.getFrom().getWorld() != null ? event.getFrom().getWorld().getName() : "";
        if (manager.isTemporaryEndWorld(fromWorld)) {
            // Allow vanilla portal mechanics.
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String toWorld = player.getWorld().getName();
        if (manager.isTemporaryEndWorld(toWorld)) {
            var instance = manager.findInstanceByWorld(toWorld);
            if (instance != null) {
                long remaining = Duration.between(Instant.now(), instance.expirationTime()).getSeconds();
                actionBarHudService.onEnterTemporaryEnd(player, Math.max(0, remaining));
            }
        } else if (manager.isTemporaryEndWorld(event.getFrom().getName())) {
            actionBarHudService.onLeaveTemporaryEnd(player);
        }
    }
}
