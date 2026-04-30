package com.lkjmcsmp.plugin;

import com.lkjmcsmp.gui.HotbarMenuListener;
import com.lkjmcsmp.gui.HotbarMenuService;
import com.lkjmcsmp.gui.MenuListener;
import com.lkjmcsmp.persistence.FirstJoinDao;
import com.lkjmcsmp.plugin.hud.ActionBarHudListener;
import com.lkjmcsmp.plugin.temporarydimension.TemporaryDimensionManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

final class ListenerRegistry {
    private ListenerRegistry() {
    }

    static void registerAll(JavaPlugin plugin, Services services, FirstJoinDao firstJoinDao,
                            SchedulerBridge schedulerBridge, TemporaryDimensionManager temporaryDimensionManager) {
        Logger logger = plugin.getLogger();
        FileConfiguration config = plugin.getConfig();

        plugin.getServer().getPluginManager().registerEvents(new MenuListener(services.menus()), plugin);
        HotbarMenuService hotbarMenuService = new HotbarMenuService(plugin, services.menus());
        plugin.getServer().getPluginManager().registerEvents(new HotbarMenuListener(hotbarMenuService), plugin);
        for (var online : plugin.getServer().getOnlinePlayers()) {
            hotbarMenuService.install(online);
        }
        plugin.getServer().getPluginManager().registerEvents(new ActionBarHudListener(services.hud()), plugin);
        services.hud().start();
        services.hud().refreshIdleAllOnline();
        plugin.getServer().getPluginManager().registerEvents(new TeleportCommandOverrideListener(logger), plugin);
        if (config.getBoolean("teleport.first-join.enabled", true)) {
            String firstJoinWorld = Objects.requireNonNull(config.getString("teleport.first-join.world", ""));
            plugin.getServer().getPluginManager().registerEvents(
                    new FirstJoinTeleportListener(services.teleports(), firstJoinDao, schedulerBridge, firstJoinWorld, logger),
                    plugin);
        }
        if (config.getBoolean("respawn-on-death.random-teleport.enabled", true)) {
            plugin.getServer().getPluginManager().registerEvents(
                    new RespawnRtpListener(services.teleports(), schedulerBridge, temporaryDimensionManager, logger), plugin);
        }
    }
}
