package com.lkjmcsmp.command;

import com.lkjmcsmp.gui.MenuService;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AchievementCommand implements CommandExecutor {
    private final AchievementService achievementService;
    private final MenuService menuService;
    private final ActionBarRouter actionBarHudService;

    public AchievementCommand(
            AchievementService achievementService,
            MenuService menuService,
            ActionBarRouter actionBarHudService) {
        this.achievementService = achievementService;
        this.menuService = menuService;
        this.actionBarHudService = actionBarHudService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
                if (!CommandUtil.requirePermission(sender, "lkjmcsmp.achievement.admin")) {
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("Player not found: " + args[1]);
                    return true;
                }
                var result = achievementService.resetAll(target.getUniqueId());
                sender.sendMessage(result.message());
                return true;
            }

            return CommandUtil.requirePlayer(sender).map(player -> {
                try {
                    if (args.length == 2 && args[0].equalsIgnoreCase("claim")) {
                        var result = achievementService.claim(player.getUniqueId(), args[1]);
                        if (result.success()) {
                            actionBarHudService.refreshIdle(player);
                        }
                        player.sendMessage(result.message());
                        return true;
                    }
                    if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                        for (var entry : achievementService.getViews(player.getUniqueId()).entrySet()) {
                            var view = entry.getValue();
                            player.sendMessage(
                                    entry.getKey()
                                            + " [" + view.status().name() + "] "
                                            + view.progress() + "/" + view.definition().target()
                                            + " :: " + view.definition().description());
                        }
                        return true;
                    }
                    menuService.openAchievement(player);
                } catch (Exception ex) {
                    player.sendMessage("Achievement command failed: " + ex.getMessage());
                }
                return true;
            }).orElse(true);
        } catch (Exception ex) {
            sender.sendMessage("Achievement command failed: " + ex.getMessage());
            return true;
        }
    }
}
