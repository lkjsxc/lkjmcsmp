package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.plugin.SchedulerBridge;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
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

public final class TemporaryDimensionListener implements Listener {
    private final TemporaryDimensionManager manager;
    private final SchedulerBridge schedulerBridge;
    private final Logger logger;
    private final ActionBarRouter actionBarRouter;

    public TemporaryDimensionListener(TemporaryDimensionManager manager, SchedulerBridge schedulerBridge, Logger logger,
                                      ActionBarRouter actionBarRouter) {
        this.manager = manager;
        this.schedulerBridge = schedulerBridge;
        this.logger = logger;
        this.actionBarRouter = actionBarRouter;
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
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            String fromWorld = event.getFrom().getWorld() != null ? event.getFrom().getWorld().getName() : "";
            if (manager.isTemporaryDimensionWorld(fromWorld)) {
                manager.removeParticipant(fromWorld, event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        String fromWorld = event.getFrom().getWorld() != null ? event.getFrom().getWorld().getName() : "";
        if (manager.isTemporaryDimensionWorld(fromWorld)) {
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String toWorld = player.getWorld().getName();
        if (manager.isTemporaryDimensionWorld(toWorld)) {
            var instance = manager.findInstanceByWorld(toWorld);
            if (instance != null) {
                long remaining = Duration.between(Instant.now(), instance.expirationTime()).getSeconds();
                actionBarRouter.onEnterTemporaryDimension(player, Math.max(0, remaining));
            }
        } else if (manager.isTemporaryDimensionWorld(event.getFrom().getName())) {
            actionBarRouter.onLeaveTemporaryDimension(player);
        }
    }
}
