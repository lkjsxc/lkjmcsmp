package com.lkjmcsmp.plugin;

import com.lkjmcsmp.command.AchievementCommand;
import com.lkjmcsmp.command.HomeCommand;
import com.lkjmcsmp.command.HomeTabCompleter;
import com.lkjmcsmp.command.LkjmcsmpCommand;
import com.lkjmcsmp.command.LkjmcsmpTabCompleter;
import com.lkjmcsmp.command.MenuCommand;
import com.lkjmcsmp.command.PointsCommand;
import com.lkjmcsmp.command.ProfileCommand;
import com.lkjmcsmp.command.TeamCommand;
import com.lkjmcsmp.command.TeleportCommand;
import com.lkjmcsmp.plugin.temporarydimension.TemporaryDimensionCommand;
import com.lkjmcsmp.command.WarpCommand;
import com.lkjmcsmp.plugin.temporarydimension.TemporaryDimensionManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

final class CommandRegistry {
    private CommandRegistry() {
    }

    static void registerAll(JavaPlugin plugin, Services services, TemporaryDimensionManager temporaryDimensionManager) {
        LkjmcsmpCommand rootCommand = new LkjmcsmpCommand(services);
        LkjmcsmpTabCompleter rootCompleter = new LkjmcsmpTabCompleter(services);
        register(plugin, "lkjmcsmp", rootCommand, rootCompleter);
        register(plugin, "lkj", rootCommand, rootCompleter);
        register(plugin, "menu", new MenuCommand(services.menus()));
        register(plugin, "points", new PointsCommand(services.points(), services.menus(), services.achievement(), services.hud()));
        register(plugin, "convert", new PointsCommand(services.points(), services.menus(), services.achievement(), services.hud()));
        register(plugin, "shop", new PointsCommand(services.points(), services.menus(), services.achievement(), services.hud()));
        HomeCommand homeCommand = new HomeCommand(
                services.homes(), services.homeSlotPurchases(),
                services.teleports(), services.achievement(), services.hud());
        HomeTabCompleter homeCompleter = new HomeTabCompleter(services.homes());
        register(plugin, "home", homeCommand, homeCompleter);
        register(plugin, "delhome", homeCommand);
        register(plugin, "homes", homeCommand);
        register(plugin, "warp", new WarpCommand(services.warps(), services.teleports(), services.achievement()));
        register(plugin, "setwarp", new WarpCommand(services.warps(), services.teleports(), services.achievement()));
        register(plugin, "delwarp", new WarpCommand(services.warps(), services.teleports(), services.achievement()));
        register(plugin, "warps", new WarpCommand(services.warps(), services.teleports(), services.achievement()));
        register(plugin, "team", new TeamCommand(services.parties(), services.teleports(), services.achievement()));
        TeleportCommand teleportCommand = new TeleportCommand(
                services.teleports(), services.menus(), services.achievement(), services.messages());
        register(plugin, "tp", teleportCommand);
        register(plugin, "tpa", teleportCommand);
        register(plugin, "tpahere", teleportCommand);
        register(plugin, "tpaccept", teleportCommand);
        register(plugin, "tpdeny", teleportCommand);
        register(plugin, "tpdecision", teleportCommand);
        register(plugin, "rtp", teleportCommand);
        AchievementCommand achievementCommand = new AchievementCommand(
                services.achievement(), services.menus(), services.hud(), services.messages());
        register(plugin, "achievement", achievementCommand);
        register(plugin, "ach", achievementCommand);
        register(plugin, "profile", new ProfileCommand(services.menus()));
        register(plugin, "tempdim", new TemporaryDimensionCommand(services.points(), temporaryDimensionManager));
    }

    private static void register(JavaPlugin plugin, String command, CommandExecutor executor) {
        var pluginCommand = Objects.requireNonNull(plugin.getCommand(command), "Command missing in plugin.yml: " + command);
        pluginCommand.setExecutor(executor);
        if (executor instanceof TabCompleter completer) {
            pluginCommand.setTabCompleter(completer);
        }
    }

    private static void register(JavaPlugin plugin, String command, CommandExecutor executor, TabCompleter completer) {
        var pluginCommand = Objects.requireNonNull(plugin.getCommand(command), "Command missing in plugin.yml: " + command);
        pluginCommand.setExecutor(executor);
        pluginCommand.setTabCompleter(completer);
    }
}
