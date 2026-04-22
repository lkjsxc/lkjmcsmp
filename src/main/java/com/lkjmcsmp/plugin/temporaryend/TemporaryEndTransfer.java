package com.lkjmcsmp.plugin.temporaryend;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.TemporaryEndInstance;
import com.lkjmcsmp.persistence.TemporaryEndDao;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public final class TemporaryEndTransfer {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryEndDao temporaryEndDao;
    private final Logger logger;
    private final int transferRadius;

    public TemporaryEndTransfer(SchedulerBridge schedulerBridge, TemporaryEndDao temporaryEndDao,
                                Logger logger, int transferRadius) {
        this.schedulerBridge = schedulerBridge;
        this.temporaryEndDao = temporaryEndDao;
        this.logger = logger;
        this.transferRadius = transferRadius;
    }

    public void captureAndTransfer(Location origin, World world, String instanceId) {
        double radiusSq = transferRadius * (double) transferRadius;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(origin.getWorld())) {
                continue;
            }
            if (player.getLocation().distanceSquared(origin) > radiusSq) {
                continue;
            }
            if (!player.isValid() || !player.isOnline()) {
                continue;
            }
            NamedLocation returnLoc = new NamedLocation("", origin.getWorld().getName(),
                    origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), origin.getPitch());
            try {
                temporaryEndDao.insertParticipant(instanceId, player.getUniqueId(), returnLoc);
            } catch (Exception e) {
                logger.warning("Failed to insert participant: " + e.getMessage());
            }
            Location spawn = world.getSpawnLocation().clone();
            schedulerBridge.runPlayerTask(player, () -> {
                if (player.isOnline()) {
                    player.teleportAsync(spawn);
                }
            });
        }
        logger.info("Transferred nearby players into temporary end instance " + instanceId);
    }

    public void evacuateAll(TemporaryEndInstance instance) {
        World world = Bukkit.getWorld(instance.worldName());
        if (world == null) {
            return;
        }
        for (Player player : world.getPlayers()) {
            schedulerBridge.runPlayerTask(player, () -> returnPlayer(player, instance.origin()));
        }
    }

    private void returnPlayer(Player player, NamedLocation origin) {
        if (!player.isOnline()) {
            return;
        }
        World ow = Bukkit.getWorld(origin.world());
        if (ow == null) {
            ow = Bukkit.getWorlds().get(0);
        }
        Location loc = new Location(ow, origin.x(), origin.y(), origin.z(), origin.yaw(), origin.pitch());
        player.teleportAsync(loc);
    }
}
