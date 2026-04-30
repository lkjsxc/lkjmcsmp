package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.ShopEffectExecutor;
import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.domain.model.TemporaryDimensionInstance;
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
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class TemporaryDimensionManager implements ShopEffectExecutor {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionDao temporaryDimensionDao;
    private final TemporaryDimensionRefund refund;
    private final TemporaryDimensionActivationCleanup activationCleanup;
    private final TemporaryDimensionWorldFactory worldFactory;
    private final TemporaryDimensionTransfer transfer;
    private final Logger logger;
    private final Duration duration;
    private final Map<String, TemporaryDimensionInstance> activeInstances = new ConcurrentHashMap<>();

    public TemporaryDimensionManager(
            SchedulerBridge schedulerBridge, TemporaryDimensionDao temporaryDimensionDao,
            TemporaryDimensionWorldFactory worldFactory, TemporaryDimensionTransfer transfer,
            Logger logger, Duration duration) {
        this.schedulerBridge = schedulerBridge;
        this.temporaryDimensionDao = temporaryDimensionDao;
        this.refund = new TemporaryDimensionRefund(temporaryDimensionDao, logger);
        this.worldFactory = worldFactory;
        this.activationCleanup = new TemporaryDimensionActivationCleanup(
                schedulerBridge, temporaryDimensionDao, worldFactory, logger);
        this.transfer = transfer;
        this.logger = logger;
        this.duration = duration;
    }

    public Collection<TemporaryDimensionInstance> activeInstances() { return activeInstances.values(); }
    public TemporaryDimensionInstance findInstance(String instanceId) { return activeInstances.get(instanceId); }
    public boolean isTemporaryDimensionWorld(String worldName) { return findInstanceByWorld(worldName) != null; }
    @Override
    public void execute(Player player, ShopEntry entry, int deductedPoints, Consumer<ShopEffectExecutor.Result> callback) {
        World.Environment env = World.Environment.THE_END;
        if (entry != null && !entry.environment().isBlank()) {
            try { env = World.Environment.valueOf(entry.environment().toUpperCase()); }
            catch (IllegalArgumentException ignored) { }
        }
        createInstance(player, player.getLocation().clone(), env, callback);
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

    public void createInstance(Player creator, Location origin, World.Environment environment,
                               Consumer<ShopEffectExecutor.Result> callback) {
        UUID creatorId = creator.getUniqueId();
        if (hasActiveInstanceByPlayer(creatorId)) {
            var existing = activeInstances.values().stream().filter(i -> i.creatorUuid().equals(creatorId)).findFirst().orElse(null);
            long remaining = existing != null ? Duration.between(Instant.now(), existing.expirationTime()).toMinutes() : 0;
            complete(creator, callback, ShopEffectExecutor.Result.fail(
                    "\u00A7cYou already have an active temporary dimension. "
                            + Math.max(0, remaining) + "m remaining. Purchase refunded."));
            return;
        }
        String instanceId = UUID.randomUUID().toString();
        String worldName = "lkjmcsmp_tempdim_" + instanceId.replace("-", "");
        schedulerBridge.runGlobalTask(() -> {
            World world = worldFactory.createWorld(worldName, environment);
            if (world == null) {
                logger.severe("Failed to create temporary dimension world: " + worldName);
                complete(creator, callback, ShopEffectExecutor.Result.fail("Creation failed: world creation failed."));
                return;
            }
            Instant now = Instant.now();
            Instant expiration = now.plus(duration);
            NamedLocation originLoc = new NamedLocation("", origin.getWorld().getName(), origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), origin.getPitch());
            TemporaryDimensionInstance instance = new TemporaryDimensionInstance(
                    instanceId, worldName, creatorId, world.getEnvironment(), originLoc, now, expiration, InstanceLifecycle.ACTIVE);
            try {
                temporaryDimensionDao.insertInstance(instance);
            } catch (Exception e) {
                logger.severe("Failed to persist temporary dimension instance: " + e.getMessage());
                worldFactory.unloadAndDelete(worldName);
                complete(creator, callback, ShopEffectExecutor.Result.fail("Creation failed: persistence failed."));
                return;
            }
            activeInstances.put(instanceId, instance);
            logger.info("Created temporary dimension instance " + instanceId + " world=" + worldName + " env=" + environment);
            transfer.captureAndTransfer(origin, world, instanceId, creator, ok -> {
                if (ok) {
                    complete(creator, callback, ShopEffectExecutor.Result.ok(
                            "\u00A7aTemporary dimension created. Nearby players are being transferred."));
                } else {
                    activationCleanup.cleanup(instanceId, worldName, activeInstances);
                    complete(creator, callback, ShopEffectExecutor.Result.fail("Creation failed: transfer failed."));
                }
            });
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
                    refund.cleanupDb(instanceId, retry);
                });
            } else {
                refund.cleanupDb(instanceId, true);
            }
        });
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

    private void complete(Player player, Consumer<ShopEffectExecutor.Result> callback, ShopEffectExecutor.Result result) {
        schedulerBridge.runPlayerTask(player, () -> callback.accept(result));
    }
}
