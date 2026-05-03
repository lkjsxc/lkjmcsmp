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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

final class TemporaryDimensionEvacuation {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionDao dao;
    private final Logger logger;

    TemporaryDimensionEvacuation(SchedulerBridge schedulerBridge, TemporaryDimensionDao dao, Logger logger) {
        this.schedulerBridge = schedulerBridge;
        this.dao = dao;
        this.logger = logger;
    }

    void evacuateAll(TemporaryDimensionInstance instance, Runnable callback) {
        schedulerBridge.runAsyncTask(() -> {
            Map<UUID, NamedLocation> tracked = loadTracked(instance.instanceId());
            schedulerBridge.runGlobalTask(() -> schedule(instance, tracked, callback));
        });
    }

    private Map<UUID, NamedLocation> loadTracked(String instanceId) {
        Map<UUID, NamedLocation> tracked = new HashMap<>();
        try {
            for (var participant : dao.listParticipants(instanceId, ParticipantLifecycle.ACTIVE)) {
                tracked.put(participant.playerUuid(), participant.location());
            }
        } catch (Exception e) {
            logger.warning("Failed to load temporary dimension participants: " + e.getMessage());
        }
        return tracked;
    }

    private void schedule(TemporaryDimensionInstance instance, Map<UUID, NamedLocation> tracked, Runnable callback) {
        World world = Bukkit.getWorld(instance.worldName());
        if (world == null || world.getPlayers().isEmpty()) {
            markRemainingReturnPending(instance.instanceId(), callback);
            return;
        }
        var occupants = new ArrayList<>(world.getPlayers());
        AtomicInteger remaining = new AtomicInteger(occupants.size());
        for (Player player : occupants) {
            schedulerBridge.runPlayerTask(player, () -> returnPlayer(
                    player, instance.instanceId(), tracked.get(player.getUniqueId()), () -> {
                        if (remaining.decrementAndGet() == 0) {
                            markRemainingReturnPending(instance.instanceId(),
                                    () -> schedulerBridge.runGlobalTask(callback));
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
            } else if (origin != null) {
                markParticipantReturnPending(instanceId, player.getUniqueId());
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
            dao.deleteParticipant(instanceId, playerId);
            dao.deleteClosedInstanceIfNoParticipants(instanceId);
        } catch (Exception e) {
            logger.warning("Failed to delete returned participant: " + e.getMessage());
        }
    }

    private void markParticipantReturnPending(String instanceId, UUID playerId) {
        schedulerBridge.runAsyncTask(() -> {
            try {
                dao.updateParticipantState(instanceId, playerId, ParticipantLifecycle.RETURN_PENDING);
            } catch (Exception e) {
                logger.warning("Failed to mark participant return pending: " + e.getMessage());
            }
        });
    }

    private void markRemainingReturnPending(String instanceId, Runnable callback) {
        schedulerBridge.runAsyncTask(() -> {
            try {
                dao.markActiveParticipantsReturnPending(instanceId);
            } catch (Exception e) {
                logger.warning("Failed to mark remaining returns pending: " + e.getMessage());
            }
            callback.run();
        });
    }
}
