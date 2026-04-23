package com.lkjmcsmp.command;

import com.lkjmcsmp.gui.MenuService;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
    }
}
