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

    public void captureAndTransfer(Location origin, World world, String instanceId) {
        double radiusSq = transferRadius * (double) transferRadius;
        Location spawn = worldFactory.resolveSpawnLocation(world);
        List<UUID> candidateIds = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            candidateIds.add(player.getUniqueId());
        }
        for (UUID playerId : candidateIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) continue;
            schedulerBridge.runPlayerTask(player, () -> {
                if (!player.isOnline() || !player.isValid()) return;
                if (!player.getWorld().equals(origin.getWorld())) return;
                if (player.getLocation().distanceSquared(origin) > radiusSq) return;
                Location playerLoc = player.getLocation();
                NamedLocation returnLoc = new NamedLocation("", playerLoc.getWorld().getName(),
                        playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(), playerLoc.getYaw(), playerLoc.getPitch());
                try {
                    temporaryDimensionDao.insertParticipant(instanceId, player.getUniqueId(), returnLoc);
                } catch (Exception e) {
                    logger.warning("Failed to insert participant: " + e.getMessage());
                }
                player.teleportAsync(spawn);
            });
        }
        logger.info("Scheduled transfers for temporary dimension instance " + instanceId);
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
