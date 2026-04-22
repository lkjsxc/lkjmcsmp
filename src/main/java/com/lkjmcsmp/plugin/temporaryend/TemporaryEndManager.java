package com.lkjmcsmp.plugin.temporaryend;

import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.TemporaryEndInstance;
import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.TemporaryEndDao;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class TemporaryEndManager {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryEndDao temporaryEndDao;
    private final PointsDao pointsDao;
    private final TemporaryEndWorldFactory worldFactory;
    private final TemporaryEndTransfer transfer;
    private final Logger logger;
    private final int cost;
    private final Duration duration;
    private final Map<String, TemporaryEndInstance> activeInstances = new ConcurrentHashMap<>();

    public TemporaryEndManager(
            SchedulerBridge schedulerBridge, TemporaryEndDao temporaryEndDao, PointsDao pointsDao,
            TemporaryEndWorldFactory worldFactory, TemporaryEndTransfer transfer,
            Logger logger, int cost, Duration duration) {
        this.schedulerBridge = schedulerBridge;
        this.temporaryEndDao = temporaryEndDao;
        this.pointsDao = pointsDao;
        this.worldFactory = worldFactory;
        this.transfer = transfer;
        this.logger = logger;
        this.cost = cost;
        this.duration = duration;
    }

    public int cost() { return cost; }

    public void recoverOnStartup() {
        schedulerBridge.runAsyncTask(() -> {
            try {
                List<TemporaryEndInstance> active = temporaryEndDao.listByState(InstanceLifecycle.ACTIVE);
                schedulerBridge.runGlobalTask(() -> {
                    for (TemporaryEndInstance instance : active) {
                        if (Bukkit.getWorld(instance.worldName()) != null) {
                            activeInstances.put(instance.instanceId(), instance);
                            logger.info("Recovered temporary end instance: " + instance.instanceId());
                        } else {
                            try {
                                cleanupRecord(instance.instanceId());
                                logger.info("Cleaned up orphaned temporary end record: " + instance.instanceId());
                            } catch (Exception e) {
                                logger.warning("Cleanup failed for orphaned record: " + e.getMessage());
                            }
                        }
                    }
                });
            } catch (Exception e) {
                logger.warning("Temporary end startup recovery failed: " + e.getMessage());
            }
        });
    }

    public boolean hasActiveInstanceByPlayer(UUID playerUuid) {
        return activeInstances.values().stream().anyMatch(i -> i.creatorUuid().equals(playerUuid));
    }

    public void createInstance(Player creator, Location origin) {
        UUID creatorId = creator.getUniqueId();
        if (hasActiveInstanceByPlayer(creatorId)) {
            var existing = activeInstances.values().stream().filter(i -> i.creatorUuid().equals(creatorId)).findFirst().orElse(null);
            long remaining = existing != null ? Duration.between(Instant.now(), existing.expirationTime()).toMinutes() : 0;
            schedulerBridge.runPlayerTask(creator, () -> creator.sendMessage("\u00A7cYou already have an active temporary End. " + Math.max(0, remaining) + "m remaining."));
            return;
        }
        String instanceId = UUID.randomUUID().toString();
        String worldName = "lkjmcsmp_tempend_" + instanceId.replace("-", "");
        World world = worldFactory.createEndWorld(worldName);
        if (world == null) {
            logger.severe("Failed to create temporary End world: " + worldName);
            refundAndNotify(creator, "world_creation_failed");
            return;
        }
        Instant now = Instant.now();
        Instant expiration = now.plus(duration);
        NamedLocation originLoc = new NamedLocation("", origin.getWorld().getName(), origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), origin.getPitch());
        TemporaryEndInstance instance = new TemporaryEndInstance(instanceId, worldName, creatorId, originLoc, now, expiration, InstanceLifecycle.ACTIVE);
        try {
            temporaryEndDao.insertInstance(instance);
        } catch (Exception e) {
            logger.severe("Failed to persist temporary end instance: " + e.getMessage());
            worldFactory.unloadAndDelete(worldName);
            refundAndNotify(creator, "db_persist_failed");
            return;
        }
        activeInstances.put(instanceId, instance);
        logger.info("Created temporary end instance " + instanceId + " world=" + worldName);
        transfer.captureAndTransfer(origin, world, instanceId);
    }

    public void expireInstance(String instanceId) {
        TemporaryEndInstance instance = activeInstances.remove(instanceId);
        if (instance == null) return;
        try {
            temporaryEndDao.updateState(instanceId, InstanceLifecycle.EXPIRING);
        } catch (Exception e) {
            logger.warning("Failed to update instance state to EXPIRING: " + e.getMessage());
        }
        schedulerBridge.runGlobalTask(() -> {
            transfer.evacuateAll(instance);
            boolean deleted = worldFactory.unloadAndDelete(instance.worldName());
            if (!deleted) {
                schedulerBridge.runGlobalDelayedTask(20L, () -> {
                    boolean retry = worldFactory.unloadAndDelete(instance.worldName());
                    if (!retry) logger.severe("Permanent cleanup failure for " + instance.instanceId());
                    cleanupDb(instanceId, retry);
                });
            } else {
                cleanupDb(instanceId, true);
            }
        });
    }

    private void cleanupDb(String instanceId, boolean deleted) {
        try {
            temporaryEndDao.updateState(instanceId, InstanceLifecycle.CLOSED);
            if (deleted) cleanupRecord(instanceId);
            logger.info("Cleaned up temporary end instance " + instanceId);
        } catch (Exception e) {
            logger.warning("Cleanup DB update failed for " + instanceId + ": " + e.getMessage());
        }
    }

    private void cleanupRecord(String instanceId) throws Exception {
        temporaryEndDao.deleteParticipantsByInstance(instanceId);
        temporaryEndDao.deleteInstance(instanceId);
    }

    private void refundAndNotify(Player player, String reason) {
        try {
            pointsDao.addPoints(player.getUniqueId(), cost, "TEMPORARY_END_REFUND", "{\"reason\":\"" + reason + "\"}");
            schedulerBridge.runPlayerTask(player, () -> player.sendMessage("\u00A7cCreation failed. \u00A7a" + cost + " points refunded."));
        } catch (Exception ex) {
            logger.severe("Refund failed for " + player.getUniqueId() + ": " + ex.getMessage());
            schedulerBridge.runPlayerTask(player, () -> player.sendMessage("\u00A7cCreation failed and refund could not be applied. Contact an admin."));
        }
    }

    public NamedLocation pollPendingReturns(UUID playerUuid) {
        try {
            var pending = temporaryEndDao.findPendingReturns(playerUuid);
            if (!pending.isEmpty()) {
                var first = pending.get(0);
                temporaryEndDao.deleteParticipant(first.instanceId(), playerUuid);
                return first.location();
            }
        } catch (Exception e) {
            logger.warning("Failed to poll pending returns: " + e.getMessage());
        }
        return null;
    }

    public Collection<TemporaryEndInstance> activeInstances() { return activeInstances.values(); }
    public TemporaryEndInstance findInstance(String instanceId) { return activeInstances.get(instanceId); }
    public boolean isTemporaryEndWorld(String worldName) { return findInstanceByWorld(worldName) != null; }

    public TemporaryEndInstance findInstanceByWorld(String worldName) {
        return worldName == null ? null : activeInstances.values().stream().filter(i -> i.worldName().equals(worldName)).findFirst().orElse(null);
    }

    public void removeParticipant(String worldName, UUID playerUuid) {
        TemporaryEndInstance instance = findInstanceByWorld(worldName);
        if (instance != null) {
            try {
                temporaryEndDao.deleteParticipant(instance.instanceId(), playerUuid);
            } catch (Exception e) {
                logger.warning("Failed to remove participant: " + e.getMessage());
            }
        }
    }
}
