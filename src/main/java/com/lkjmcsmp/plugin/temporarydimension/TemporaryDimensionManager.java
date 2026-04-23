package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.ShopEffectExecutor;
import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.domain.model.TemporaryDimensionInstance;
import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.TemporaryDimensionDao;
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
public final class TemporaryDimensionManager implements ShopEffectExecutor {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionDao temporaryDimensionDao;
    private final PointsDao pointsDao;
    private final TemporaryDimensionWorldFactory worldFactory;
    private final TemporaryDimensionTransfer transfer;
    private final Logger logger;
    private final int cost;
    private final Duration duration;
    private final Map<String, TemporaryDimensionInstance> activeInstances = new ConcurrentHashMap<>();
    public TemporaryDimensionManager(
            SchedulerBridge schedulerBridge, TemporaryDimensionDao temporaryDimensionDao, PointsDao pointsDao,
            TemporaryDimensionWorldFactory worldFactory, TemporaryDimensionTransfer transfer,
            Logger logger, int cost, Duration duration) {
        this.schedulerBridge = schedulerBridge;
        this.temporaryDimensionDao = temporaryDimensionDao;
        this.pointsDao = pointsDao;
        this.worldFactory = worldFactory;
        this.transfer = transfer;
        this.logger = logger;
        this.cost = cost;
        this.duration = duration;
    }
    public int cost() { return cost; }
    public Collection<TemporaryDimensionInstance> activeInstances() { return activeInstances.values(); }
    public TemporaryDimensionInstance findInstance(String instanceId) { return activeInstances.get(instanceId); }
    public boolean isTemporaryDimensionWorld(String worldName) { return findInstanceByWorld(worldName) != null; }
    @Override
    public void execute(Player player, ShopEntry entry) {
        World.Environment env = World.Environment.THE_END;
        if (entry != null && !entry.environment().isBlank()) {
            try {
                env = World.Environment.valueOf(entry.environment().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        createInstance(player, player.getLocation(), env);
    }
    public void recoverOnStartup() {
        schedulerBridge.runAsyncTask(() -> {
            try {
                List<TemporaryDimensionInstance> active = temporaryDimensionDao.listByState(InstanceLifecycle.ACTIVE);
                schedulerBridge.runGlobalTask(() -> {
                    for (TemporaryDimensionInstance instance : active) {
                        if (Bukkit.getWorld(instance.worldName()) != null) {
                            activeInstances.put(instance.instanceId(), instance);
                            logger.info("Recovered temporary dimension instance: " + instance.instanceId());
                        } else {
                            try {
                                temporaryDimensionDao.deleteParticipantsByInstance(instance.instanceId());
                                temporaryDimensionDao.deleteInstance(instance.instanceId());
                                logger.info("Cleaned up orphaned temporary dimension record: " + instance.instanceId());
                            } catch (Exception e) {
                                logger.warning("Cleanup failed for orphaned record: " + e.getMessage());
                            }
                        }
                    }
                });
            } catch (Exception e) {
                logger.warning("Temporary dimension startup recovery failed: " + e.getMessage());
            }
        });
    }
    public java.util.Optional<NamedLocation> findParticipantReturn(UUID playerUuid) throws Exception {
        return temporaryDimensionDao.findParticipantReturn(playerUuid);
    }
    public boolean hasActiveInstanceByPlayer(UUID playerUuid) {
        return activeInstances.values().stream().anyMatch(i -> i.creatorUuid().equals(playerUuid));
    }
    public void createInstance(Player creator, Location origin, World.Environment environment) {
        UUID creatorId = creator.getUniqueId();
        if (hasActiveInstanceByPlayer(creatorId)) {
            var existing = activeInstances.values().stream().filter(i -> i.creatorUuid().equals(creatorId)).findFirst().orElse(null);
            long remaining = existing != null ? Duration.between(Instant.now(), existing.expirationTime()).toMinutes() : 0;
            try {
                pointsDao.addPoints(creatorId, cost, "TEMPORARY_DIMENSION_REFUND", "{\"reason\":\"duplicate_instance\"}");
            } catch (Exception ex) {
                logger.severe("Duplicate-instance refund failed: " + ex.getMessage());
            }
            schedulerBridge.runPlayerTask(creator, () -> creator.sendMessage("\u00A7cYou already have an active temporary dimension. " + Math.max(0, remaining) + "m remaining. \u00A7a" + cost + " Cobblestone Points refunded."));
            return;
        }
        String instanceId = UUID.randomUUID().toString();
        String worldName = "lkjmcsmp_tempdim_" + instanceId.replace("-", "");
        schedulerBridge.runGlobalTask(() -> {
            World world;
            try {
                world = worldFactory.createWorld(worldName, environment);
            } catch (Exception ex) {
                logger.severe("Exception creating temporary dimension world: " + worldName + " — " + ex.getMessage());
                refundAndNotify(creator, "world_creation_failed");
                return;
            }
            if (world == null) {
                logger.severe("Failed to create temporary dimension world: " + worldName);
                refundAndNotify(creator, "world_creation_failed");
                return;
            }
            Instant now = Instant.now();
            Instant expiration = now.plus(duration);
            NamedLocation originLoc = new NamedLocation("", origin.getWorld().getName(), origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), origin.getPitch());
            TemporaryDimensionInstance instance = new TemporaryDimensionInstance(instanceId, worldName, creatorId, environment, originLoc, now, expiration, InstanceLifecycle.ACTIVE);
            try {
                temporaryDimensionDao.insertInstance(instance);
            } catch (Exception e) {
                logger.severe("Failed to persist temporary dimension instance: " + e.getMessage());
                worldFactory.unloadAndDelete(worldName);
                refundAndNotify(creator, "db_persist_failed");
                return;
            }
            activeInstances.put(instanceId, instance);
            logger.info("Created temporary dimension instance " + instanceId + " world=" + worldName + " env=" + environment);
            transfer.captureAndTransfer(origin, world, instanceId);
        });
    }
    public void expireInstance(String instanceId) {
        TemporaryDimensionInstance instance = activeInstances.remove(instanceId);
        if (instance == null) return;
        try {
            temporaryDimensionDao.updateState(instanceId, InstanceLifecycle.EXPIRING);
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
            temporaryDimensionDao.updateState(instanceId, InstanceLifecycle.CLOSED);
            if (deleted) {
                temporaryDimensionDao.deleteParticipantsByInstance(instanceId);
                temporaryDimensionDao.deleteInstance(instanceId);
            }
            logger.info("Cleaned up temporary dimension instance " + instanceId);
        } catch (Exception e) {
            logger.warning("Cleanup DB update failed for " + instanceId + ": " + e.getMessage());
        }
    }
    private void refundAndNotify(Player player, String reason) {
        try {
            pointsDao.addPoints(player.getUniqueId(), cost, "TEMPORARY_DIMENSION_REFUND", "{\"reason\":\"" + reason + "\"}");
            schedulerBridge.runPlayerTask(player, () -> player.sendMessage("\u00A7cCreation failed. \u00A7a" + cost + " Cobblestone Points refunded."));
        } catch (Exception ex) {
            logger.severe("Refund failed for " + player.getUniqueId() + ": " + ex.getMessage());
            schedulerBridge.runPlayerTask(player, () -> player.sendMessage("\u00A7cCreation failed and refund could not be applied. Contact an admin."));
        }
    }
    public NamedLocation pollPendingReturns(UUID playerUuid) {
        try {
            var pending = temporaryDimensionDao.findPendingReturns(playerUuid);
            if (!pending.isEmpty()) {
                var first = pending.get(0);
                temporaryDimensionDao.deleteParticipant(first.instanceId(), playerUuid);
                return first.location();
            }
        } catch (Exception e) {
            logger.warning("Failed to poll pending returns: " + e.getMessage());
        }
        return null;
    }
    public TemporaryDimensionInstance findInstanceByWorld(String worldName) {
        return worldName == null ? null : activeInstances.values().stream().filter(i -> i.worldName().equals(worldName)).findFirst().orElse(null);
    }
    public void removeParticipant(String worldName, UUID playerUuid) {
        TemporaryDimensionInstance instance = findInstanceByWorld(worldName);
        if (instance != null) {
            try {
                temporaryDimensionDao.deleteParticipant(instance.instanceId(), playerUuid);
            } catch (Exception e) {
                logger.warning("Failed to remove participant: " + e.getMessage());
            }
        }
    }
}