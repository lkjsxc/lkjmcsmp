package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.TemporaryDimensionInstance;
import com.lkjmcsmp.persistence.TemporaryDimensionDao;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class TemporaryDimensionTransfer {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionDao temporaryDimensionDao;
    private final Logger logger;
    private final int transferRadius;
    private final TemporaryDimensionWorldFactory worldFactory;

    public TemporaryDimensionTransfer(SchedulerBridge schedulerBridge, TemporaryDimensionDao temporaryDimensionDao,
                                      Logger logger, int transferRadius, TemporaryDimensionWorldFactory worldFactory) {
        this.schedulerBridge = schedulerBridge;
        this.temporaryDimensionDao = temporaryDimensionDao;
        this.logger = logger;
        this.transferRadius = transferRadius;
        this.worldFactory = worldFactory;
    }

    public void captureAndTransfer(Location origin, World world, String instanceId, Player creator,
                                   Consumer<Boolean> creatorCallback) {
        Location spawn = worldFactory.resolveSpawnLocation(world);
        schedulerBridge.runPlayerTask(creator, () -> {
            if (!eligible(creator, origin, transferRadius * (double) transferRadius)) {
                creatorCallback.accept(false);
                return;
            }
            NamedLocation returnLoc = returnLocation(creator);
            schedulerBridge.runAsyncTask(() -> persistThenTeleport(creator, instanceId, returnLoc, spawn, ok -> {
                    if (ok) scheduleNearby(origin, world, instanceId, creator.getUniqueId(), spawn);
                    creatorCallback.accept(ok);
            }));
        });
    }

    private void scheduleNearby(Location origin, World world, String instanceId, UUID creatorId, Location spawn) {
        double radiusSq = transferRadius * (double) transferRadius;
        List<UUID> candidateIds = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            candidateIds.add(player.getUniqueId());
        }
        for (UUID playerId : candidateIds) {
            if (playerId.equals(creatorId)) continue;
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) continue;
            schedulerBridge.runPlayerTask(player, () -> {
                if (eligible(player, origin, radiusSq)) {
                    NamedLocation returnLoc = returnLocation(player);
                    schedulerBridge.runAsyncTask(() -> persistThenTeleport(
                            player, instanceId, returnLoc, spawn, ok -> { }));
                }
            });
        }
        logger.info("Scheduled transfers for temporary dimension instance " + instanceId);
    }

    private void persistThenTeleport(Player player, String instanceId, NamedLocation returnLoc, Location spawn,
                                     Consumer<Boolean> callback) {
        try {
            temporaryDimensionDao.insertParticipant(instanceId, player.getUniqueId(), returnLoc);
        } catch (Exception e) {
            logger.warning("Failed to insert participant: " + e.getMessage());
            callback.accept(false);
            return;
        }
        schedulerBridge.runPlayerTask(player, () -> {
            if (player.isOnline() && player.isValid()) {
                player.teleportAsync(spawn).whenComplete((ok, ex) -> callback.accept(ex == null && Boolean.TRUE.equals(ok)));
            } else {
                callback.accept(false);
            }
        });
    }

    private static boolean eligible(Player player, Location origin, double radiusSq) {
        return player.isOnline()
                && player.isValid()
                && !player.isDead()
                && player.getWorld().equals(origin.getWorld())
                && player.getLocation().distanceSquared(origin) <= radiusSq;
    }

    private static NamedLocation returnLocation(Player player) {
        Location loc = player.getLocation();
        return new NamedLocation("", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public void evacuateAll(TemporaryDimensionInstance instance) {
        World world = Bukkit.getWorld(instance.worldName());
        if (world == null) return;
        for (Player player : world.getPlayers()) {
            schedulerBridge.runPlayerTask(player, () -> returnPlayer(player, instance.origin()));
        }
    }

    private void returnPlayer(Player player, NamedLocation origin) {
        if (!player.isOnline()) return;
        World ow = Bukkit.getWorld(origin.world());
        if (ow == null) ow = Bukkit.getWorlds().get(0);
        Location loc = new Location(ow, origin.x(), origin.y(), origin.z(), origin.yaw(), origin.pitch());
        player.teleportAsync(loc);
    }
}
