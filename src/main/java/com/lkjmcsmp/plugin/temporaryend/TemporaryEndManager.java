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
            SchedulerBridge schedulerBridge,
            TemporaryEndDao temporaryEndDao,
            PointsDao pointsDao,
            TemporaryEndWorldFactory worldFactory,
            TemporaryEndTransfer transfer,
            Logger logger,
            int cost,
            Duration duration) {
        this.schedulerBridge = schedulerBridge;
        this.temporaryEndDao = temporaryEndDao;
        this.pointsDao = pointsDao;
        this.worldFactory = worldFactory;
        this.transfer = transfer;
        this.logger = logger;
        this.cost = cost;
        this.duration = duration;
    }

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

    public void purchase(Player creator, Location origin) {
        UUID creatorId = creator.getUniqueId();
        schedulerBridge.runAsyncTask(() -> {
            try {
                pointsDao.addPoints(creatorId, -cost, "TEMPORARY_END_PURCHASE",
                        "{\"cost\":" + cost + ",\"world\":\"" + origin.getWorld().getName() + "\"}");
                schedulerBridge.runGlobalTask(() -> createInstance(creator, origin));
                schedulerBridge.runPlayerTask(creator, () -> creator.sendMessage("\u00A7aPurchased temporary End dimension for " + cost + " points."));
            } catch (IllegalArgumentException e) {
                schedulerBridge.runPlayerTask(creator, () -> creator.sendMessage("\u00A7cInsufficient points."));
            } catch (Exception e) {
                logger.warning("Temporary end purchase failed for " + creatorId + ": " + e.getMessage());
                schedulerBridge.runPlayerTask(creator, () -> creator.sendMessage("\u00A7cPurchase failed."));
            }
        });
    }

    private void createInstance(Player creator, Location origin) {
        String instanceId = UUID.randomUUID().toString();
        String worldName = "lkjmcsmp_tempend_" + instanceId.replace("-", "");
        World world = worldFactory.createEndWorld(worldName);
        if (world == null) {
            logger.severe("Failed to create temporary End world: " + worldName);
            schedulerBridge.runPlayerTask(creator, () -> creator.sendMessage("\u00A7cWorld creation failed. Contact an admin."));
            return;
        }
        Instant now = Instant.now();
        Instant expiration = now.plus(duration);
        NamedLocation originLoc = new NamedLocation("", origin.getWorld().getName(),
                origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), origin.getPitch());
        TemporaryEndInstance instance = new TemporaryEndInstance(
                instanceId, worldName, creator.getUniqueId(), originLoc, now, expiration, InstanceLifecycle.ACTIVE);
        try {
            temporaryEndDao.insertInstance(instance);
        } catch (Exception e) {
            logger.severe("Failed to persist temporary end instance: " + e.getMessage());
            worldFactory.unloadAndDelete(worldName);
            return;
        }
        activeInstances.put(instanceId, instance);
        logger.info("Created temporary end instance " + instanceId + " world=" + worldName);
        transfer.captureAndTransfer(origin, world, instanceId);
    }

    public void expireInstance(String instanceId) {
        TemporaryEndInstance instance = activeInstances.remove(instanceId);
        if (instance == null) {
            return;
        }
        try {
            temporaryEndDao.updateState(instanceId, InstanceLifecycle.EXPIRING);
        } catch (Exception e) {
            logger.warning("Failed to update instance state to EXPIRING: " + e.getMessage());
        }
        schedulerBridge.runGlobalTask(() -> {
            transfer.evacuateAll(instance);
            boolean deleted = worldFactory.unloadAndDelete(instance.worldName());
            try {
                temporaryEndDao.updateState(instanceId, InstanceLifecycle.CLOSED);
                if (deleted) {
                    cleanupRecord(instanceId);
                }
                logger.info("Cleaned up temporary end instance " + instanceId);
            } catch (Exception e) {
                logger.warning("Cleanup DB update failed for " + instanceId + ": " + e.getMessage());
            }
        });
    }

    private void cleanupRecord(String instanceId) throws Exception {
        temporaryEndDao.deleteParticipantsByInstance(instanceId);
        temporaryEndDao.deleteInstance(instanceId);
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

    public Collection<TemporaryEndInstance> activeInstances() {
        return activeInstances.values();
    }

    public TemporaryEndInstance findInstance(String instanceId) {
        return activeInstances.get(instanceId);
    }

    public boolean isTemporaryEndWorld(String worldName) {
        return findInstanceByWorld(worldName) != null;
    }

    public TemporaryEndInstance findInstanceByWorld(String worldName) {
        return worldName == null ? null : activeInstances.values().stream()
                .filter(i -> i.worldName().equals(worldName)).findFirst().orElse(null);
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
