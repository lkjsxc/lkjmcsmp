package com.lkjmcsmp.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class FoliaSchedulerBridge implements SchedulerBridge {
    private final JavaPlugin plugin;

    public FoliaSchedulerBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runPlayerTask(Player player, Runnable runnable) {
        player.getScheduler().run(plugin, task -> runnable.run(), null);
    }

    @Override
    public void runRegionTask(Location location, Runnable runnable) {
        plugin.getServer().getRegionScheduler().run(
                plugin,
                location,
                task -> runnable.run());
    }

    @Override
    public void runAsyncTask(Runnable runnable) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> runnable.run());
    }
}
