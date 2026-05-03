package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.ParticipantLifecycle;
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
    private final TemporaryDimensionEvacuation evacuation;

    public TemporaryDimensionTransfer(SchedulerBridge schedulerBridge, TemporaryDimensionDao temporaryDimensionDao,
                                      Logger logger, int transferRadius, TemporaryDimensionWorldFactory worldFactory) {
        this.schedulerBridge = schedulerBridge;
        this.temporaryDimensionDao = temporaryDimensionDao;
        this.logger = logger;
        this.transferRadius = transferRadius;
        this.worldFactory = worldFactory;
        this.evacuation = new TemporaryDimensionEvacuation(schedulerBridge, temporaryDimensionDao, logger);
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
                            player, instanceId, returnLoc, spawn, ok -> {
                                if (!ok) {
                                    logger.warning("Nearby tempdim transfer failed player=" + playerId
                                            + " instance=" + instanceId);
                                }
                            }));
                }
            });
        }
        logger.info("Scheduled transfers for temporary dimension instance " + instanceId);
    }

    private void persistThenTeleport(Player player, String instanceId, NamedLocation returnLoc, Location spawn,
                                     Consumer<Boolean> callback) {
        try {
            temporaryDimensionDao.insertParticipant(
                    instanceId, player.getUniqueId(), ParticipantLifecycle.PENDING_TRANSFER, returnLoc);
        } catch (Exception e) {
            logger.warning("Failed to insert participant: " + e.getMessage());
            callback.accept(false);
            return;
        }
        schedulerBridge.runPlayerTask(player, () -> {
            if (player.isOnline() && player.isValid()) {
                player.teleportAsync(spawn).whenComplete((ok, ex) ->
                        finishTransfer(player, instanceId, returnLoc, ex == null && Boolean.TRUE.equals(ok), callback));
            } else {
                cleanupPending(instanceId, player.getUniqueId(), callback);
            }
        });
    }

    private void finishTransfer(Player player, String instanceId, NamedLocation returnLoc, boolean teleported,
                                Consumer<Boolean> callback) {
        schedulerBridge.runAsyncTask(() -> {
            try {
                if (!teleported) {
                    temporaryDimensionDao.deleteParticipant(instanceId, player.getUniqueId());
                    callback.accept(false);
                    return;
                }
                temporaryDimensionDao.updateParticipantState(
                        instanceId, player.getUniqueId(), ParticipantLifecycle.ACTIVE);
                callback.accept(true);
            } catch (Exception e) {
                logger.warning("Failed to activate tempdim participant: " + e.getMessage());
                try {
                    temporaryDimensionDao.deleteParticipant(instanceId, player.getUniqueId());
                } catch (Exception ignored) {
                }
                schedulerBridge.runPlayerTask(player, () -> player.teleportAsync(toBukkit(returnLoc)));
                callback.accept(false);
            }
        });
    }

    private void cleanupPending(String instanceId, UUID playerId, Consumer<Boolean> callback) {
        schedulerBridge.runAsyncTask(() -> {
            try {
                temporaryDimensionDao.deleteParticipant(instanceId, playerId);
            } catch (Exception e) {
                logger.warning("Failed to cleanup pending tempdim participant: " + e.getMessage());
            }
            callback.accept(false);
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
        evacuation.evacuateAll(instance, callback);
    }

    private Location toBukkit(NamedLocation origin) {
        World ow = Bukkit.getWorld(origin.world());
        if (ow == null) ow = Bukkit.getWorlds().get(0);
        return new Location(ow, origin.x(), origin.y(), origin.z(), origin.yaw(), origin.pitch());
    }

}
