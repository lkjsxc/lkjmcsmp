package com.lkjmcsmp.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface SchedulerBridge {
    void runPlayerTask(Player player, Runnable runnable);

    void runRegionTask(Location location, Runnable runnable);

    void runAsyncTask(Runnable runnable);
}
