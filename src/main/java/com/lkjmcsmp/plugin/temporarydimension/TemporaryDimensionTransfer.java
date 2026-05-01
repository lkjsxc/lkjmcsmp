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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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

    public void evacuateAll(TemporaryDimensionInstance instance, Runnable callback) {
        schedulerBridge.runAsyncTask(() -> {
            Map<UUID, NamedLocation> tracked = new HashMap<>();
            try {
                for (var participant : temporaryDimensionDao.listParticipants(instance.instanceId())) {
                    tracked.put(participant.playerUuid(), participant.location());
                }
            } catch (Exception e) {
                logger.warning("Failed to load temporary dimension participants: " + e.getMessage());
            }
            schedulerBridge.runGlobalTask(() -> scheduleEvacuation(instance, tracked, callback));
        });
    }

    private void scheduleEvacuation(TemporaryDimensionInstance instance, Map<UUID, NamedLocation> tracked,
                                    Runnable callback) {
        World world = Bukkit.getWorld(instance.worldName());
        if (world == null) {
            callback.run();
            return;
        }
        List<Player> occupants = new ArrayList<>(world.getPlayers());
        if (occupants.isEmpty()) {
            callback.run();
            return;
        }
        AtomicInteger remaining = new AtomicInteger(occupants.size());
        for (Player player : occupants) {
            NamedLocation origin = tracked.get(player.getUniqueId());
            schedulerBridge.runPlayerTask(player, () -> returnPlayer(player, instance.instanceId(), origin, () -> {
                if (remaining.decrementAndGet() == 0) {
                    schedulerBridge.runGlobalTask(callback);
                }
            }));
        }
    }

    private void returnPlayer(Player player, String instanceId, NamedLocation origin, Runnable callback) {
        if (!player.isOnline()) {
            callback.run();
            return;
        }
        Location loc = origin == null ? Bukkit.getWorlds().get(0).getSpawnLocation() : toBukkit(origin);
        player.teleportAsync(loc).whenComplete((ok, ex) -> {
            if (origin != null && ex == null && Boolean.TRUE.equals(ok)) {
                schedulerBridge.runAsyncTask(() -> deleteParticipant(instanceId, player.getUniqueId()));
            }
            callback.run();
        });
    }

    private Location toBukkit(NamedLocation origin) {
        World ow = Bukkit.getWorld(origin.world());
        if (ow == null) ow = Bukkit.getWorlds().get(0);
        return new Location(ow, origin.x(), origin.y(), origin.z(), origin.yaw(), origin.pitch());
    }

    private void deleteParticipant(String instanceId, UUID playerId) {
        try {
            temporaryDimensionDao.deleteParticipant(instanceId, playerId);
            temporaryDimensionDao.deleteClosedInstanceIfNoParticipants(instanceId);
        } catch (Exception e) {
            logger.warning("Failed to delete returned participant: " + e.getMessage());
        }
    }
}
