package com.lkjmcsmp.plugin;

import com.lkjmcsmp.command.AchievementCommand;
import com.lkjmcsmp.command.HomeCommand;
import com.lkjmcsmp.command.MenuCommand;
import com.lkjmcsmp.command.PointsCommand;
import com.lkjmcsmp.command.TeamCommand;
import com.lkjmcsmp.command.TeleportCommand;
import com.lkjmcsmp.plugin.temporarydimension.TemporaryDimensionCommand;
import com.lkjmcsmp.command.WarpCommand;
import com.lkjmcsmp.plugin.temporarydimension.TemporaryDimensionManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

final class CommandRegistry {
    private CommandRegistry() {
    }

    static void registerAll(JavaPlugin plugin, Services services, TemporaryDimensionManager temporaryDimensionManager) {
        register(plugin, "menu", new MenuCommand(services.menus()));
        register(plugin, "points", new PointsCommand(services.points(), services.menus(), services.achievement(), services.hud()));
        register(plugin, "convert", new PointsCommand(services.points(), services.menus(), services.achievement(), services.hud()));
        register(plugin, "shop", new PointsCommand(services.points(), services.menus(), services.achievement(), services.hud()));
        register(plugin, "home", new HomeCommand(services.homes(), services.teleports(), services.achievement()));
        register(plugin, "sethome", new HomeCommand(services.homes(), services.teleports(), services.achievement()));
        register(plugin, "delhome", new HomeCommand(services.homes(), services.teleports(), services.achievement()));
        register(plugin, "homes", new HomeCommand(services.homes(), services.teleports(), services.achievement()));
        register(plugin, "warp", new WarpCommand(services.warps(), services.teleports(), services.achievement()));
        register(plugin, "setwarp", new WarpCommand(services.warps(), services.teleports(), services.achievement()));
        register(plugin, "delwarp", new WarpCommand(services.warps(), services.teleports(), services.achievement()));
        register(plugin, "warps", new WarpCommand(services.warps(), services.teleports(), services.achievement()));
        register(plugin, "team", new TeamCommand(services.parties(), services.teleports(), services.achievement()));
        register(plugin, "tp", new TeleportCommand(services.teleports(), services.menus(), services.achievement()));
        register(plugin, "tpa", new TeleportCommand(services.teleports(), services.menus(), services.achievement()));
        register(plugin, "tpahere", new TeleportCommand(services.teleports(), services.menus(), services.achievement()));
        register(plugin, "tpaccept", new TeleportCommand(services.teleports(), services.menus(), services.achievement()));
        register(plugin, "tpdeny", new TeleportCommand(services.teleports(), services.menus(), services.achievement()));
        register(plugin, "rtp", new TeleportCommand(services.teleports(), services.menus(), services.achievement()));
        register(plugin, "achievement", new AchievementCommand(services.achievement(), services.menus(), services.hud()));
        register(plugin, "ach", new AchievementCommand(services.achievement(), services.menus(), services.hud()));
        register(plugin, "tempdim", new TemporaryDimensionCommand(services.points(), temporaryDimensionManager));
    }

    private static void register(JavaPlugin plugin, String command, CommandExecutor executor) {
        Objects.requireNonNull(plugin.getCommand(command), "Command missing in plugin.yml: " + command).setExecutor(executor);
    }
}
