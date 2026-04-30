package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.TemporaryDimensionDao;
import com.lkjmcsmp.plugin.SchedulerBridge;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public final class TemporaryDimensionBootstrap {
    public static TemporaryDimensionManager bootstrap(
            JavaPlugin plugin,
            SchedulerBridge schedulerBridge,
            PointsDao pointsDao,
            TemporaryDimensionDao temporaryDimensionDao,
            FileConfiguration config,
            ActionBarRouter actionBarRouter) {
        int radius = config.getInt("temporary-dimension.transfer-radius", 5);
        int duration = config.getInt("temporary-dimension.duration-minutes", 180);
        TemporaryDimensionWorldFactory factory = new TemporaryDimensionWorldFactory(plugin.getLogger());
        TemporaryDimensionTransfer transfer = new TemporaryDimensionTransfer(schedulerBridge, temporaryDimensionDao, plugin.getLogger(), radius, factory);
        TemporaryDimensionManager manager = new TemporaryDimensionManager(
                schedulerBridge, temporaryDimensionDao, factory, transfer,
                plugin.getLogger(), Duration.ofMinutes(duration));
        manager.recoverOnStartup();
        new TemporaryDimensionLifecycle(schedulerBridge, manager, plugin.getLogger()).start();
        plugin.getServer().getPluginManager().registerEvents(
                new TemporaryDimensionListener(manager, schedulerBridge, plugin.getLogger(), actionBarRouter), plugin);
        return manager;
    }
}
