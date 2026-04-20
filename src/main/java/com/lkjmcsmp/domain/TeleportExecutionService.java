package com.lkjmcsmp.domain;

import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.function.Consumer;

final class TeleportExecutionService {
    private final SchedulerBridge schedulerBridge;
    private final Duration stabilityDelay;
    private final double stabilityRadiusBlocks;
    private final TeleportHudSink teleportHudSink;

    TeleportExecutionService(
            SchedulerBridge schedulerBridge,
            Duration stabilityDelay,
            double stabilityRadiusBlocks,
            TeleportHudSink teleportHudSink) {
        this.schedulerBridge = schedulerBridge;
        this.stabilityDelay = stabilityDelay;
        this.stabilityRadiusBlocks = stabilityRadiusBlocks;
        this.teleportHudSink = teleportHudSink == null ? TeleportHudSink.NO_OP : teleportHudSink;
    }

    void teleport(
            Player actor,
            Location destination,
            String successMessage,
            boolean applyStabilityDelay,
            Consumer<TeleportService.Result> callback) {
        if (!applyStabilityDelay || stabilityDelay.isZero()) {
            executeTeleport(actor, destination, successMessage, callback);
            return;
        }
        schedulerBridge.runPlayerTask(actor, () -> {
            if (!actor.isOnline()) {
                complete(actor, callback, TeleportService.Result.fail("player went offline"));
                return;
            }
            Location origin = actor.getLocation().clone();
            long seconds = Math.max(0L, stabilityDelay.toSeconds());
            actor.sendMessage("Teleport starts in " + seconds
                    + "s. Stay within " + stabilityRadiusBlocks + " block(s).");
            runDelayStep(actor, origin, destination, successMessage, seconds, callback);
        });
    }

    private void runDelayStep(
            Player actor,
            Location origin,
            Location destination,
            String successMessage,
            long secondsRemaining,
            Consumer<TeleportService.Result> callback) {
        if (!actor.isOnline()) {
            complete(actor, callback, TeleportService.Result.fail("player went offline"));
            return;
        }
        if (movedTooFar(actor.getLocation(), origin)) {
            complete(actor, callback, TeleportService.Result.fail("teleport cancelled: you moved"));
            return;
        }
        if (secondsRemaining <= 0) {
            executeTeleport(actor, destination, successMessage, callback);
            return;
        }
        teleportHudSink.onTeleportCountdown(actor, secondsRemaining);
        schedulerBridge.runPlayerDelayedTask(actor, 20L,
                () -> runDelayStep(actor, origin, destination, successMessage, secondsRemaining - 1L, callback));
    }

    private void executeTeleport(
            Player actor,
            Location destination,
            String successMessage,
            Consumer<TeleportService.Result> callback) {
        schedulerBridge.runPlayerTask(actor, () -> {
            if (!actor.isOnline()) {
                complete(actor, callback, TeleportService.Result.fail("player went offline"));
                return;
            }
            actor.teleportAsync(destination).whenComplete((moved, ex) -> {
                if (ex != null) {
                    String detail = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
                    complete(actor, callback, TeleportService.Result.fail("teleport failed: " + detail));
                    return;
                }
                complete(actor, callback, Boolean.TRUE.equals(moved)
                        ? TeleportService.Result.ok(successMessage)
                        : TeleportService.Result.fail("teleport failed"));
            });
        });
    }

    private boolean movedTooFar(Location current, Location origin) {
        if (current.getWorld() == null || origin.getWorld() == null) {
            return true;
        }
        if (!current.getWorld().getUID().equals(origin.getWorld().getUID())) {
            return true;
        }
        return current.distanceSquared(origin) > stabilityRadiusBlocks * stabilityRadiusBlocks;
    }

    private void complete(Player player, Consumer<TeleportService.Result> callback, TeleportService.Result result) {
        schedulerBridge.runPlayerTask(player, () -> {
            teleportHudSink.onTeleportResult(player, result.success(), result.message());
            callback.accept(result);
        });
    }
}
