package com.lkjmcsmp.plugin.temporaryend;

import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.TemporaryEndDao;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public final class TemporaryEndBootstrap {
    public static TemporaryEndManager bootstrap(
            JavaPlugin plugin,
            SchedulerBridge schedulerBridge,
            PointsDao pointsDao,
            TemporaryEndDao temporaryEndDao,
            FileConfiguration config) {
        int cost = config.getInt("temporary-end.cost", 10000);
        int radius = config.getInt("temporary-end.transfer-radius", 10);
        int duration = config.getInt("temporary-end.duration-minutes", 180);
        TemporaryEndWorldFactory factory = new TemporaryEndWorldFactory(plugin.getLogger());
        TemporaryEndTransfer transfer = new TemporaryEndTransfer(schedulerBridge, temporaryEndDao, plugin.getLogger(), radius);
        TemporaryEndManager manager = new TemporaryEndManager(
                schedulerBridge, temporaryEndDao, pointsDao, factory, transfer,
                plugin.getLogger(), cost, Duration.ofMinutes(duration));
        manager.recoverOnStartup();
        new TemporaryEndLifecycle(schedulerBridge, manager, plugin.getLogger()).start();
        plugin.getServer().getPluginManager().registerEvents(
                new TemporaryEndListener(manager, schedulerBridge, plugin.getLogger()), plugin);
        return manager;
    }
}
