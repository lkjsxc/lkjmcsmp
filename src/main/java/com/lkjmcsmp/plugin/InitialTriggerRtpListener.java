package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.TeleportHudSink;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class InitialTriggerRtpListener implements Listener {
    private final TeleportService teleportService;
    private final TeleportHudSink hudSink;
    private final SchedulerBridge schedulerBridge;
    private final InitialTriggerRtpConfig config;
    private final Logger logger;
    private final Set<UUID> armed = ConcurrentHashMap.newKeySet();

    public InitialTriggerRtpListener(
            TeleportService teleportService,
            TeleportHudSink hudSink,
            SchedulerBridge schedulerBridge,
            InitialTriggerRtpConfig config,
            Logger logger) {
        this.teleportService = teleportService;
        this.hudSink = hudSink;
        this.schedulerBridge = schedulerBridge;
        this.config = config;
        this.logger = logger;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scheduleArmCheck(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        scheduleArmCheck(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        scheduleArmCheck(event.getPlayer());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        scheduleArmCheck(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        armed.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || sameBlock(from, to)) {
            return;
        }
        tryArm(event.getPlayer());
    }

    private void tryArm(Player player) {
        UUID playerId = player.getUniqueId();
        if (armed.contains(playerId) || !insideZone(player)) {
            return;
        }
        arm(player);
    }

    private void arm(Player player) {
        UUID playerId = player.getUniqueId();
        if (!armed.add(playerId)) {
            return;
        }
        player.sendMessage("Random teleport starts in " + config.countdownSeconds() + "s. Stay in the spawn zone.");
        runCountdown(player, config.countdownSeconds());
    }

    private void runCountdown(Player player, int secondsRemaining) {
        UUID playerId = player.getUniqueId();
        if (!eligible(player)) {
            armed.remove(playerId);
            return;
        }
        if (config.cancelOnExit() && !insideZone(player)) {
            armed.remove(playerId);
            player.sendMessage("Initial random teleport cancelled: left the trigger zone.");
            return;
        }
        if (secondsRemaining <= 0) {
            completeCountdown(player);
            return;
        }
        hudSink.onTeleportCountdown(player, secondsRemaining);
        schedulerBridge.runPlayerDelayedTask(player, 20L, () -> runCountdown(player, secondsRemaining - 1));
    }

    private void completeCountdown(Player player) {
        String targetWorld = config.targetWorld().isBlank() ? player.getWorld().getName() : config.targetWorld();
        teleportService.randomTeleport(player, targetWorld, true, false, result -> {
            armed.remove(player.getUniqueId());
            player.sendMessage(result.message());
            if (!result.success()) {
                logger.fine("Initial trigger RTP failed for " + player.getUniqueId() + ": " + result.message());
            }
        });
    }

    private void scheduleArmCheck(Player player) {
        schedulerBridge.runPlayerDelayedTask(player, 1L, () -> tryArm(player));
    }

    private boolean eligible(Player player) {
        return player.isOnline() && player.isValid() && !player.isDead();
    }

    private boolean insideZone(Player player) {
        if (!eligible(player) || player.getWorld() == null) {
            return false;
        }
        if (!player.getWorld().getName().equals(config.triggerWorld())) {
            return false;
        }
        Location loc = player.getLocation();
        double dx = loc.getX() - config.centerX();
        double dz = loc.getZ() - config.centerZ();
        return dx * dx + dz * dz <= config.radius() * config.radius();
    }

    private static boolean sameBlock(Location from, Location to) {
        return from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()
                && from.getWorld() == to.getWorld();
    }
}
