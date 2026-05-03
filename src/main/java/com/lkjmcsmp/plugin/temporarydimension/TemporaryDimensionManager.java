package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.ShopEffectExecutor;
import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.ParticipantLifecycle;
import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.domain.model.TemporaryDimensionInstance;
import com.lkjmcsmp.persistence.TemporaryDimensionDao;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class TemporaryDimensionManager implements ShopEffectExecutor {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionDao temporaryDimensionDao;
    private final TemporaryDimensionRefund refund;
    private final TemporaryDimensionActivationCleanup activationCleanup;
    private final TemporaryDimensionRecovery recovery;
    private final TemporaryDimensionWorldFactory worldFactory;
    private final TemporaryDimensionTransfer transfer;
    private final Logger logger;
    private final Duration duration;
    private final Map<String, TemporaryDimensionInstance> activeInstances = new ConcurrentHashMap<>();
    private final Set<UUID> creatingPlayers = ConcurrentHashMap.newKeySet();

    public TemporaryDimensionManager(SchedulerBridge schedulerBridge, TemporaryDimensionDao temporaryDimensionDao,
                                     TemporaryDimensionWorldFactory worldFactory, TemporaryDimensionTransfer transfer,
                                     Logger logger, Duration duration) {
        this.schedulerBridge = schedulerBridge;
        this.temporaryDimensionDao = temporaryDimensionDao;
        this.refund = new TemporaryDimensionRefund(temporaryDimensionDao, logger);
        this.worldFactory = worldFactory;
        this.activationCleanup = new TemporaryDimensionActivationCleanup(schedulerBridge, temporaryDimensionDao, worldFactory, logger);
        this.recovery = new TemporaryDimensionRecovery(schedulerBridge, temporaryDimensionDao, activeInstances, logger);
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
            try { env = World.Environment.valueOf(entry.environment().toUpperCase()); } catch (IllegalArgumentException ignored) { }
        }
        createInstance(player, player.getLocation().clone(), env, callback);
    }

    public void recoverOnStartup() {
        recovery.recoverOnStartup();
    }

    public java.util.Optional<NamedLocation> findParticipantReturn(UUID playerUuid) throws Exception { return temporaryDimensionDao.findParticipantReturn(playerUuid); }

    public Map<ParticipantLifecycle, Integer> countParticipantsByState(String instanceId) throws Exception {
        return temporaryDimensionDao.countParticipantsByState(instanceId);
    }

    public boolean hasActiveInstanceByPlayer(UUID playerUuid) { return activeInstances.values().stream().anyMatch(i -> i.creatorUuid().equals(playerUuid)); }

    public void createInstance(Player creator, Location origin, World.Environment environment, Consumer<ShopEffectExecutor.Result> callback) {
        UUID creatorId = creator.getUniqueId();
        if (hasActiveInstanceByPlayer(creatorId)) {
            var existing = activeInstances.values().stream().filter(i -> i.creatorUuid().equals(creatorId)).findFirst().orElse(null);
            long remaining = existing != null ? Duration.between(Instant.now(), existing.expirationTime()).toMinutes() : 0;
            complete(creator, callback, ShopEffectExecutor.Result.fail("\u00A7cYou already have an active temporary dimension. "
                    + Math.max(0, remaining) + "m remaining. Purchase refunded."));
            return;
        }
        if (!creatingPlayers.add(creatorId)) {
            complete(creator, callback, ShopEffectExecutor.Result.fail("\u00A7cTemporary dimension creation is already in progress. Purchase refunded."));
            return;
        }
        String instanceId = UUID.randomUUID().toString();
        String worldName = "lkjmcsmp_tempdim_" + instanceId.replace("-", "");
        schedulerBridge.runGlobalTask(() -> {
            World world = worldFactory.createWorld(worldName, environment);
            if (world == null) {
                logger.severe("Failed to create temporary dimension world: " + worldName);
                finishCreate(creatorId, creator, callback, ShopEffectExecutor.Result.fail("Creation failed: world creation failed."));
                return;
            }
            Instant now = Instant.now();
            Instant expiration = now.plus(duration);
            NamedLocation originLoc = new NamedLocation("", origin.getWorld().getName(), origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), origin.getPitch());
            TemporaryDimensionInstance instance = new TemporaryDimensionInstance(instanceId, worldName, creatorId,
                    world.getEnvironment(), originLoc, now, expiration, InstanceLifecycle.ACTIVE);
            try {
                temporaryDimensionDao.insertInstance(instance);
            } catch (Exception e) {
                logger.severe("Failed to persist temporary dimension instance: " + e.getMessage());
                worldFactory.unloadAndDelete(worldName);
                finishCreate(creatorId, creator, callback, ShopEffectExecutor.Result.fail("Creation failed: persistence failed."));
                return;
            }
            activeInstances.put(instanceId, instance);
            logger.info("Created temporary dimension instance " + instanceId + " world=" + worldName
                    + " requestedEnv=" + environment + " actualEnv=" + world.getEnvironment());
            transfer.captureAndTransfer(origin, world, instanceId, creator, ok -> {
                if (ok) {
                    finishCreate(creatorId, creator, callback, ShopEffectExecutor.Result.ok("\u00A7aTemporary dimension created ("
                            + world.getEnvironment() + "). Nearby players are being transferred."));
                } else {
                    activationCleanup.cleanup(instanceId, worldName, activeInstances);
                    finishCreate(creatorId, creator, callback, ShopEffectExecutor.Result.fail("Creation failed: transfer failed."));
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
        schedulerBridge.runGlobalTask(() -> transfer.evacuateAll(instance, () -> unloadAndCleanup(instanceId, instance.worldName())));
    }

    public TemporaryDimensionDao.ParticipantReturn findPendingReturn(UUID playerUuid) {
        try {
            var pending = temporaryDimensionDao.findPendingReturns(playerUuid);
            if (!pending.isEmpty()) {
                return pending.get(0);
            }
        } catch (Exception e) {
            logger.warning("Failed to find pending returns: " + e.getMessage());
        }
        return null;
    }

    public void consumePendingReturn(String instanceId, UUID playerUuid) {
        try {
            temporaryDimensionDao.deleteParticipant(instanceId, playerUuid);
            temporaryDimensionDao.deleteClosedInstanceIfNoParticipants(instanceId);
        } catch (Exception e) {
            logger.warning("Failed to consume pending return: " + e.getMessage());
        }
    }

    public TemporaryDimensionInstance findInstanceByWorld(String worldName) {
        return worldName == null ? null : activeInstances.values().stream().filter(i -> i.worldName().equals(worldName)).findFirst().orElse(null);
    }

    public void removeParticipant(String worldName, UUID playerUuid) {
        TemporaryDimensionInstance instance = findInstanceByWorld(worldName);
        if (instance != null) {
            try { temporaryDimensionDao.deleteParticipant(instance.instanceId(), playerUuid); }
            catch (Exception e) { logger.warning("Failed to remove participant: " + e.getMessage()); }
        }
    }

    private void complete(Player player, Consumer<ShopEffectExecutor.Result> callback, ShopEffectExecutor.Result result) { schedulerBridge.runPlayerTask(player, () -> callback.accept(result)); }

    private void finishCreate(UUID creatorId, Player player, Consumer<ShopEffectExecutor.Result> callback, ShopEffectExecutor.Result result) {
        creatingPlayers.remove(creatorId);
        complete(player, callback, result);
    }

    private void unloadAndCleanup(String instanceId, String worldName) {
        boolean deleted = worldFactory.unloadAndDelete(worldName);
        if (deleted) {
            refund.cleanupDb(instanceId, true);
            return;
        }
        schedulerBridge.runGlobalDelayedTask(20L, () -> {
            boolean retry = worldFactory.unloadAndDelete(worldName);
            if (!retry) logger.severe("Permanent cleanup failure for " + instanceId);
            refund.cleanupDb(instanceId, retry);
        });
    }
}
